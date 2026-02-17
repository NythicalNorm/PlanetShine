package com.nythicalnorm.voxelspaceprogram.solarsystem.bodies;

public interface CelestialBodyAccessor {
    boolean ps$isPlanet();
    CelestialBody ps$getCelestialBody();
    void ps$setCelestialBody(CelestialBody celestialBody);
}
