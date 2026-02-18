package com.nythicalnorm.planetshine.solarsystem.bodies;

public interface CelestialBodyAccessor {
    boolean ps$isPlanet();
    CelestialBody ps$getCelestialBody();
    void ps$setCelestialBody(CelestialBody celestialBody);
}
