package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.rendering.renderTypes.*;
import com.nythicalnorm.planetshine.rendering.renderers.AtmosphereRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.Collection;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MapRenderer {
    public static final float SCALE_FACTOR = 1/1000000000f;
    private MapRenderable renderTree;
    private MapSolarSystemScreen currentOpenScreen;
    private final Map<OrbitId, MapRenderable> mapRenderables = new Object2ObjectOpenHashMap<>(); // probably need change the renderTree to use this instead.

    public void renderSkybox(PoseStack mapPosestack, Matrix4f projectionMatrix) {
        AtmosphereRenderer.renderSpaceSky(mapPosestack, projectionMatrix);
        PSRenderer.drawStarBuffer(mapPosestack, projectionMatrix, 1.0f);
    }

    public void renderMapObjects(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, Vector3d cameraPos, OrbitalBody currentFocus) {
        if (renderTree == null || currentFocus == null) {
            return;
        }

        Vector3f mapCameraPos = toMapCoordinate(cameraPos);
        poseStack.translate(-mapCameraPos.x, -mapCameraPos.y, -mapCameraPos.z);
        renderTree.propagateRender(graphics, poseStack, projectionMatrix, null, currentFocus);
        ManeuverRenderer.renderFromBody(PSClient.get().getControllingBody(), this.mapRenderables, poseStack, projectionMatrix);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void updateMapRenderables(PSClient css, OrbitalBody currentFocusedBody) {
        this.mapRenderables.clear();
        CelestialBody rootStar = css.getSolarSystem().getRootStar();
        MapRelativeState starMapState = MapRelativeState.AbsolutePos;
        if (rootStar.hasChild(currentFocusedBody)) {
            starMapState = MapRelativeState.FocusedBodyParent;
        } else if (rootStar.equals(currentFocusedBody)) {
            starMapState = MapRelativeState.FocusedBody;
        }

        this.renderTree = new MapRenderablePlanet(rootStar, starMapState);
        mapRenderables.put(rootStar.getOrbitId(), renderTree);
        traverseAndPopulateList(css.getSolarSystem().getRootStar(), currentFocusedBody, renderTree);
    }

    private MapRenderable traverseAndPopulateList(CelestialBody parentBody, OrbitalBody currentFocusedBody, MapRenderable parentRenderableInMap) {
        Collection<CelestialBody> OrbitChildren = parentBody.getPlanetChildren();

        if (OrbitChildren != null) {
            for (CelestialBody childBody : OrbitChildren) {
                boolean isCurrentFocusedBody = childBody.equals(currentFocusedBody);
                MapRelativeState mapState = MapRelativeState.AbsolutePos;
                if (isCurrentFocusedBody) {
                    mapState = MapRelativeState.FocusedBody;
                } else if (childBody.hasChild(currentFocusedBody)) {
                    mapState = MapRelativeState.FocusedBodyParent;
                } else if (currentFocusedBody instanceof CelestialBody currentCelestialBody && currentCelestialBody.hasChild(childBody)) {
                    mapState = MapRelativeState.RelativePos;
                } else if (currentFocusedBody.getParent() != null && currentFocusedBody.getParent().equals(childBody.getParent())) {
                    mapState = MapRelativeState.SameParent;
                }

                MapRenderable renderInMap = new MapRenderablePlanet(childBody, mapState);
                mapRenderables.put(childBody.getOrbitId(), renderInMap);
                parentRenderableInMap.addChildRenderable(traverseAndPopulateList(childBody, currentFocusedBody, renderInMap));
            }
        }

        return parentRenderableInMap;
    }

    public @Nullable MapRenderable getMapRenderable(OrbitId orbitId) {
        return this.mapRenderables.get(orbitId);
    }

    public static Vector3f toMapCoordinate(Vector3dc position) {
        return new Vector3f((float) position.x() * SCALE_FACTOR, (float) position.y() * SCALE_FACTOR, (float) position.z() * SCALE_FACTOR);
    }

    public static Vector3f getPos(MapRelativeState state, OrbitalBody bodyToPlace, OrbitalBody currentFocusedBody) {
        Vector3d returnPos = switch (state) {
            case AbsolutePos -> new Vector3d(bodyToPlace.getAbsolutePos()).sub(currentFocusedBody.getAbsolutePos());
            case RelativePos -> new Vector3d(bodyToPlace.getRelativePos());
            case FocusedBodyParent -> new Vector3d(currentFocusedBody.getRelativePos()).negate();
            case SameParent -> new Vector3d(bodyToPlace.getRelativePos()).sub(currentFocusedBody.getRelativePos());
            default -> new Vector3d(0f, 0f, 0f);
        };

        return MapRenderer.toMapCoordinate(returnPos);
    }

    public void setScreen(MapSolarSystemScreen mapSolarSystem) {
        currentOpenScreen = mapSolarSystem;
    }
}
