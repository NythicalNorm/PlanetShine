package com.nythicalnorm.voxelspaceprogram.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.voxelspaceprogram.PSClient;
import com.nythicalnorm.voxelspaceprogram.gui.PSScreenManager;
import com.nythicalnorm.voxelspaceprogram.gui.widgets.AltitudeWidget;
import com.nythicalnorm.voxelspaceprogram.gui.widgets.LeftPanelWidget;
import com.nythicalnorm.voxelspaceprogram.gui.widgets.NavballWidget;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.voxelspaceprogram.rendering.map.MapRenderer;
import com.nythicalnorm.voxelspaceprogram.util.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class MapSolarSystemScreen extends MouseLookScreen {
    private PSScreenManager screenManager;
    private OrbitalBody[] FocusableBodies;
    private int currentFocusedBodyIndex;
    private final boolean isSpacecraftScreenOpen;

    public MapSolarSystemScreen(boolean PisSpacecraftScreenOpen) {
        super(Component.empty());
        PSClient.getInstance().ifPresent (psClient -> {
            this.screenManager = psClient.getScreenManager();
            psClient.getScreenManager().setMapScreenOpen(true);
        });
        this.isSpacecraftScreenOpen = PisSpacecraftScreenOpen;
    }

    @Override
    protected void init() {
        populateFocusedBodiesList();
        MapRenderer.setScreen(this);
        this.addRenderableWidget(new TimeWarpWidget(0,0, width, height, Component.empty()));
        if (isSpacecraftScreenOpen) {
            this.addRenderableWidget(new NavballWidget(width/2, height, width, height, Component.empty()));
            this.addRenderableWidget(new LeftPanelWidget(0, height, width, height, Component.empty()));
            this.addRenderableWidget(new AltitudeWidget(width/2, 0, width, height, Component.empty()));
        }
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.depthMask(false);
        PoseStack mapPosestack = new PoseStack();
        Matrix4f projectionMatrix = new Matrix4f().setPerspective(70, (float) graphics.guiWidth()/graphics.guiHeight(), 0.0000001f, 100.0f);

        Quaternionf dragCameraRot = new Quaternionf().rotateYXZ(cameraYrot, cameraXrot, 0f); //.mul(yRotQuaternion);
        Vector3d relativeCameraPos = new Vector3d(0d, 0d, zoomLevel * radiusZoomLevel);
        relativeCameraPos.rotate(new Quaterniond(dragCameraRot.x, dragCameraRot.y,dragCameraRot.z,dragCameraRot.w));
        //Vector3d absoluteCameraPos = currentFocusedBody.getAbsolutePos().add(relativeCameraPos);

        mapPosestack.pushPose();
        mapPosestack.mulPose(dragCameraRot.conjugate());

        MapRenderer.renderSkybox(mapPosestack, projectionMatrix);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        MapRenderer.renderMapObjects(graphics, mapPosestack, projectionMatrix, relativeCameraPos, FocusableBodies[currentFocusedBodyIndex]);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        mapPosestack.popPose();

        if (!mapPosestack.clear()) {
            throw new IllegalStateException("popped poses are not closed properly.");
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {

        if (KeyBindings.OPEN_SOLAR_SYSTEM_MAP_KEY.matches(pKeyCode, pScanCode)) {
            this.onClose();
            return true;
        }  else if (isSpacecraftScreenOpen) {
            if (screenManager.getSpacecraftScreen().keyPressed(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
        }

        if (GLFW.GLFW_KEY_TAB == pKeyCode){
            changeFocusBody(1);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (isSpacecraftScreenOpen) {
            if (screenManager.getSpacecraftScreen().keyReleased(pKeyCode, pScanCode, pModifiers)) {
                return true;
            }
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (isSpacecraftScreenOpen) {
            Minecraft.getInstance().setScreen(screenManager.getSpacecraftScreen());
        }
        screenManager.closeMapScreen();
    }

    public void changeFocusBody(int additionalIndex) {
        currentFocusedBodyIndex += additionalIndex;
        if (currentFocusedBodyIndex >= FocusableBodies.length) {
            currentFocusedBodyIndex = 0;
        }
        if (FocusableBodies[currentFocusedBodyIndex] instanceof CelestialBody celestialBody) {
            PSClient psClient = PSClient.get();
            if (psClient.getCurrentPlanet().isPresent()) {
                if (psClient.getCurrentPlanet().get().equals(FocusableBodies[currentFocusedBodyIndex])) {
                    Vector3d playerRelativePos = new Vector3d(psClient.getPlayerOrbit().getRelativePos());
                    playerRelativePos.normalize();
                    cameraYrot = (float) Math.atan2(playerRelativePos.x,playerRelativePos.z);
                    cameraXrot = (float) Math.asin(playerRelativePos.y);
                }
            }
            radiusZoomLevel = celestialBody.getRadius();
        } else if (FocusableBodies[currentFocusedBodyIndex] instanceof EntityOrbitBody) {
            radiusZoomLevel = 1000000;
        }

        MapRenderer.updateMapRenderables(PSClient.get(), FocusableBodies[currentFocusedBodyIndex]);
    }

    private void populateFocusedBodiesList() {
        OrbitalBody currentFocusedBody = null;
        PSClient psClient = PSClient.get();

        if (psClient.isOnPlanet()) {
            currentFocusedBody = psClient.getCurrentPlanet().get();
        } else if (psClient.weInSpaceDim()) {
            currentFocusedBody = psClient.getPlayerOrbit();
        }

        int totalFocusAmount = psClient.getSolarSystem().getAllPlanetaryBodies().size();
        if (currentFocusedBody instanceof EntityOrbitBody) {
            totalFocusAmount += 1;
        }

        //setting the first element to the desired body and later filling in planets that aren't currentfocusedbody
        int index = 0;

        FocusableBodies = new OrbitalBody[totalFocusAmount];
        FocusableBodies[index] = currentFocusedBody;
        currentFocusedBodyIndex = index;

        for (CelestialBody plnt : psClient.getSolarSystem().getAllPlanetaryBodies().values()) {
            if (plnt != currentFocusedBody) {
                index++;
                FocusableBodies[index] = plnt;
            }
        }
        changeFocusBody(0);
    }
}
