package com.nythicalnorm.planetshine.solarsystem.orbits;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.OptionalLong;

// an interface for immutable reference for OrbitalElements.
public interface OrbitalElementsc {
    double ToCartesian(long timeElapsed, Vector3d outPos, Vector3d outVel);
    double getSemiMajorAxis();
    double getEccentricity();
    long getPeriapsisTime();
    double getMeanAngularMotion();
    double getInclination();
    double getArgumentOfPeriapsis();
    double getLongitudeOfAscendingNode();
    double getParentMass();
    double getMu();
    double getOrbitalPeriod();
    long getOrbitalPeriodLong();

    long getLastPeriapsisTime(long elapsedTime);
    OptionalLong getNextPeriapsisTime(long elapsedTime);
    boolean isHyperbolic();
    double getApoapsis();
    double getPeriapsis();
    Quaterniondc getOrbitRotation();
    void initCalcs(double parentMass);

    @Nullable OrbitalCalc.SOIIntercept findOrbitEscapeIntercept(CelestialBody body, long elapsedTime);
}
