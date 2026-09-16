package com.nythicalnorm.planetshine.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.mixinducks.PlanetWorldBorder;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.mixinducks.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.util.UniverseStage;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
            if (celestialBody instanceof PlanetaryBody planetaryBody && planetaryBody.getDimensionalProperties().isRenderCustomSkybox()) {
                planetTimeAccessor.ps$setDaylightData(new DaylightData(celestialBody));
            }
        }
        if (level.getWorldBorder() instanceof PlanetWorldBorder planetWorldBorder) {
            planetWorldBorder.ps$setPlanetBorder(celestialBody);
        }
    }

    @Override
    public @Nullable String ps$getBiomeGroupNameAt(int x, int y, int z) {
        if (this.ps$celestialBody == null || this.ps$celestialBody.getCelestialServerData() == null ||
                this.ps$celestialBody.getCelestialServerData().getPlanetGradient() == null) {
            return null;
        }
        return this.ps$celestialBody.getCelestialServerData().getPlanetGradient().getBiomeNameAt(x, y, z, this.ps$celestialBody);
    }

    @ModifyReturnValue(method = "getDayTime", at= @At(value = "RETURN"))
    public long getDayTime(long original) {
        if (this instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists() && ps$isPlanet()) {
            if (UniverseStage.get() != null && UniverseStage.get().getPsCommonConfig().doChangeMCDayTimeValue()) {
                return planetTimeAccessor.ps$getDayTime(0.0d, 0.0d);
            }
        }

        return original;
    }

    @WrapMethod(method = "getSunAngle")
    private float getSpaceSkyDarken(float pPartialTick, Operation<Float> original) {
        if (this instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists() && ps$isPlanet()) {
            float sunAngle = planetTimeAccessor.ps$getSunAngle(0.0d, 0.0d);
            return sunAngle * ((float)Math.PI * 2F);
        }

        return original.call(pPartialTick);
    }
}
