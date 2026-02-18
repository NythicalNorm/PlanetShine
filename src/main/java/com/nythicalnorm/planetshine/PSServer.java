package com.nythicalnorm.planetshine;

import com.nythicalnorm.planetshine.dimensions.SpaceDimension;
import com.nythicalnorm.planetshine.network.*;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundLoginSolarSystemState;
import com.nythicalnorm.planetshine.network.time.ClientboundSolarSystemTimeUpdate;
import com.nythicalnorm.planetshine.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.planetshine.planettexgen.lod_tex.BiomeColorHolder;
import com.nythicalnorm.planetshine.solarsystem.HostSpaceManager;
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
import com.nythicalnorm.planetshine.storage.SpacecraftDataStorage;
import com.nythicalnorm.planetshine.storage.PSCommonSaveData;
import com.nythicalnorm.planetshine.storage.PSDataPackManager;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import com.nythicalnorm.planetshine.util.Stage;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.valkyrienskies.core.api.util.PhysTickOnly;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import java.util.List;
import java.util.Optional;

public class PSServer extends Stage {
    private static PSServer instance;
    private final MinecraftServer server;
    private PlanetTexHandler planetTexHandler;
    private HostSpaceManager hostSpaceManager;
    private final SpacecraftDataStorage spacecraftDataStorage;
    private long serverRunningTicks; // in VSPhysTicks
    private volatile boolean sleepTimeWarping = false;

    public PSServer(MinecraftServer server, SolarSystem solarSystem) {
        super(solarSystem);
        instance = this;
        this.server = server;
        BiomeColorHolder.init();
        serverRunningTicks = 0;
        spacecraftDataStorage = new SpacecraftDataStorage(server, solarSystem);
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
        Stage.close();
        instance = null;
    }

    public MinecraftServer getServer() {
        return server;
    }

    @PhysTickOnly
    public void OnPhysTick(double delta) {
        serverRunningTicks++;
        solarSystem.UpdatePlanets(currentTime, this.isTimeWarping());
        if (!sleepTimeWarping) {
            setCurrentTime(currentTime + timePassPerTick);
        } else {
            setCurrentTime(currentTime + TimeCalc.TimePerTickToTimePerMilliTick(timeWarpSettings.get(4)));
        }
        hostSpaceManager.onPhysTick();

        if (serverRunningTicks % 3 == 0) {
            PacketHandler.sendToAllClients(new ClientboundSolarSystemTimeUpdate(currentTime));
        }
    }

    public void OnGameTick() {
        hostSpaceManager.onGameTick();
    }

    public void serverStarted() {
        solarSystem.getRootStar().initCalcs();
        PSCommonSaveData PSCommonSaveData = PSDataPackManager.createOrLoadSaveData(server);
        setCurrentTime(PSCommonSaveData.getCurrentTime());
        setTimePassPerTick(PSCommonSaveData.getTimeWarp());
        spacecraftDataStorage.readSpacecraftData(solarSystem);
        this.planetTexHandler = new PlanetTexHandler();

        this.hostSpaceManager = new HostSpaceManager(this, spacecraftDataStorage.readHostSpaces());
        server.execute(() -> planetTexHandler.loadOrCreatePlanetTex(server, this.solarSystem, spacecraftDataStorage.getModSaveFolder()));
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

    public void playerJoined(Player entity) {
        OrbitId playerEntityID = new OrbitId(entity);
        List<CelestialBody> allPlanetaryBodies = solarSystem.getAllPlanetaryBodies().values().stream().toList();
        ServerPlayerOrbitBody playerSpacecraftBody = null;

        if (solarSystem.getAllSpacecraftBodies().containsKey(playerEntityID)) {
            if (solarSystem.getAllSpacecraftBodies().get(playerEntityID) instanceof ServerPlayerOrbitBody pPlrSpacecraftBody) {
                pPlrSpacecraftBody.setPlayer(entity);
                playerSpacecraftBody = pPlrSpacecraftBody;
            }
        } else if (OrbitalBodyUtils.isSpaceLevel(entity.level())) {
            Vec3 spawnPosition = server.overworld().getSharedSpawnPos().getCenter();
            hostSpaceManager.teleportEntity(entity, server.overworld(), spawnPosition.x, spawnPosition.y, spawnPosition.z);
        }

        PacketHandler.sendToPlayer(new ClientboundLoginSolarSystemState(playerSpacecraftBody, allPlanetaryBodies, getCurrentTime(), getTimePassPerTick()), (ServerPlayer) entity);

        if (planetTexHandler != null) {
            planetTexHandler.sendAllTexToPlayer((ServerPlayer) entity, solarSystem.getAllPlanetaryBodies());
            server.execute(() -> PlanetTexHandler.sendBiomeTexToPlayer((ServerPlayer) entity, solarSystem.getDimensionOfPlanet(entity.level().dimension())));
        }
    }

    public void playerLeft(Player player) {
        AbstractPlayerOrbitBody playerOrbitBody = ((PlayerOrbitAccessor)player).getOrbitalBody();
        if (playerOrbitBody != null) {
            playerOrbitBody.playerLeft();
        }
    }

    // Called when the player changes SOIs or joins on orbit artificially like the teleport command
    public void playerTeleportOrbit(CelestialBody body, ServerPlayer player, OrbitalElements elements) {
        OrbitId PlayerID = new OrbitId(player.getUUID());
        AbstractPlayerOrbitBody playerOrbitBody = (AbstractPlayerOrbitBody) solarSystem.getAllSpacecraftBodies().get(PlayerID);

        if (playerOrbitBody != null) {
            solarSystem.playerChangeOrbitalSOIs(playerOrbitBody, body, elements);
            playerOrbitBody.setPlayer(player);
            PacketHandler.sendToPlayer(new ClientboundOrbitSOIChange(PlayerID, body.getOrbitId(), elements), player);
        }
        else  {
            AbstractPlayerOrbitBody.PlayerOrbitBuilder builder = new AbstractPlayerOrbitBody.PlayerOrbitBuilder();
            builder.setPlayer(player);
            builder.setRotation(new Quaternionf());
            builder.setStableOrbit(true);
            builder.setOrbitalElements(elements);

            playerOrbitBody = builder.build();

            solarSystem.playerJoinedOrbital(body, playerOrbitBody);
            PacketHandler.sendToPlayer(new ClientboundOrbitSOIChange(PlayerID, body.getOrbitId(), elements), player);
        }

        OrbitHostSpace playerHostSpace = hostSpaceManager.getOrCreateHostSpace(playerOrbitBody);
        playerOrbitBody.setHostSpace(playerHostSpace.getOrbitIdOfHost());

        hostSpaceManager.teleportEntity(player, server.getLevel(SpaceDimension.SPACE_LEVEL_KEY), playerHostSpace.getOriginPos());
    }

    public void playerCloned(ServerPlayer player) {
        EntityOrbitBody spacecraftBody = solarSystem.getAllSpacecraftBodies().get(new OrbitId(player));
        if (spacecraftBody instanceof ServerPlayerOrbitBody serverPlayerSpacecraftBody) {
            serverPlayerSpacecraftBody.setPlayer(player);
        }

        playerDimChanged(player, player.level().dimension());
    }

    public void playerDimChanged(Player entity, ResourceKey<Level> toDimension) {
        if (toDimension != SpaceDimension.SPACE_LEVEL_KEY) {
            EntityOrbitBody entitySpacecraftBody = solarSystem.getAllSpacecraftBodies().get(new OrbitId(entity));

            if (entitySpacecraftBody instanceof ServerPlayerOrbitBody serverPlayerSpacecraftBody) {
                solarSystem.entityRemoveOrbital(serverPlayerSpacecraftBody);
                PacketHandler.sendToAllClients(new ClientboundOrbitRemove(entitySpacecraftBody.getOrbitId()));
            }
        }
    }

    public ServerLevel getSpaceLevel() {
        return server.getLevel(SpaceDimension.SPACE_LEVEL_KEY);
    }

    public HostSpaceManager getEntityShipManager() {
        return hostSpaceManager;
    }

    public String getSpaceLevelString() {
        return ValkyrienSkies.api().getDimensionId(getSpaceLevel());
    }
}
