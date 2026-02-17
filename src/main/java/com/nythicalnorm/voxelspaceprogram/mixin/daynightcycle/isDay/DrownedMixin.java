package com.nythicalnorm.voxelspaceprogram.mixin.daynightcycle.isDay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.PlanetTimeAccessor;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.class)
public class DrownedMixin {
    @ModifyExpressionValue(method = "okTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        Drowned drowned = (Drowned) (Object) this;
        if (drowned.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(drowned.getX(), drowned.getZ());
        }
        return original;
    }
}
