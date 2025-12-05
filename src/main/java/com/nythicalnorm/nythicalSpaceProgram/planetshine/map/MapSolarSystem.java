package com.nythicalnorm.nythicalSpaceProgram.planetshine.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.orbit.PlanetaryBody;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.gui.TimeWarpWidget;
import com.nythicalnorm.nythicalSpaceProgram.util.KeyBindings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class MapSolarSystem extends Screen implements GuiEventListener {
    private PlanetaryBody currentFocusedBody;
    private float cameraYrot;
    private float cameraXrot;
    private float zoomLevel = 2f;

    public MapSolarSystem(Component pTitle) {
        super(pTitle);
        NythicalSpaceProgram.getCelestialStateSupplier().ifPresent (celestialStateSupplier -> {
            celestialStateSupplier.setMapScreenOpen(true);
        });
    }

    @Override
    protected void init() {
        if (NythicalSpaceProgram.getCelestialStateSupplier().isPresent()) {
            if (NythicalSpaceProgram.getCelestialStateSupplier().get().isOnPlanet()) {
                currentFocusedBody = NythicalSpaceProgram.getCelestialStateSupplier().get().getCurrentPlanet().get();
                Vector3d playerRelativePos = NythicalSpaceProgram.getCelestialStateSupplier().get().getPlayerData().getRelativePos();
                playerRelativePos.normalize();
                cameraYrot = (float) Math.atan2(playerRelativePos.x,playerRelativePos.z);
                cameraXrot = (float) Math.asin(playerRelativePos.y);
            }
        }
        MapRenderer.initModel();
        this.addRenderableWidget(new TimeWarpWidget(0,0, width, height, Component.empty()));
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.depthMask(false);

        PoseStack mapPosestack = new PoseStack();
        Matrix4f projectionMatrix = new Matrix4f().setPerspective(70, (float) graphics.guiWidth()/graphics.guiHeight(), 0.0000001f, 100.0f);;

        Quaternionf dragCameraRot = new Quaternionf().rotateYXZ(cameraYrot, cameraXrot, 0f); //.mul(yRotQuaternion);
        Vector3d relativeCameraPos = new Vector3d(0d, 0d, zoomLevel * currentFocusedBody.getRadius());
        relativeCameraPos.rotate(new Quaterniond(dragCameraRot.x, dragCameraRot.y,dragCameraRot.z,dragCameraRot.w));
        //Vector3d absoluteCameraPos = currentFocusedBody.getAbsolutePos().add(relativeCameraPos);

        mapPosestack.pushPose();
        mapPosestack.mulPose(dragCameraRot.conjugate());

        MapRenderer.renderSkybox(mapPosestack, projectionMatrix);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();;
        MapRenderer.renderMapObjects(mapPosestack, projectionMatrix, relativeCameraPos, currentFocusedBody);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        super.render(graphics, mouseX, mouseY, partialTick);
        mapPosestack.popPose();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton == 1) {
            float sensitivity = 1.40041507642f;

            cameraYrot = cameraYrot + (float) -Math.sin(sensitivity*(pDragX/width));
            cameraXrot = cameraXrot + (float) -Math.sin(sensitivity*(pDragY/height));
            cameraXrot = Mth.clamp(cameraXrot, -Mth.HALF_PI, Mth.HALF_PI);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        zoomLevel =  zoomLevel * (float) Math.pow(1.075, -pDelta);
        zoomLevel = Mth.clamp(zoomLevel, 1.00000001f, 1000);
        return true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (KeyBindings.OPEN_SOLAR_SYSTEM_MAP_KEY.matches(pKeyCode, pScanCode)) {
            this.onClose();
            return true;
        }

        else if (KeyBindings.INC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
            NythicalSpaceProgram.getCelestialStateSupplier().ifPresent((celestialStateSupplier ->
                    celestialStateSupplier.TryChangeTimeWarp(true)));
            return true;
        }

        else if (KeyBindings.DEC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
                NythicalSpaceProgram.getCelestialStateSupplier().ifPresent((celestialStateSupplier ->
                        celestialStateSupplier.TryChangeTimeWarp(false)));
            return true;
        }
        else if (GLFW.GLFW_KEY_TAB == pKeyCode){

        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        NythicalSpaceProgram.getCelestialStateSupplier().ifPresent (celestialStateSupplier -> {
            celestialStateSupplier.setMapScreenOpen(false);
        });
    }
}
