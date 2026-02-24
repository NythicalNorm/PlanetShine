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
    private OrbitalBody currentFocusedBody;
    private MapSolarSystemScreen currentOpenScreen;

    public void renderSkybox(PoseStack mapPosestack, Matrix4f projectionMatrix) {
        AtmosphereRenderer.renderSpaceSky(mapPosestack, projectionMatrix);
        PSRenderer.drawStarBuffer(mapPosestack, projectionMatrix, 1.0f);
    }

    public void renderMapObjects(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, Vector3d cameraPos, OrbitalBody currentFocus) {
        currentFocusedBody = currentFocus;
        if (renderTree == null || currentFocusedBody == null) {
            return;
        }

        Vector3f mapCameraPos = toMapCoordinate(cameraPos);
        poseStack.translate(-mapCameraPos.x, -mapCameraPos.y, -mapCameraPos.z);
        renderTree.propagateRender(poseStack, projectionMatrix, null, currentFocusedBody);

//        for (MapRenderableIcon icon : iconsList) {
//            renderIcon(graphics, icon.getScreenPos(), icon.getPlayerTextureLoc(), 64);
//        }
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

//        Optional<CelestialBody> planetOn = css.getCurrentPlanet();
//        if (planetOn.isPresent()) {
//            homePlanetPlayerDisplay = new MapRenderableIcon(css.getPlayerOrbit(), Minecraft.getInstance().player.getSkinTextureLocation(),
//                    MapRelativeState.AlwaysParentRelative, planetOn.get());
//            iconsList.add(homePlanetPlayerDisplay);
//        }

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

    private void renderIcon(GuiGraphics graphics, int[] screenPos, ResourceLocation TextureLoc, float size) {
        float relativeHeadSize = size/8;
        graphics.blit(TextureLoc, (int) (screenPos[0] - relativeHeadSize*0.5f), (int) (screenPos[1] - relativeHeadSize*0.5f), (int) relativeHeadSize,
                (int) relativeHeadSize,(int) relativeHeadSize, (int) relativeHeadSize,  (int) size, (int) size);
    }

    public static Vector3f toMapCoordinate(Vector3d position) {
        position.mul(SCALE_FACTOR);
        return new Vector3f((float) position.x, (float) position.y, (float) position.z);
    }

    public void setScreen(MapSolarSystemScreen mapSolarSystem) {
        currentOpenScreen = mapSolarSystem;
    }
}
