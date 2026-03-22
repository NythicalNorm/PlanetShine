package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class DaylightRegion {
    private float sunAngle;
    private int DarknessAmount;
    private float sunOcclusion;
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
        Vector3d blockPosOnPlanet = PlanetCalc.getPlanetRelativePosition(x, 0, z, celestialBody, false);
        Vector3d planetAbsolutePos = new Vector3d(celestialBody.getAbsolutePos()).add(blockPosOnPlanet);

        this.sunOcclusion = DayNightCycleCalc.getSunOcclusionForPlanet(celestialBody, planetAbsolutePos);
        this.sunAngle = DayNightCycleCalc.getSunAngle(blockPosOnPlanet, planetAbsolutePos, this.sunOcclusion);
        this.DarknessAmount = DayNightCycleCalc.getDarknessLightLevel(this.sunAngle, level);
        this.isDay = this.DarknessAmount < 4;
        this.calculatedThisTick = true;
    }

    public float getSunAngle() {
        return sunAngle;
    }

    public float getSunOcclusion() {
        return sunOcclusion;
    }

    public boolean isOngoingEclipse() {
        return this.sunOcclusion > 0.0f;
    }

    public int getDarknessAmount() {
        return DarknessAmount;
    }

    public boolean isDay() {
        return isDay;
    }
}
