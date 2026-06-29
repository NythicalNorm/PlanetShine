package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public class AtmosphereCalc {
    // Universal constants
    private static final double R = 287.05287;       // J/(kg·K)

    // Atmosphere layers
    // Base heights (m)
    private static final double[] H = {
            0,
            11000,
            20000,
            32000,
            47000,
            51000,
            71000,
            84852
    };

    // Base temperatures (K)
    private static final double[] T = {
            288.15,
            216.65,
            216.65,
            228.65,
            270.65,
            270.65,
            214.65,
            186.946
    };

    // Temperature lapse rates (K/m)
    private static final double[] L = {
            -0.0065,
            0.0,
            0.0010,
            0.0028,
            0.0,
            -0.0028,
            -0.0020
    };

    // Base pressures (Pa)
    private static final double[] P = {
            101325.0,
            22632.06,
            5474.889,
            868.0187,
            110.9063,
            66.93887,
            3.956420
    };

    /**
     * Calculates atmospheric density (kg/m^3)
     * for altitudes up to roughly 120 km.
     */
    public static double getAirDensity(CelestialBody celestialBody, double altitudeMeters) {
        altitudeMeters = (altitudeMeters / celestialBody.getAtmosphere().getAtmosphereHeight()) * 120000.0d;


        if (altitudeMeters < 0.0d) {
            altitudeMeters = 0.0d;
        }

        // Above modeled atmosphere
        if (altitudeMeters > 120000.0d) {
            return 0.0d;
        }

        int layer = H.length - 2;

        for (int i = 0; i < H.length - 1; i++) {
            if (altitudeMeters < H[i + 1]) {
                layer = i;
                break;
            }
        }

        double h0 = H[layer];
        double T0 = T[layer];
        double L0 = L[layer];
        double P0 = P[layer] * celestialBody.getAtmosphere().getAtmosphericPressure();

        double temperature;
        double pressure;

        if (L0 == 0.0) {
            // Isothermal layer
            temperature = T0;
            pressure = P0 * Math.exp((-celestialBody.getAccelerationDueToGravity() * (altitudeMeters - h0))
                            / (R * T0));
        } else {
            // Gradient layer
            temperature = T0 + L0 * (altitudeMeters - h0);
            pressure = P0 * Math.pow( T0 / temperature,
                    celestialBody.getAccelerationDueToGravity() / (R * L0));
        }

        // Ideal gas law
        return pressure / (R * temperature);
    }

    public static Vector3d getPlanetGroundSpeedAt(PlanetaryBody planetBody,Vector3dc position, Vector3fc northPoleDir) {
        double entityDistance = position.length();
        double angleToNorth = northPoleDir.dot((float) (position.x()/entityDistance),
                (float) (position.y()/entityDistance), (float) (position.z()/entityDistance));

        double bodyAngularVelocity = 2 * Math.PI / planetBody.getRotationPeriodInSeconds();
        double speedAtPoint = bodyAngularVelocity * entityDistance * angleToNorth; // cos of acos cancels out for angleToNorth
        Vector3d rotateDirection = new Vector3d(northPoleDir).cross(position).normalize();

        return rotateDirection.mul(speedAtPoint);
    }

    public static Vector3d getDragForce(double airDensity, Vector3d airVelocity, double dragCoefficient, double crossSectionalArea) {
        Vector3d airDir = new Vector3d(airVelocity).normalize();
        double velocity = airVelocity.length();
        double dragForce = 0.5d * airDensity * (velocity * velocity) * crossSectionalArea * dragCoefficient;
        return airDir.mul(dragForce);
    }

    public static Vector3d getDragForce(PlanetaryBody planetaryBody, EntityOrbitBody<?> entityOrbitBody) {
        //Vector3d airVelocity = getPlanetGroundSpeedAt(planetaryBody, entityOrbitBody.getRelativePos(), planetaryBody.getNorthPoleDir());
        Vector3d airVelocity = entityOrbitBody.getRelativeVelocity().negate(new Vector3d());
        double airDensity = getAirDensity(planetaryBody, entityOrbitBody.getAltitude());
        double crossSectionalArea = entityOrbitBody.getCrossSectionalArea(airVelocity);
        return getDragForce(airDensity, airVelocity, 1.0d, crossSectionalArea);
    }
}
