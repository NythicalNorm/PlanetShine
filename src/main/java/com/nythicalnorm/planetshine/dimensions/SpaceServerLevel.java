package com.nythicalnorm.planetshine.dimensions;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

public class SpaceServerLevel extends ServerLevel implements PlanetTimeAccessor {
    private HostSpaceManager hostSpaceManager;

    public SpaceServerLevel (MinecraftServer pServer, Executor pDispatcher, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, ServerLevelData pServerLevelData, ResourceKey<Level> pDimension, LevelStem pLevelStem, ChunkProgressListener pProgressListener, boolean pIsDebug, long pBiomeZoomSeed, List<CustomSpawner> pCustomSpawners, boolean pTickTime, @Nullable RandomSequences pRandomSequences) {
        super(pServer, pDispatcher, pLevelStorageAccess, pServerLevelData, pDimension, pLevelStem, pProgressListener, pIsDebug, pBiomeZoomSeed, pCustomSpawners, pTickTime, pRandomSequences);
        this.hostSpaceManager = PSServer.get().getHostSpaceManager();
        this.hostSpaceManager.setSpaceLevel(this);
    }

    public HostSpaceManager getHostSpaceManager() {
        return hostSpaceManager;
    }

    @Override
    public void addDuringCommandTeleport(@NotNull ServerPlayer pPlayer) {
        super.addDuringCommandTeleport(pPlayer);
        this.hostSpaceManager.playerAddedToSpace(pPlayer);
    }

    @Override
    public void addDuringPortalTeleport(@NotNull ServerPlayer pPlayer) {
        super.addDuringPortalTeleport(pPlayer);
        this.hostSpaceManager.playerAddedToSpace(pPlayer);
    }

    @Override
    public void addNewPlayer(@NotNull ServerPlayer pPlayer) {
        super.addNewPlayer(pPlayer);
        this.hostSpaceManager.playerAddedToSpace(pPlayer);
    }

    @Override
    public void addRespawnedPlayer(@NotNull ServerPlayer pPlayer) {
        super.addRespawnedPlayer(pPlayer);
        this.hostSpaceManager.playerAddedToSpace(pPlayer);
    }

    @Override
    public void removePlayerImmediately(@NotNull ServerPlayer pPlayer, Entity.@NotNull RemovalReason pReason) {
        super.removePlayerImmediately(pPlayer, pReason);
        this.hostSpaceManager.playerLeftSpace(pPlayer, pReason);
    }

    @Override
    public boolean setBlock(@NotNull BlockPos pPos, @NotNull BlockState pState, int pFlags, int pRecursionLeft) {
        if (VSGameUtilsKt.isBlockInShipyard(this, pPos)) {
            return super.setBlock(pPos, pState, pFlags, pRecursionLeft);
        } else {
            return false;
        }
    }

    @Override
    public boolean isNaturalSpawningAllowed(BlockPos pPos) {
        return false;
    }

    @Override
    public boolean isNaturalSpawningAllowed(ChunkPos pChunkPos) {
        return false;
    }

    @Override
    public boolean isRaining() {
        return false;
    }

    @Override
    public boolean isThundering() {
        return false;
    }

    @Override
    protected void prepareWeather() {
        this.rainLevel = 0.0f;
        this.thunderLevel = 0.0f;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.hostSpaceManager = null;
    }

    @Override
    public boolean ps$DaylightDataExists() {
        return true;
    }

    @Override
    public float ps$getSunAngle(double x, double z) {
        if (this.hostSpaceManager == null) {
            return 0.0f;
        }

        OrbitHostSpace hostSpace = this.hostSpaceManager.getHostSpaceAt(x, z);
        if (hostSpace != null) {
            return DayNightCycleCalc.getSunAngleFromSunOcclusion(hostSpace.getSunOcclusion());
        }

        return 0.0f;
    }

    @Override
    public int ps$getDarknessAmount(double x, double z) {
        if (this.hostSpaceManager == null) {
            return 0;
        }
        OrbitHostSpace hostSpace = this.hostSpaceManager.getHostSpaceAt(x, z);
        if (hostSpace != null) {
            return DayNightCycleCalc.getDarknessLightLevelFromSunOcclusion(hostSpace.getSunOcclusion());
        }
        return 0;
    }

    @Override
    public boolean ps$isDay(double x, double z) {
        return this.ps$getDarknessAmount(x, z) < 4;
    }
}
