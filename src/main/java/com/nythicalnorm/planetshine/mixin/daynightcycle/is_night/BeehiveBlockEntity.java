package com.nythicalnorm.planetshine.mixin.daynightcycle.is_night;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.level.block.entity.BeehiveBlockEntity.class)
public class BeehiveBlockEntity {
    @ModifyExpressionValue(method = "releaseOccupant",  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"))
    private static boolean isNight(boolean original, @Local(ordinal = 0, argsOnly = true) BlockPos blockPos, @Local(ordinal = 0, argsOnly = true) Level level) {
        if (level instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return !planetTimeAccessor.ps$isDay(blockPos.getX(), blockPos.getZ());
        }
        return original;
    }
}
