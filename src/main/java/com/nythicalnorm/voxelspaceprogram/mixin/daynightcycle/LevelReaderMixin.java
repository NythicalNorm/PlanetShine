package com.nythicalnorm.voxelspaceprogram.mixin.daynightcycle;

import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.PlanetTimeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends BlockAndTintGetter, CollisionGetter, SignalGetter, BiomeManager.NoiseBiomeSource {
    @Shadow
    int getMaxLocalRawBrightness(BlockPos pPos, int brightness);

    @Shadow
    int getSkyDarken();

    /**
     * @author NythicalNorm
     * @reason injecting into default interface method doesn't seem to work so once again overwriting this method to give
     * the correct time for a timezone.
     */
    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pPos) {
        Integer darkLevelFromPlanet = null;

        if (this instanceof Level) {
            if (this instanceof PlanetTimeAccessor planetTimeAccessor) {
               darkLevelFromPlanet = planetTimeAccessor.ps$getDarknessAmount(pPos.getX(), pPos.getZ());
            }
        }
        else if (this instanceof WorldGenRegion worldGenRegion) {
            Level level = worldGenRegion.getLevel();
            if (level instanceof PlanetTimeAccessor planetTimeAccessor) {
                darkLevelFromPlanet = planetTimeAccessor.ps$getDarknessAmount(pPos.getX(), pPos.getZ());
            }
        }
        return getMaxLocalRawBrightness(pPos, Objects.requireNonNullElseGet(darkLevelFromPlanet, this::getSkyDarken));
    }
}
