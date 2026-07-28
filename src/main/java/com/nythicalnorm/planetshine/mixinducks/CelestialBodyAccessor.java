package com.nythicalnorm.planetshine.mixinducks;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;

public interface CelestialBodyAccessor {
    boolean ps$isPlanet();
    CelestialBody ps$getCelestialBody();
    void ps$setCelestialBody(CelestialBody celestialBody);
}
