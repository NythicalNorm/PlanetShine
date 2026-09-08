package com.nythicalnorm.planetshine.mixinducks;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import org.jetbrains.annotations.Nullable;

public interface CelestialBodyAccessor {
    boolean ps$isPlanet();
    CelestialBody ps$getCelestialBody();
    @Nullable String ps$getBiomeGroupNameAt(int x, int y, int z);
    void ps$setCelestialBody(CelestialBody celestialBody);
}
