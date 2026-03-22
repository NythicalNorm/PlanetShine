package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class DayNightCycleCalc {
    private static final Vector3d upVector = new Vector3d(0d, 1d, 0d);

    public static float getSunAngle(Vector3dc blockPosOnPlanet, Vector3dc planetAbsolutePos, float sunOcclusion) {
        Vector3d blockPosNormalized = blockPosOnPlanet.normalize(new Vector3d());
        Vector3d planetPosNormalized =  planetAbsolutePos.normalize(new Vector3d());

        float dotAngles = (float) blockPosNormalized.dot(planetPosNormalized);

        if (dotAngles < -0.15f) {
            if (sunOcclusion > 0.0f) {
                dotAngles = -0.15f + (sunOcclusion * 0.3f);
            }
        }

        float diff = (dotAngles + 1.0f) * 0.25f;
        float angleDir = (float) blockPosNormalized.cross(planetPosNormalized).dot(upVector);

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

    public static float getSunOcclusionForPlanet(CelestialBody planet, Vector3dc perspectivePos) {
        StarBody starBody = planet.getSolarSystem().getRootStar();
        float percentCovered = 0.0f;

        if (planet.getParent() != null && ! (planet.getParent().equals(starBody)) ) {
            percentCovered = getPlanetPerspectiveOverlap(starBody, planet.getParent(), perspectivePos);
        } else {
            for (CelestialBody body: planet.getPlanetChildren()) {
                float bodyCovered = getPlanetPerspectiveOverlap(starBody, body, perspectivePos);
                if (bodyCovered > percentCovered) {
                    percentCovered = bodyCovered;
                }
            }
        }

        return percentCovered;
    }

    public static float getPlanetPerspectiveOverlap (CelestialBody bodyA, CelestialBody bodyB, Vector3dc perspectivePos) {
        Vector3d bodyARelativePos = new Vector3d(bodyA.getAbsolutePos()).sub(perspectivePos);
        Vector3d bodyBRelativePos = new Vector3d(bodyB.getAbsolutePos()).sub(perspectivePos);

        double angularSizeA = Math.asin(bodyA.getRadius() / bodyARelativePos.length());
        double angularSizeB = Math.asin(bodyB.getRadius() / bodyBRelativePos.length());

        bodyARelativePos.normalize();
        bodyBRelativePos.normalize();

        double angleBetweenCircles = Math.acos(Mth.clamp(bodyARelativePos.dot(bodyBRelativePos), -1.0f, 1.0f));

        // B doesn't cover A
        if (angleBetweenCircles >= (angularSizeA + angularSizeB)) {
            return 0.0f;
        }

        // B fully covers A
        if (angleBetweenCircles <= angularSizeB - angularSizeA) {
            return 1.0f;
        }

        // B fully inside A
        if (angleBetweenCircles <= angularSizeA - angularSizeB)
            return (float) ((angularSizeB*angularSizeB) / (angularSizeA*angularSizeA));

        double overlap = getOverlap(angleBetweenCircles, angularSizeA, angularSizeB);

        return (float) (overlap / (Math.PI * angularSizeA * angularSizeA));
    }

    private static double getOverlap(double angleBetweenCircles, double angularSizeA, double angularSizeB) {
        double alpha = Math.acos((angleBetweenCircles * angleBetweenCircles + angularSizeA * angularSizeA - angularSizeB * angularSizeB) /
                (2 * angleBetweenCircles * angularSizeA)) * 2;
        double beta  = Math.acos((angleBetweenCircles * angleBetweenCircles + angularSizeB * angularSizeB - angularSizeA * angularSizeA) /
                (2 * angleBetweenCircles * angularSizeB)) * 2;

        double area1 = 0.5f * angularSizeA * angularSizeA * (alpha - Math.sin(alpha));
        double area2 = 0.5f * angularSizeB * angularSizeB * (beta  - Math.sin(beta));

        return area1 + area2;
    }
}