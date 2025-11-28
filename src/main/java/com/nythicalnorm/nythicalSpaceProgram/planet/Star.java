package com.nythicalnorm.nythicalSpaceProgram.planet;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class Star extends PlanetaryBody{
    public Star(PlanetAtmosphere effects, @Nullable String[] childBody, double radius, double mass, ResourceLocation texture) {
        super(null, effects, childBody, radius, mass, 0f, 0, 0, texture);
    }

    public void simulatePlanets(double currentTime) {
        this.simulateChildren(currentTime, new Vector3d(0d, 0d, 0d));
    }

    public void initCalcs() {
        this.setSphereOfInfluence(Double.POSITIVE_INFINITY);
        this.calculateOrbitalPeriod();
        super.UpdateSOIs();
    }
}
