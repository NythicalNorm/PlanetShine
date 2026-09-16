package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.util.calculations.MiscCalc;

public class PlanetDimensionProperties {
    protected boolean renderCustomSkybox;
    protected boolean drawSunriseDisk;
    protected int defaultSkyColor;
    protected boolean affectEntityGravity;
    protected boolean affectVSShipGravity;

    public PlanetDimensionProperties(boolean renderCustomSkybox, boolean drawSunriseDisk, int defaultSkyColor, boolean affectEntityGravity, boolean affectVSShipGravity) {
        this.renderCustomSkybox = renderCustomSkybox;
        this.drawSunriseDisk = drawSunriseDisk;
        this.defaultSkyColor = defaultSkyColor;
        this.affectEntityGravity = affectEntityGravity;
        this.affectVSShipGravity = affectVSShipGravity;
    }

    public boolean isRenderCustomSkybox() {
        return renderCustomSkybox;
    }

    public boolean isDrawSunriseDisk() {
        return drawSunriseDisk;
    }

    public int getDefaultSkyColor() {
        return defaultSkyColor;
    }

    public float[] getDefaultSkyColor(float alpha)
    {
        return MiscCalc.getRGBAFloats(defaultSkyColor, alpha);
    }

    public boolean isAffectEntityGravity() {
        return affectEntityGravity;
    }

    public boolean isAffectVSShipGravity() {
        return affectVSShipGravity;
    }
}
