package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import net.minecraft.world.level.Level;

public class DaylightRegion {
    private float sunAngle;
    private int DarknessAmount;
    private boolean isDay;

    private boolean calculatedThisTick;

    public boolean isCalculatedThisTick() {
        return calculatedThisTick;
    }

    public void setCalculatedThisTick(boolean calculatedThisTick) {
        this.calculatedThisTick = calculatedThisTick;
    }

    public void calculate(int x, int z, CelestialBody celestialBody, Level level) {
        if (level == null) {
            return;
        }
        this.sunAngle = DayNightCycleCalc.getSunAngle(x, z, celestialBody);
        this.DarknessAmount = DayNightCycleCalc.getDarknessLightLevel(this.sunAngle, level);
        this.isDay = this.DarknessAmount < 4;
        this.calculatedThisTick = true;
    }

    public float getSunAngle() {
        return sunAngle;
    }

    public int getDarknessAmount() {
        return DarknessAmount;
    }

    public boolean isDay() {
        return isDay;
    }
}
