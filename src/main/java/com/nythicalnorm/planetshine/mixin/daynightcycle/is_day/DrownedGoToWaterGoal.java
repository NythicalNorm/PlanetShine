package com.nythicalnorm.planetshine.mixin.daynightcycle.is_day;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.Drowned$DrownedGoToWaterGoal")
public class DrownedGoToWaterGoal {
    @Shadow
    @Final
    private PathfinderMob mob;

    @Shadow
    @Final
    private Level level;

    @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        if (level instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(mob.getX(), mob.getZ());
        }
        return original;
    }
}
