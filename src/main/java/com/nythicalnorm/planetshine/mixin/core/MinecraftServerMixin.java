package com.nythicalnorm.planetshine.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.dimensions.SpaceDimension;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

// Get ready for the most absolutely sus mixin in my mod, but hey this makes it more performant for a single change probably
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract ServerLevel overworld();

    @WrapOperation(method = "createLevels", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)Lnet/minecraft/server/level/ServerLevel;"))
    public ServerLevel initSpaceServerLevel
            (MinecraftServer pServer, Executor pDispatcher, LevelStorageSource.LevelStorageAccess pLevelStorageAccess,
             ServerLevelData pServerLevelData, ResourceKey<Level> pDimension, LevelStem pLevelStem, ChunkProgressListener pProgressListener,
             boolean pIsDebug, long pBiomeZoomSeed, List pCustomSpawners, boolean pTickTime, RandomSequences pRandomSequences,
             Operation<ServerLevel> original) {
        if (pDimension.equals(SpaceDimension.SPACE_LEVEL_KEY)) {
            return new SpaceServerLevel(pServer, pDispatcher, pLevelStorageAccess, pServerLevelData, pDimension,
                    pLevelStem, pProgressListener, pIsDebug,
                    pBiomeZoomSeed, pCustomSpawners, pTickTime, pRandomSequences);
        } else {
            return original.call(pServer, pDispatcher, pLevelStorageAccess, pServerLevelData, pDimension,
                    pLevelStem, pProgressListener, pIsDebug,
                    pBiomeZoomSeed, pCustomSpawners, pTickTime, pRandomSequences);
        }
    }

    @Inject(
            method = "createLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getDataStorage()Lnet/minecraft/world/level/storage/DimensionDataStorage;"
            )
    )
    public void dimensionDataLoaded(CallbackInfo ci) {
        PSServer.get().onDimensionDataLoaded(overworld().getDataStorage());
    }
}
