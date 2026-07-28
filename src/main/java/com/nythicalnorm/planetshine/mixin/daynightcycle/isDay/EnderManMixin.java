package com.nythicalnorm.planetshine.mixin.daynightcycle.isDay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public class EnderManMixin {
    @ModifyExpressionValue(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        EnderMan enderMan = (EnderMan) (Object) this;
        if (enderMan.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(enderMan.getX(), enderMan.getZ());
        }
        return original;
    }
}
