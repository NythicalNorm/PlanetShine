package com.nythicalnorm.planetshine.dimensions;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

public class SpaceServerLevel extends ServerLevel {
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
    public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
        if (VSGameUtilsKt.isBlockInShipyard(this, pPos)) {
            return super.setBlock(pPos, pState, pFlags, pRecursionLeft);
        } else {
            return false;
        }
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
        this.hostSpaceManager = null;
        super.close();
    }
}
