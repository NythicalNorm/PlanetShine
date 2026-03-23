package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.rendering.map.OrbitDrawer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.rendering.renderers.PlanetRenderer;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.ProjectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

@OnlyIn(Dist.CLIENT)
public class MapRenderablePlanet extends MapRenderable {
    protected CelestialBody planetBody;

    public MapRenderablePlanet(CelestialBody planetBody, MapRelativeState mapRelativeState) {
        super(mapRelativeState);
        this.planetBody = planetBody;
    }

    @Override
    public Vector3f render(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, OrbitalBody currentFocusedBody) {
        Vector3f pos = MapRenderer.getPos(this.relativeState, planetBody, currentFocusedBody);
        poseStack.translate(pos.x,pos.y, pos.z);

        poseStack.pushPose();
        float PlanetSize = (float) (2f* MapRenderer.SCALE_FACTOR*planetBody.getRadius());
        poseStack.scale(PlanetSize, PlanetSize, PlanetSize);
        poseStack.mulPose(planetBody.getRotation());

        PlanetRenderer.render(planetBody, poseStack, projectionMatrix);
        poseStack.popPose();

        renderChildBodies(graphics, planetBody, currentFocusedBody, poseStack, projectionMatrix);

        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        return pos;
    }

    private void renderChildBodies(GuiGraphics graphics, CelestialBody planetBody, OrbitalBody currentFocusedBody, PoseStack poseStack, Matrix4f projectionMatrix) {
        planetBody.getPlanetChildren().forEach(celestialBody ->
                OrbitDrawer.drawCelestialBodyOrbit(celestialBody, poseStack, projectionMatrix));
        planetBody.getEntityChildren().forEach(entityOrbitBody -> {
            if (this.renderIconForOrbitalBody(graphics, entityOrbitBody, currentFocusedBody, poseStack, projectionMatrix)) {
                RenderSystem.enableBlend();
                OrbitDrawer.drawCurrentEntityOrbit(entityOrbitBody, poseStack, projectionMatrix);
            }
        });
        RenderSystem.disableBlend();
    }

    public boolean renderIconForOrbitalBody(GuiGraphics graphics, EntityOrbitBody<?> entityOrbitBody, OrbitalBody currentFocusedBody, PoseStack poseStack, Matrix4f projectionMatrix) {
        Vector3f pos = MapRenderer.toMapCoordinate(entityOrbitBody.getRelativePos());
        Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);

        Screen screen = Minecraft.getInstance().screen;
        Vector2i screenPos = ProjectionUtils.worldToScreenCoordinate(pos, poseMatrix, projectionMatrix, screen.width, screen.height);
        if (screenPos != null) {
            return entityOrbitBody.drawIcon(graphics, screenPos, 8);
        }
        return false;
    }

    public CelestialBody getBody() {
        return planetBody;
    }
}
