package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class OrbitalCalc {
    public static final double ACCELERATION_DUE_TO_GRAVITY_EARTH = 9.80665d;

    public static Vector3d getNewtonAcceleration(double parentMass, Vector3dc relativeOrbitalPos) {
        double distance = relativeOrbitalPos.length();
        Vector3d angleVector = new Vector3d(relativeOrbitalPos).div(distance);
        double F = -(OrbitalElements.UniversalGravitationalConstant * parentMass) / (distance*distance);
        F = F / TimeCalc.PhysTickPerSec; // basically calculating it per phys tick, if the physics tick slow down then time slows down so no need for delta time
        return angleVector.mul(F);
    }

    // copy of a method in Orbital Elements, this is more suited for SOI calcs.
    public static long getTimeStampFromTrueAnomaly(double meanAngularMotion, double trueAnomaly, double eccentricity,
                                                    long lastPeriapsisTime) {
        if (eccentricity < 1) {
            double E = 2 * Math.atan2(Math.tan(trueAnomaly * 0.5d), Math.sqrt((1 + eccentricity) / (1 - eccentricity)));

            double timeDiffTerm = (E - eccentricity * Math.sin(E)) / meanAngularMotion;
            return (lastPeriapsisTime + TimeCalc.timeDoubleToLong(timeDiffTerm));
        } else {
            double cosTrueAnomoly = Math.cos(trueAnomaly);
            double H = invCosh((eccentricity + cosTrueAnomoly) / (1 + eccentricity * cosTrueAnomoly));

            double timeDiffTerm = (eccentricity * Math.sinh(H) - H) / meanAngularMotion;
            return (lastPeriapsisTime + TimeCalc.timeDoubleToLong(timeDiffTerm));
        }
    }

    public static double invCosh(double x) {
        if (x < 1.0) {
            return Double.NaN;
        }
        return Math.log(x + Math.sqrt(x*x - 1));
    }

    public record SOIIntercept(double trueAnomaly, long timeElapsed, OrbitId interceptingBody, boolean isEscape) {}
}