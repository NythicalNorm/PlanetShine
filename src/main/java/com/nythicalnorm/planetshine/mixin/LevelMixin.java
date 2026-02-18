package com.nythicalnorm.planetshine.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public class LevelMixin implements CelestialBodyAccessor {
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
