package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.rendering.renderers.PlanetRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MapRenderablePlanet extends MapRenderable {
    protected CelestialBody planetBody;

    public MapRenderablePlanet(CelestialBody planetBody, MapRelativeState mapRelativeState, @Nullable OrbitalBody parentBody) {
        super(mapRelativeState, parentBody);
        this.planetBody = planetBody;
    }

    @Override
    public Vector3f render(PoseStack poseStack, Matrix4f projectionMatrix) {
        Vector3f pos = getPos(planetBody, MapRenderer.getCurrentFocusedBody());
        poseStack.translate(pos.x,pos.y, pos.z);

        float PlanetSize = (float) (2f* MapRenderer.SCALE_FACTOR*planetBody.getRadius());
        poseStack.scale(PlanetSize, PlanetSize, PlanetSize);
        poseStack.mulPose(planetBody.getRotation());

        PlanetRenderer.render(planetBody, poseStack, projectionMatrix);

        return pos;
    }

    public CelestialBody getBody() {
        return planetBody;
    }
}
