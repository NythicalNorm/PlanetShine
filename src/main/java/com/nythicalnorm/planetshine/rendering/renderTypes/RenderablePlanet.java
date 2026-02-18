package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.rendering.renderers.PlanetRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.Optional;

public class RenderablePlanet extends SpaceRenderable {
    private final CelestialBody body;

    public RenderablePlanet(CelestialBody body) {
        super();
        this.body = body;
    }

    public CelestialBody getBody() {
        return body;
    }

    @Override
    public void calculatePos(OrbitalBody relativeTo) {
        Vector3d differenceVector = new Vector3d(this.body.getAbsolutePos());
        differenceVector.sub(relativeTo.getAbsolutePos());
        setDifferenceVector(differenceVector);
        setDistance(relativeTo.getAbsolutePos().distance(this.body.getAbsolutePos()));
    }

    @Override
    public void render(Optional<PlanetAtmosphere> currentPlanetAtmosphere, PoseStack poseStack, Matrix4f projectionMatrix, float currentAlbedo, boolean isCurrentPlanetOn, float opacityEasing) {
        PlanetRenderer.render(body, currentPlanetAtmosphere, poseStack, projectionMatrix, currentAlbedo, isCurrentPlanetOn, opacityEasing);
    }
}
