package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class MouseLookScreen extends Screen {
    protected float cameraYrot = 0f;
    protected float cameraXrot = 0f;
    protected float zoomLevel = 2f;
    protected ViewMode viewMode = ViewMode.NON_ROTATING;
    Component currentMessage = Component.empty();
    float messageRemainingTicks;
    public static final int textColor = 0x00ff2b;

    boolean pressed = false;
    long lastClickTime = 0;

    protected MouseLookScreen(Component pTitle) {
        super(pTitle);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            pressed = true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && pressed) {
            long now = System.currentTimeMillis();

            if (now - lastClickTime < 200) {
                lastClickTime = 0;
                return this.mouseLeftDoubleClicked(pMouseX, pMouseY);
            } else {
                lastClickTime = now;
            }

            pressed = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    protected boolean mouseLeftDoubleClicked(double pMouseX, double pMouseY) {
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

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
    }

    protected void loadRotState(MouseLookScreenState rotState) {
        this.cameraYrot = rotState.getCameraYrot();
        this.cameraXrot = rotState.getCameraXrot();
        this.zoomLevel = rotState.getZoomLevel();
    }

    protected abstract float getMaxDistanceZoom();

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (PSKeyBinds.INC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(true, this.overrideTimeWarpAllowance())));
            return true;
        }

        else if (PSKeyBinds.DEC_TIME_WARP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(false, this.overrideTimeWarpAllowance())));
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public boolean movePlayerCamera() {
        return false;
    }

    public boolean overrideTimeWarpAllowance() {
        return false;
    }

    public float getCameraZoomLevel(double shipCameraMinDist) {
        float maxRenderDist = Minecraft.getInstance().gameRenderer.getDepthFar() * 0.10f;
        return (float) Math.min(zoomLevel * shipCameraMinDist, maxRenderDist);
    }

    public @Nullable PSSpacecraftScreen getSpacecraftScreen() {
        return null;
    }

    public float getViewYrot() {
        return -cameraYrot*Mth.RAD_TO_DEG;
    }

    public float getViewXrot() {
        return -cameraXrot*Mth.RAD_TO_DEG;
    }

    public boolean resetKeysOnScreenOpen() {
        return false;
    }

    public void saveScreenState() {
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.messageRemainingTicks > 0f) {
            renderSystemMessage(pGuiGraphics);
            this.messageRemainingTicks -= pPartialTick;
        }
    }

    private void renderSystemMessage(GuiGraphics pGuiGraphics) {
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float)(this.width / 2), 50f, 0.0F);
        pGuiGraphics.drawString(font, this.currentMessage, -font.width(this.currentMessage) / 2, -4, textColor);
        pGuiGraphics.pose().popPose();
    }

    public void setSystemMessage(Component message) {
        this.currentMessage = message;
        this.messageRemainingTicks = 60f;
    }

    public static class MouseLookScreenState {
        private final float cameraYrot;
        private final float cameraXrot;
        private final float zoomLevel;

        public MouseLookScreenState(float cameraYrot, float cameraXrot, float zoomLevel) {
            this.cameraYrot = cameraYrot;
            this.cameraXrot = cameraXrot;
            this.zoomLevel = zoomLevel;
        }

        public float getCameraYrot() {
            return cameraYrot;
        }

        public float getCameraXrot() {
            return cameraXrot;
        }

        public float getZoomLevel() {
            return zoomLevel;
        }
    }

    public enum ViewMode {
        LOCKED("Locked"),
        NON_ROTATING("Non Rotating"),
        SURFACE_DOWN("Surface Down");

        private final String viewName;

        // Constructor
        ViewMode(String name) {
            this.viewName = name;
        }

        public String getViewName() {
            return viewName;
        }
    }
}
