package com.nythicalnorm.planetshine;

import com.nythicalnorm.planetshine.dimensions.SpaceDimension;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.network.*;
import com.nythicalnorm.planetshine.network.orbitaldata.*;
import com.nythicalnorm.planetshine.network.time.ClientboundSolarSystemTimeUpdate;
import com.nythicalnorm.planetshine.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.planetshine.planettexgen.lod_tex.BiomeColorHolder;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.planettexgen.handlers.PlanetTexHandler;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import com.nythicalnorm.planetshine.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.storage.SpacecraftDataStorage;
import com.nythicalnorm.planetshine.storage.PSCommonSaveData;
import com.nythicalnorm.planetshine.storage.PSDataPackManager;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.RunnableExecutor;
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.util.PhysTickOnly;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.Optional;

public class PSServer extends UniverseStage {
    private static PSServer instance;
    private final MinecraftServer server;
    private PlanetTexHandler planetTexHandler;
    private HostSpaceManager hostSpaceManager;
    private final SpacecraftDataStorage spacecraftDataStorage;
    private long runningPhysTicks; // in VSPhysTicks
    private volatile boolean sleepTimeWarping = false;
    private final RunnableExecutor physTickRunnable;

    // Called on the server starting event
    public PSServer(MinecraftServer server, SolarSystem solarSystem) {
        super(solarSystem);
        instance = this;
        this.server = server;
        BiomeColorHolder.init(server.registryAccess());
        this.runningPhysTicks = 0;
        this.spacecraftDataStorage = new SpacecraftDataStorage(server, solarSystem);
        this.physTickRunnable = new RunnableExecutor();
        this.initPlanets();
    }

    public void onDimensionDataLoaded(DimensionDataStorage dataStorage) {
        PSCommonSaveData PSCommonSaveData = PSDataPackManager.createOrLoadSaveData(dataStorage);
        this.setCurrentTime(PSCommonSaveData.getCurrentTime());
        this.setTimePassPerTick(PSCommonSaveData.getTimeWarp());
        this.spacecraftDataStorage.readSpacecraftData(solarSystem);
        this.planetTexHandler = new PlanetTexHandler();
        this.hostSpaceManager = new HostSpaceManager(this, solarSystem.getAllSpacecraftBodies().values(), spacecraftDataStorage.readHostSpaces());
    }

    public void serverStarted() {
        this.hostSpaceManager.serverStarted();
        server.execute(() -> planetTexHandler.loadOrCreatePlanetTex(server, this.solarSystem, spacecraftDataStorage.getModSaveFolder()));
    }

    public static PSServer get() {
        return instance;
    }

    public static Optional<PSServer> getInstance() {
        if (instance != null) {
            return Optional.of(instance);
        }
        return Optional.empty();
    }

    public static void close() {
        UniverseStage.close();
        instance.hostSpaceManager.close();
        instance = null;
    }

    public MinecraftServer getMCServer() {
        return server;
    }

    @PhysTickOnly
    public void OnPhysTick(double delta, PhysLevel physLevel) {
        physTickRunnable.executeAll();
        runningPhysTicks++;
        solarSystem.UpdatePlanets(currentTime, this.isTimeWarping());
        solarSystem.UpdateSpacecraft(currentTime, this.isTimeWarping());

        if (!sleepTimeWarping) {
            setCurrentTime(currentTime + timePassPerTick);
        } else {
            setCurrentTime(currentTime + TimeCalc.TimePerTickToTimePerMilliTick(timeWarpSettings.get(4)));
        }
        hostSpaceManager.onPhysTick(physLevel);

        if (runningPhysTicks % 30 == 0) {
            this.getSolarSystem().calculateSpacecraftIntercepts(this.getCurrentTime());
        }
        if (runningPhysTicks % 3 == 0) {
            PSServer.addGameTickRunnable(() -> PacketHandler.sendToAllClients(new ClientboundSolarSystemTimeUpdate(currentTime)));
        }
        this.solarSystem.OnPhysTick(physLevel);
    }

    public void OnGameTick() {
        this.hostSpaceManager.onGameTick();
        this.solarSystem.onServerTick(this.getSpaceLevel());
    }

    public static void addGameTickRunnable(Runnable runnable) {
        if (instance != null) {
            PSServer.get().server.execute(runnable);
        }
    }

    public void saveSolarSys() {
        spacecraftDataStorage.saveSpacecraft(this.solarSystem);
        spacecraftDataStorage.saveHostSpaces(this.hostSpaceManager);
    }

    public void ChangeTimeWarp(long proposedSetTimeWarpSpeed, ServerPlayer player) {
        long timePassPerSec = (long) Mth.clamp(proposedSetTimeWarpSpeed, 0, 5000000);
        timePassPerSec = TimeCalc.TimePerTickToTimePerMilliTick(timePassPerSec);
        setTimePassPerTick(timePassPerSec);

        server.getPlayerList().broadcastSystemMessage(Component.translatable("planetshine.state.settimewarp",
                proposedSetTimeWarpSpeed), true);
        PacketHandler.sendToAllClients(new ClientboundTimeWarpUpdate(true, timePassPerSec));
    }

    public void setSleepTimeWarping(boolean sleepTimeWarping) {
        this.sleepTimeWarping = sleepTimeWarping;
    }

    public void playerJoined(ServerPlayer player) {
        OrbitId playerEntityID = new OrbitId(player);
        List<CelestialBody> allPlanetaryBodies = solarSystem.getAllPlanetaryBodies().values().stream().toList();
        ServerPlayerOrbitBody playerSpacecraftBody = null;

        if (solarSystem.getSpacecraftOrbit(playerEntityID) instanceof ServerPlayerOrbitBody pPlrSpacecraftBody) {
            playerSpacecraftBody = pPlrSpacecraftBody;
            pPlrSpacecraftBody.setBody(player);
        } else if (SpaceUtils.isSpaceLevel(player.level())) {
            Vec3 spawnPosition = server.overworld().getSharedSpawnPos().getCenter();
            hostSpaceManager.teleportEntity(player, server.overworld(), spawnPosition.x, spawnPosition.y, spawnPosition.z);
        }

        PacketHandler.sendToPlayer(new ClientboundLoginPSClientStart(playerSpacecraftBody, allPlanetaryBodies, getCurrentTime(), getTimePassPerTick()), player);
        PacketHandler.sendToPlayer(new ClientboundLoginEntityBodiesList(this.solarSystem.getAllEntitiesOrbitsList()), player);

        if (playerSpacecraftBody != null) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(playerSpacecraftBody.getHostSpaceAccess().getOrbitIdOfHost(),
                    playerSpacecraftBody.getHostSpaceAccess().getOriginPos()), player);
        }

        if (planetTexHandler != null) {
            planetTexHandler.sendAllTexToPlayer(player, solarSystem.getAllPlanetaryBodies());
            // server.execute(() -> PlanetTexHandler.sendBiomeTexToPlayer(player, solarSystem.getDimensionOfPlanet(player.level().dimension())));
        }
    }

    public void playerLeft(Player player) {
        AbstractPlayerOrbitBody playerOrbitBody = ((PlayerOrbitAccessor)player).getOrbitalBody();
        if (playerOrbitBody != null) {
            playerOrbitBody.playerLeft();
        }
    }

    // Called when the player changes SOIs or joins on orbit artificially like the teleport command
    public void playerTeleportToOrbit(CelestialBody planet, ServerPlayer player, OrbitalElements elements) {
        OrbitId PlayerID = new OrbitId(player.getUUID());

        if (solarSystem.getSpacecraftOrbit(PlayerID) instanceof ServerPlayerOrbitBody playerOrbitBody) {
            playerOrbitBody.setBody(player);
            OrbitHostSpace hostSpace = this.hostSpaceManager.getHostSpaceAt(playerOrbitBody.getMcPosition());
            this.physTickRunnable.addRun(() -> hostSpace.changeSOI(planet.getOrbitId(), elements));
        } else {
            AbstractPlayerOrbitBody.PlayerOrbitBuilder builder = new AbstractPlayerOrbitBody.PlayerOrbitBuilder();
            builder.setPlayer(player);
            builder.setStableOrbit(false);
            builder.setOrbitalElements(elements);

            AbstractPlayerOrbitBody playerOrbitBody = builder.build();
            OrbitHostSpace playerHostSpace = hostSpaceManager.getOrCreateHostSpace(playerOrbitBody);

            solarSystem.entityJoinedOrbital(planet, playerOrbitBody);

            hostSpaceManager.teleportEntity(player, server.getLevel(SpaceDimension.SPACE_LEVEL_KEY),
                    playerHostSpace.getOriginPos().x, playerHostSpace.getOriginPos().y, playerHostSpace.getOriginPos().z);

            this.sendPacketsPlayerJoinOrbital(player, playerOrbitBody);
        }
    }

    // player teleport and this are very similar, but eh can't be bothered to actually make a common method or generic glorp.
    public void shipTeleportToOrbit(CelestialBody planet, LoadedServerShip ship, OrbitalElements elements,
                                    Quaterniondc shipNewRotation, Vector3dc shipNewOmega) {
        OrbitId shipID = new OrbitId(ship.getId());

        if (solarSystem.getSpacecraftOrbit(shipID) instanceof ServerSpaceshipBody serverSpaceshipBody) {
            serverSpaceshipBody.setBody(ship);
            OrbitHostSpace hostSpace = this.hostSpaceManager.getHostSpaceAt(serverSpaceshipBody.getMcPosition());
            this.physTickRunnable.addRun(() -> hostSpace.changeSOI(planet.getOrbitId(), elements));
        } else {
            AbstractSpaceshipBody.ShipOrbitBuilder builder = new AbstractSpaceshipBody.ShipOrbitBuilder();
            builder.setShip(ship);
            builder.setStableOrbit(false);
            builder.setOrbitalElements(elements);

            AbstractSpaceshipBody spaceshipOrbitBody = builder.build();
            OrbitHostSpace shipHostSpace = hostSpaceManager.getOrCreateHostSpace(spaceshipOrbitBody);

            ServerLevel levelOld = server.getLevel(VSGameUtilsKt.getResourceKey(ship.getChunkClaimDimension()));

            ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(shipHostSpace.getOriginPos(), shipNewRotation,
            new Vector3d(), shipNewOmega, VSGameUtilsKt.getDimensionId(this.getSpaceLevel()),null, null);

            this.solarSystem.entityJoinedOrbital(planet, spaceshipOrbitBody);
            this.hostSpaceManager.getShipTeleporter().teleportShipsWithEntities(ship, shipTeleportData, levelOld, this.getSpaceLevel());

            PacketHandler.sendToAllClients(new ClientboundEntityBodyJoinOrbital(spaceshipOrbitBody));
        }
    }

    public void sendPacketsPlayerJoinOrbital(ServerPlayer player, AbstractPlayerOrbitBody playerOrbitBody) {
        PacketHandler.sendToPlayer(new ClientboundLocalPlayerJoinOrbital(playerOrbitBody.getParent().getOrbitId(),
                playerOrbitBody.getOrbitalElements()), player);
        PacketHandler.sendToAllPlayersExcept(new ClientboundEntityBodyJoinOrbital(playerOrbitBody), player,
                this.getMCServer().getPlayerList().getPlayers());
    }

    public void playerCloned(ServerPlayer playerNew) {
        EntityOrbitBody<?> spacecraftBody = solarSystem.getSpacecraftOrbit(new OrbitId(playerNew));
        if (spacecraftBody instanceof ServerPlayerOrbitBody serverPlayerSpacecraftBody) {
            serverPlayerSpacecraftBody.setBody(playerNew);
        }
    }

    public SpaceServerLevel getSpaceLevel() {
        return (SpaceServerLevel) server.getLevel(SpaceDimension.SPACE_LEVEL_KEY);
    }

    public HostSpaceManager getHostSpaceManager() {
        return hostSpaceManager;
    }

    public String getSpaceLevelString() {
        return ValkyrienSkies.api().getDimensionId(this.getSpaceLevel());
    }
}
