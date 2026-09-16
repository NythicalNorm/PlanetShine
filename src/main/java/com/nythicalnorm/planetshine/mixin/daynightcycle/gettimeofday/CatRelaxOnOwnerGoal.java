package com.nythicalnorm.planetshine.mixin.daynightcycle.gettimeofday;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Cat$CatRelaxOnOwnerGoal")
public class CatRelaxOnOwnerGoal {
    @Shadow
    @Final
    private Cat cat; // Shadow the field

    @ModifyExpressionValue(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getTimeOfDay(F)F"))
    public float getTimeOfDay(float original) {
        if (cat.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$getSunAngle(cat.getX(), cat.getZ());
        }
        return original;
    }
}
