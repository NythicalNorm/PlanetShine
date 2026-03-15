package com.nythicalnorm.planetshine.solarsystem.orbits;

import org.joml.Quaterniondc;

// an interface for immutable reference for OrbitalElements.
public interface OrbitalElementsc {
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
    double getEccentricityAnomaly();
    long getLastPeriapsisTime(long elapsedTime);
    boolean isHyperbolic();
    double getApoapsis();
    double getPeriapsis();
    Quaterniondc getOrbitRotation();
    void initCalcs(double parentMass);
}
