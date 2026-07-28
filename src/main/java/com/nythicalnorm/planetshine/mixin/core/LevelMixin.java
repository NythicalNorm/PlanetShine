package com.nythicalnorm.planetshine.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.mixinducks.PlanetWorldBorder;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.mixinducks.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class LevelMixin implements CelestialBodyAccessor {
    @Shadow
    public abstract boolean isClientSide();

    @Unique
    CelestialBody ps$celestialBody;

    @Override
    public boolean ps$isPlanet() {
        return ps$celestialBody != null;
    }

    @Override
    public CelestialBody ps$getCelestialBody() {
        return ps$celestialBody;
    }

    @Override
    public void ps$setCelestialBody(CelestialBody celestialBody) {
        this.ps$celestialBody = celestialBody;
        Level level = (Level) (Object)this;
        if (celestialBody != null && level instanceof PlanetTimeAccessor planetTimeAccessor) {
            planetTimeAccessor.ps$setDaylightData(new DaylightData(celestialBody));
        }
        if (level.getWorldBorder() instanceof PlanetWorldBorder planetWorldBorder) {
            planetWorldBorder.ps$setPlanetBorder(celestialBody);
        }
    }

    @ModifyReturnValue(method = "getDayTime", at= @At(value = "RETURN"))
    public long getDayTime(long original) {
        if (this instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists() && ps$isPlanet()) {
            if (PSServer.get() != null) {
                float sunAngle = planetTimeAccessor.ps$getSunAngle(0d, 0d);
                return DayNightCycleCalc.getDayTime(sunAngle, this.ps$celestialBody, PSServer.get().getCurrentTime());
            }
        }

        return original;
    }
}
