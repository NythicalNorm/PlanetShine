package com.nythicalnorm.nythicalSpaceProgram.planet;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Star extends PlanetaryBody{
    public Star(PlanetAtmosphere effects, @Nullable String[] childBody, double radius, double mass, ResourceLocation texture) {
        super(null, effects, childBody, radius, mass, new Vector3f(0f,1f, 0f), 0, 0, texture);
    }

    public void simulatePlanets(double currentTime) {
        this.simulateChildren(currentTime, new Vector3d(0d, 0d, 0d));
    }

    @Override
    public void UpdateSOIs() {
        this.setSphereOfInfluence(Double.POSITIVE_INFINITY);
        super.UpdateSOIs();
    }
}
