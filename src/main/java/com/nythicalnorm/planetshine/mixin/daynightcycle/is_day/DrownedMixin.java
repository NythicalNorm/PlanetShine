package com.nythicalnorm.planetshine.mixin.daynightcycle.is_day;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.class)
public class DrownedMixin {
    @ModifyExpressionValue(method = "okTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(boolean original) {
        Drowned drowned = (Drowned) (Object) this;
        if (drowned.level() instanceof PlanetTimeAccessor planetTimeAccessor) {
            return planetTimeAccessor.ps$isDay(drowned.getX(), drowned.getZ());
        }
        return original;
    }
}
