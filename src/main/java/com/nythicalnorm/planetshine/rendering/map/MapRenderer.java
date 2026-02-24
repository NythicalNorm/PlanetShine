package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.vertex.*;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.rendering.renderTypes.*;
import com.nythicalnorm.planetshine.rendering.renderers.AtmosphereRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class MapRenderer { // this is full of memory leaks like chock-full of them need to fix.
    public static final float SCALE_FACTOR = 1/1000000000f;
    private MapRenderable renderTree;
    private MapSolarSystemScreen currentOpenScreen;

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
    }

    public void updateMapRenderables(PSClient css, OrbitalBody currentFocusedBody) {
        CelestialBody rootStar = css.getSolarSystem().getRootStar();
        MapRelativeState starMapState = MapRelativeState.AbsolutePos;
        if (rootStar.hasChild(currentFocusedBody)) {
            starMapState = MapRelativeState.FocusedBodyParent;
        } else if (rootStar.equals(currentFocusedBody)) {
            starMapState = MapRelativeState.FocusedBody;
        }

        renderTree = new MapRenderablePlanet(rootStar, starMapState, null);
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
                }

                MapRenderable renderInMap = new MapRenderablePlanet(childBody, mapState, parentBody);
                parentRenderableInMap.addChildRenderable(traverseAndPopulateList(childBody, currentFocusedBody, renderInMap));
            }
        }

        return parentRenderableInMap;
    }

    public static Vector3f toMapCoordinate(Vector3dc position) {
        return new Vector3f((float) position.x() * SCALE_FACTOR, (float) position.y() * SCALE_FACTOR, (float) position.z() * SCALE_FACTOR);
    }

    public static Vector3f getPos(MapRelativeState state, OrbitalBody bodyToPlace, OrbitalBody currentFocusedBody) {
        Vector3d returnPos = switch (state) {
            case AbsolutePos -> new Vector3d(bodyToPlace.getAbsolutePos()).sub(currentFocusedBody.getAbsolutePos());
            case RelativePos, AlwaysParentRelative -> new Vector3d(bodyToPlace.getRelativePos());
            case FocusedBodyParent -> new Vector3d(currentFocusedBody.getRelativePos()).negate();
            default -> new Vector3d(0f, 0f, 0f);
        };

        return MapRenderer.toMapCoordinate(returnPos);
    }

    public void setScreen(MapSolarSystemScreen mapSolarSystem) {
        currentOpenScreen = mapSolarSystem;
    }
}
