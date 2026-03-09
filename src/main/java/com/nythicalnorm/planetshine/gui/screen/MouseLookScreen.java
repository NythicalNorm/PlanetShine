package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MouseLookScreen extends Screen implements GuiEventListener {
    protected float cameraYrot = 0f;
    protected float cameraXrot = 0f;
    protected float zoomLevel = 2f;
    protected boolean isNonRotView = false;

    protected MouseLookScreen(Component pTitle) {
        super(pTitle);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
        zoomLevel =  zoomLevel * (float) Math.pow(1.1, -pDelta);
        zoomLevel = Mth.clamp(zoomLevel, 1.000001f, getMaxDistanceZoom());
        return true;
    }

    public boolean isNonRotView() {
        return isNonRotView;
    }

    protected abstract float getMaxDistanceZoom();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (PSKeyBinds.INC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(true)));
            return true;
        }

        else if (PSKeyBinds.DEC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(false)));
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public boolean movePlayerCamera() {
        return false;
    }

    public float getCameraZoomLevel(double shipCameraMinDist) {
        float maxRenderDist = Minecraft.getInstance().gameRenderer.getDepthFar() * 0.5f;
        return (float) Math.min(zoomLevel * shipCameraMinDist, maxRenderDist);
    }

    public float getViewYrot() {
        return -cameraYrot*Mth.RAD_TO_DEG;
    }

    public float getViewXrot() {
        return -cameraXrot*Mth.RAD_TO_DEG;
    }
}
