package com.nythicalnorm.planetshine.mixin.daynightcycle.isDay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Fox.class)
public class FoxMixin {
    @ModifyExpressionValue(method = "getAmbientSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        Fox fox = (Fox) (Object) this;
        if (fox.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(fox.getX(), fox.getZ());
        }
        return original;
    }
}
