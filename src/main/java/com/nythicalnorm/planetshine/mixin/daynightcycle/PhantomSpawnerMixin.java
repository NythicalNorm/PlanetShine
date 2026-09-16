package com.nythicalnorm.planetshine.mixin.daynightcycle;

import com.llamalad7.mixinextras.sugar.Local;
import com.nythicalnorm.planetshine.mixinducks.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 72000, ordinal = 0))
    public int changeSleepTimeNeeded(int constant, @Local(argsOnly = true) ServerLevel level) {
        if (level instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            if (((CelestialBodyAccessor)level).ps$getCelestialBody() instanceof PlanetaryBody planetaryBody) {
                return (int) (planetaryBody.getRotationPeriodInSeconds() * 2.0d * 20.0d);
            }
        }
        return constant;
    }
}
