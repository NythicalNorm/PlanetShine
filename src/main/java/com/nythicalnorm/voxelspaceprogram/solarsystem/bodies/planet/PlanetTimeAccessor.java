package com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet;

public interface PlanetTimeAccessor {
    boolean ps$DaylightDataExists();
    void ps$setDaylightData(DaylightData daylightData);
    float ps$getSunAngle(double x, double z);
    int ps$getDarknessAmount(double x, double z);
    boolean ps$isDay(double x, double z);
}
