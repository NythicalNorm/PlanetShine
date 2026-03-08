package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class DayNightCycleCalc {
    private static final Vector3d upVector = new Vector3d(0d, 1d, 0d);

    public static float getSunAngle(int x, int z, CelestialBody planet) {
        Vector3d blockPosOnPlanet = PlanetCalc.planetDimPosToNormalizedVector(new Vec3(x, 0, z),
                planet.getRadius(), planet.getRotation(), true);
        Vector3d planetAbsolutePos = new Vector3d(planet.getAbsolutePos()).add(blockPosOnPlanet);

        blockPosOnPlanet.normalize();
        planetAbsolutePos.normalize();

        float diff = (float) (blockPosOnPlanet.dot(planetAbsolutePos) + 1.0d) * 0.25f;
        float angleDir = (float) blockPosOnPlanet.cross(planetAbsolutePos).dot(upVector);

        diff = angleDir > 0f ? diff : 1.0f - diff;
        return Mth.clamp(diff, 0f, 1f);
    }

    public static int getDarknessLightLevel(float sunAngle, Level level) {
        double rainLevel = 1.0D - (double) (level.getRainLevel(1.0F) * 5.0F) / 16.0D;
        double ThunderLevel = 1.0D - (double) (level.getThunderLevel(1.0F) * 5.0F) / 16.0D;
        double adjustedDarkness = 0.5D + 2.0D * Mth.clamp(Mth.cos(sunAngle * ((float) Math.PI * 2F)), -0.25D, 0.25D);
        return (int) ((1.0D - adjustedDarkness * rainLevel * ThunderLevel) * 11.0D);
    }

    public static long getDayTime(float angle, CelestialBody clst, long TimeElapsed) {
        long extraTime = 0;

        if (clst instanceof PlanetaryBody planetaryBody) {
            extraTime =  TimeElapsed / planetaryBody.getRotationPeriod();
        }

        return (long) (angle * 24000f) + (extraTime * 24000L);
    }
}
