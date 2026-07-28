package com.nythicalnorm.planetshine.mixinducks;

import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightData;

public interface PlanetTimeAccessor {
    boolean ps$DaylightDataExists();
    float ps$getSunAngle(double x, double z);
    int ps$getDarknessAmount(double x, double z);
    boolean ps$isDay(double x, double z);
    default void ps$setDaylightData(DaylightData daylightData) {
    }
}
