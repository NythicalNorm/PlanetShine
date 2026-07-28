package com.nythicalnorm.planetshine.mixin.daynightcycle.isDay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StrollThroughVillageGoal.class)
public class StrollThroughVillageGoalMixin {
    @Shadow
    @Final
    private PathfinderMob mob;

    @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        if (mob.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(mob.getX(), mob.getZ());
        }
        return original;
    }
}
