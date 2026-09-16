package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.rendering.map.IconRenderer;
import com.nythicalnorm.planetshine.rendering.map.MapIconRenderable;
import com.nythicalnorm.planetshine.rendering.map.OrbitDrawer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.rendering.renderers.PlanetRenderer;
import com.nythicalnorm.planetshine.util.ProjectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

@OnlyIn(Dist.CLIENT)
public class MapRenderablePlanet extends MapRenderable implements MapIconRenderable {
    protected CelestialBody planetBody;
    private Vector2i mapPos;

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

        boolean shouldDrawPlanetIcon = this.shouldDrawIcon();
        if (shouldDrawPlanetIcon) {
            this.renderIconForOrbitalBody(graphics, new Vector3d(), this, currentFocusedBody, poseStack, projectionMatrix);
        } else {
            this.setLatestMapPos(null);
        }

        this.renderChildBodies(graphics, planetBody, currentFocusedBody, poseStack, projectionMatrix, shouldDrawPlanetIcon);

        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        return pos;
    }

    private void renderChildBodies(GuiGraphics graphics, CelestialBody planetBody, OrbitalBody currentFocusedBody,
                                   PoseStack poseStack, Matrix4f projectionMatrix, boolean shouldDrawParentPlanetIcon) {
        planetBody.getPlanetChildren().forEach(celestialBody ->
                OrbitDrawer.drawCelestialBodyOrbit(celestialBody, poseStack, projectionMatrix));
        planetBody.getEntityChildren().forEach(entityOrbitBody -> {
            if (entityOrbitBody instanceof MapIconRenderable mapIconRenderable && mapIconRenderable.shouldDrawIcon()) {
                RenderSystem.enableBlend();
                OrbitDrawer.drawCurrentEntityOrbit(entityOrbitBody, poseStack, projectionMatrix);
                if (!shouldDrawParentPlanetIcon) {
                    this.renderIconForOrbitalBody(graphics, entityOrbitBody.getRelativePos(), mapIconRenderable,
                            currentFocusedBody, poseStack, projectionMatrix);
                } else {
                    mapIconRenderable.setLatestMapPos(null);
                }
            }
        });
        RenderSystem.disableBlend();
    }

    public void renderIconForOrbitalBody(GuiGraphics graphics, Vector3dc relativePos, MapIconRenderable mapIconRenderable,
                                         OrbitalBody currentFocusedBody, PoseStack poseStack, Matrix4f projectionMatrix) {
        Vector3f pos = MapRenderer.toMapCoordinate(relativePos);
        Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);

        Screen screen = Minecraft.getInstance().screen;
        Vector2i screenPos = ProjectionUtils.worldToScreenCoordinate(pos, poseMatrix, projectionMatrix, screen.width, screen.height);
        if (screenPos != null) {
            mapIconRenderable.drawIcon(graphics, screenPos, 8);
        } else {
            mapIconRenderable.setLatestMapPos(null);
        }
    }

    public CelestialBody getBody() {
        return planetBody;
    }

    @Override
    public Vector2ic getLatestMapPos() {
        return this.mapPos;
    }

    @Override
    public void setLatestMapPos(Vector2i pos) {
        mapPos = pos;
    }

    @Override
    public void drawIcon(GuiGraphics graphics, Vector2i screenPos, int i) {
        this.setLatestMapPos(screenPos);
        float[] planetColor = this.planetBody.getAtmosphere().getSurfaceColor(1.0f);
        RenderSystem.setShaderColor(planetColor[0],planetColor[1],planetColor[2],planetColor[3]);
        IconRenderer.drawIcon(graphics, IconRenderer.DEFAULT_PLANET_ICON, screenPos);
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
    }

    @Override
    public boolean shouldDrawIcon() {
        if (this.planetBody.getParent() == null) {
            return false;
        } else if (Minecraft.getInstance().screen instanceof MapSolarSystemScreen mapSolarSystemScreen) {
            MapRenderablePlanet parentRenderablePlanet = (MapRenderablePlanet) mapSolarSystemScreen.getMapRenderer().getMapRenderable(this.planetBody.getParent().getOrbitId());
            if (parentRenderablePlanet == null) {
                return false;
            }

            if (!parentRenderablePlanet.shouldDrawIcon() || parentRenderablePlanet.planetBody.getParent() == null) {
                double distanceToCamera = this.planetBody.getAbsolutePos().distance(mapSolarSystemScreen.getAbsoluteCameraPos());
                return distanceToCamera >= (this.planetBody.getSphereOfInfluence() * 8);
            }
        }
        return false;
    }
}
