package com.nythicalnorm.voxelspaceprogram.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.PlanetAtmosphere;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public abstract class SpaceRenderable {
    protected Vector3d differenceVector;
    protected double distance;

    public SpaceRenderable() {
        this.distance = Double.POSITIVE_INFINITY;
        differenceVector = new Vector3d();
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double dist) {
        this.distance = dist;
    }

    public void setDifferenceVector(Vector3d differenceVector) {
        this.differenceVector = differenceVector;
    }

    public Vector3d getDifferenceVector() {
        return differenceVector;
    }

    public Vector3f getNormalizedDiffVectorf() {
        return new Vector3f((float) differenceVector.x, (float)differenceVector.y, (float) differenceVector.z).normalize();
    }

    public abstract void calculatePos(OrbitalBody relativeTo);

    public abstract void render(Optional<PlanetAtmosphere> currentPlanetAtmosphere, PoseStack poseStack, Matrix4f projectionMatrix, float currentAlbedo, boolean isCurrentPlanetOn, float opacityEasing);
}
