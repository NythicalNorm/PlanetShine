package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;

public class PlanetAtmosphere {
    protected final boolean hasAtmosphere;
    protected final int surfaceColor;
    protected final int atmoColor;
    protected final double atmosphereHeight;
    protected final double atmospherePressureMultiplier;
    protected double atmosphereDensityAtSeaLevel;
    protected final float atmosphereAlpha;
    protected final float alphaNight;
    protected final float alphaDay;

    public PlanetAtmosphere(boolean hasAtmosphere, int surfaceColor, int atmoColor, double atmosphereHeight,
                            double atmospherePressureMultiplier, float atmosphereAlpha, float alphaNight, float alphaDay) {
        this.hasAtmosphere = hasAtmosphere;
        this.surfaceColor = surfaceColor;
        this.atmoColor = atmoColor;
        this.atmosphereHeight = atmosphereHeight;
        this.atmospherePressureMultiplier = atmospherePressureMultiplier;
        this.atmosphereAlpha = atmosphereAlpha;
        this.alphaNight = alphaNight;
        this.alphaDay = alphaDay;
    }

    public void init(CelestialBody celestialBody) {
        this.atmosphereDensityAtSeaLevel = AtmosphereCalc.getAirDensity(celestialBody, 0.0d);
    }

    public boolean hasAtmosphere() {
        return hasAtmosphere;
    }

    public float[] getSurfaceColor(float alpha)
    {
        return MiscCalc.getRGBAFloats(surfaceColor, alpha);
    }

    public float[] getAtmoColor() {
        return MiscCalc.getRGBAFloats(atmoColor, 1.0f);
    }

    public int getOverlayColorInt()
    {
        return surfaceColor;
    }

    public int getAtmoColorInt() {
        return atmoColor;
    }

    public double getAtmosphereHeight() {
        return atmosphereHeight;
    }

    public double getAtmosphericPressureMultiplier() {
        return atmospherePressureMultiplier;
    }

    public double getAtmosphereDensityAtSeaLevel() {
        return atmosphereDensityAtSeaLevel;
    }

    public float getAlphaNight() {
        return alphaNight;
    }

    public float getAlphaDay() {
        return alphaDay;
    }

    public float getAtmosphereAlpha() {
        return atmosphereAlpha;
    }
}
