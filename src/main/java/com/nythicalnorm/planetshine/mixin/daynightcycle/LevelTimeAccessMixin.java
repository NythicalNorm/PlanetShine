package com.nythicalnorm.planetshine.mixin.daynightcycle;


import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.mixinducks.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.Optional;

@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin extends LevelReader { // basically the whole interface is replaced lol.
    @Shadow
    long dayTime();

    /**
     * @author NythicalNorm
     * @reason  This is the easiest way to change the apparent dayTime on the client side I would use inject if I could
     * but this is an interface. The use of the original function is still there and I will make sure the  function
     * doesn't crash the whole thing. And even if there is another mod replacing this it won't crash the game, I mean
     * changing a float output shouldn't cause problems, I will make sure my value is clamped to the original 0 - 1.
     */
    @Overwrite
    default float getTimeOfDay(float pPartialTick) {
        if (this instanceof SpaceServerLevel) {
            return this.dimensionType().timeOfDay(6000L);
        } else if (this instanceof PlanetTimeAccessor planetTimeAccessor) {
            return planetTimeAccessor.ps$getSunAngle(0d, 0d);
        }

        return this.dimensionType().timeOfDay(this.dayTime());
    }

    /**
     * @author NythicalNorm
     * @reason Same reason as above
     */
    @Overwrite
    default float getMoonBrightness() {
        if (this instanceof CelestialBodyAccessor celestialBodyAccessor && celestialBodyAccessor.ps$isPlanet()) {
            CelestialBody celestialBody = celestialBodyAccessor.ps$getCelestialBody();
            Optional<CelestialBody> biggestMoon = celestialBody.getPlanetChildren().stream().max(Comparator.comparingDouble(CelestialBody::getRadius));

            if (biggestMoon.isPresent()) {
                Vector3d moonToPlanet = new Vector3d(biggestMoon.get().getRelativePos()).negate();
                Vector3d moonToSun = new Vector3d(biggestMoon.get().getAbsolutePos()).negate();
                double angleBetween = moonToPlanet.normalize().dot(moonToSun.normalize());
                return (float) ((angleBetween + 1.0f) / 2.0f);
            }
        }
        return DimensionType.MOON_BRIGHTNESS_PER_PHASE[this.dimensionType().moonPhase(this.dayTime())];
    }

    /**
     * @author NythicalNorm
     * @reason Same reason as above
     */
    @Overwrite
    default int getMoonPhase() {
        if (this instanceof CelestialBodyAccessor celestialBodyAccessor && celestialBodyAccessor.ps$isPlanet()) {
            CelestialBody celestialBody = celestialBodyAccessor.ps$getCelestialBody();
            Optional<CelestialBody> biggestMoon = celestialBody.getPlanetChildren().stream().max(Comparator.comparingDouble(CelestialBody::getRadius));

            if (biggestMoon.isPresent()) {
                Vector3d moonToPlanet = new Vector3d(biggestMoon.get().getRelativePos()).negate();
                Vector3d moonToSun = new Vector3d(biggestMoon.get().getAbsolutePos()).negate();
                double angleBetween = moonToPlanet.normalize().dot(moonToSun.normalize());
                if (angleBetween > 0.6) {
                    return 0;
                } else if (angleBetween < 0.6d && angleBetween > 0.2d) {
                    return 1;
                } else if (angleBetween < 0.2d && angleBetween > -0.2d) {
                    return 2;
                } else if (angleBetween < -0.2d && angleBetween > -0.6d) {
                    return 3;
                } else if (angleBetween < -0.6d) {
                    return 4;
                }
            }
        }
        return this.dimensionType().moonPhase(this.dayTime());
    }
}
