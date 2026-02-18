package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class OrbitalCalc {
    public static Vector3d getNewtonAcceleration(double parentMass, Vector3dc relativeOrbitalPos) {
        double distance = relativeOrbitalPos.length();
        Vector3d angleVector = new Vector3d(relativeOrbitalPos).div(distance);
        double F = -(OrbitalElements.UniversalGravitationalConstant * parentMass) / (distance*distance);
        F = F / TimeCalc.PhysTickPerSec; // basically calculating it per phys tick, if the physics tick slow down then time slows down so no need for delta time
        return angleVector.mul(F);
    }
}
