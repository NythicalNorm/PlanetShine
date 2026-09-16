package com.nythicalnorm.planetshine.mixin.daynightcycle.is_night;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bee.class)
public class BeeMixin {
    @ModifyExpressionValue(method = "wantsToEnterHive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"))
    public boolean isNight(boolean original) {
        Bee bee = (Bee) (Object) this;
        if (bee.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return !planetTimeAccessor.ps$isDay(bee.getX(), bee.getZ());
        }
        return original;
    }
}
