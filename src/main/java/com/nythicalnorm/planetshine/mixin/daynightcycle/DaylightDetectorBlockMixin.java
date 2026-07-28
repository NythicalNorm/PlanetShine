package com.nythicalnorm.planetshine.mixin.daynightcycle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DaylightDetectorBlock.class)
public class DaylightDetectorBlockMixin {
    @WrapOperation(method = "updateSignalStrength", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSunAngle(F)F"))
    private static float getSunAngle(Level level, float pPartialTicks, Operation<Float> original, @Local(argsOnly = true) BlockPos blockPos) {
        if (level instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$getSunAngle(blockPos.getX(), blockPos.getZ());
        }
        return original.call(level, pPartialTicks);
    }

    @WrapOperation(method = "updateSignalStrength", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSkyDarken()I"))
    private static int getSkyDarken(Level level, Operation<Integer> original, @Local(argsOnly = true) BlockPos blockPos) {
        if (level instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$getDarknessAmount(blockPos.getX(), blockPos.getZ());
        }
        return original.call(level);
    }
}
