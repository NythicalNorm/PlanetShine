package com.nythicalnorm.voxelspaceprogram;

import com.nythicalnorm.voxelspaceprogram.dimensions.SpaceDimension;
import com.nythicalnorm.voxelspaceprogram.network.*;
import com.nythicalnorm.voxelspaceprogram.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.voxelspaceprogram.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.voxelspaceprogram.network.orbitaldata.ClientboundLoginSolarSystemState;
import com.nythicalnorm.voxelspaceprogram.network.time.ClientboundSolarSystemTimeUpdate;
import com.nythicalnorm.voxelspaceprogram.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.voxelspaceprogram.planettexgen.lod_tex.BiomeColorHolder;
import com.nythicalnorm.voxelspaceprogram.solarsystem.EntityShipManager;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.voxelspaceprogram.planettexgen.handlers.PlanetTexHandler;
import com.nythicalnorm.voxelspaceprogram.solarsystem.SolarSystem;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.voxelspaceprogram.storage.SpacecraftDataStorage;
import com.nythicalnorm.voxelspaceprogram.storage.VSPCommonSaveData;
import com.nythicalnorm.voxelspaceprogram.storage.VSPDataPackManager;
import com.nythicalnorm.voxelspaceprogram.util.OrbitalBodyUtils;
import com.nythicalnorm.voxelspaceprogram.util.Stage;
import com.nythicalnorm.voxelspaceprogram.util.calculations.TimeCalc;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
    private final EntityShipManager entityShipManager;
    private final SpacecraftDataStorage spacecraftDataStorage;
    private long serverRunningTicks; // in VSPhysTicks

    public PSServer(MinecraftServer server, SolarSystem solarSystem) {
        super(solarSystem);
        instance = this;
        this.server = server;
        BiomeColorHolder.init();
        serverRunningTicks = 0;
        spacecraftDataStorage = new SpacecraftDataStorage(server, solarSystem);
        entityShipManager = new EntityShipManager(this, new Object2ObjectOpenHashMap<>());
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
        currentTime = currentTime + timePassPerTick;

        if (serverRunningTicks % 3 == 0) {
            PacketHandler.sendToAllClients(new ClientboundSolarSystemTimeUpdate(currentTime));
        }
    }

    public void OnGameTick() {
        entityShipManager.onGameTick();
    }

    public void serverStarted() {
        solarSystem.getRootStar().initCalcs();
        VSPCommonSaveData vspCommonSaveData = VSPDataPackManager.createOrLoadSaveData(server);
        setCurrentTime(vspCommonSaveData.getCurrentTime());
        setTimePassPerTick(vspCommonSaveData.getTimeWarp());
        spacecraftDataStorage.readSpacecraftData(solarSystem);
        this.planetTexHandler = new PlanetTexHandler();
        server.execute(() -> planetTexHandler.loadOrCreatePlanetTex(server, this.solarSystem, spacecraftDataStorage.getModSaveFolder()));
    }

    public void saveSolarSys() {
        spacecraftDataStorage.save(solarSystem);
    }

    public void ChangeTimeWarp(long proposedSetTimeWarpSpeed, ServerPlayer player) {
        long timePassPerSec = (long) Mth.clamp(proposedSetTimeWarpSpeed, 0, 5000000);
        timePassPerSec = TimeCalc.TimePerTickToTimePerMilliTick(timePassPerSec);

        setTimePassPerTick(timePassPerSec);
        server.getPlayerList().broadcastSystemMessage(Component.translatable("voxelspaceprogram.state.settimewarp",
                proposedSetTimeWarpSpeed), true);
        PacketHandler.sendToAllClients(new ClientboundTimeWarpUpdate(true, timePassPerSec));
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
            entityShipManager.teleportEntity(entity, server.overworld(), spawnPosition.x, spawnPosition.y, spawnPosition.z);
        }

        Optional<OrbitId> playerHostSpace = playerSpacecraftBody != null ? playerSpacecraftBody.getCurrentHostSpace() : Optional.empty();
        PacketHandler.sendToPlayer(new ClientboundLoginSolarSystemState(playerSpacecraftBody, playerHostSpace, allPlanetaryBodies, getCurrentTime(), getTimePassPerTick()), (ServerPlayer) entity);

        if (planetTexHandler != null) {
            planetTexHandler.sendAllTexToPlayer((ServerPlayer) entity, solarSystem.getAllPlanetaryBodies());
            server.execute(() -> PlanetTexHandler.sendBiomeTexToPlayer((ServerPlayer) entity, solarSystem.getDimensionOfPlanet(entity.level().dimension())));
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

        OrbitHostSpace playerHostSpace = entityShipManager.getOrCreateHostSpace(playerOrbitBody);
        playerOrbitBody.setHostSpace(playerHostSpace.getOrbitIdOfHost());

        if (!OrbitalBodyUtils.isSpaceLevel(player.level())) {
            entityShipManager.teleportEntity(player, server.getLevel(SpaceDimension.SPACE_LEVEL_KEY), playerHostSpace.getOriginPos());
        }
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

    public EntityShipManager getEntityShipManager() {
        return entityShipManager;
    }

    public String getSpaceLevelString() {
        return ValkyrienSkies.api().getDimensionId(getSpaceLevel());
    }
}
