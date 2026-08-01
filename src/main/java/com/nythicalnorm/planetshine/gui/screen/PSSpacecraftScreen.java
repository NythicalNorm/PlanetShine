package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.PSScreenManager;
import com.nythicalnorm.planetshine.gui.widgets.AltitudeWidget;
import com.nythicalnorm.planetshine.gui.widgets.NavballWidget;
import com.nythicalnorm.planetshine.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Objects;

public class PSSpacecraftScreen extends MouseLookScreen implements ISpacecraftOrbitDataDisplay {
    private final PSScreenManager screenManager;
    private final EntityOrbitBody<?> controllingBody;
    private final Options minecraftOptions;
    private NavballWidget navballWidget;
    private final FacingDirection facingDirection;

    public PSSpacecraftScreen(Component pTitle, EntityOrbitBody<?> controllingBody, PSScreenManager screenManager, FacingDirection facingDirection) {
        super(pTitle);
        this.controllingBody = controllingBody;
        this.minecraftOptions = Minecraft.getInstance().options;
        this.screenManager = screenManager;
        this.facingDirection = facingDirection;
    }

    @Override
    protected void init() {
        super.init();
        minecraftOptions.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraftOptions.hideGui = true;

        this.addRenderableWidget(new TimeWarpWidget(0,0, Component.empty()));
        this.navballWidget = this.addRenderableWidget(new NavballWidget(width/2, height, Component.empty()));
        this.addRenderableWidget(new AltitudeWidget(width/2, 0, Component.empty()));

        this.loadState(screenManager.getSpacecraftScreenState());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean keyPressed = false;

        if (PSKeyBinds.OPEN_SPACECRAFT_HUD_KEY.matches(pKeyCode, pScanCode)) {
            this.onClose();
            keyPressed = true;
        } else if (PSKeyBinds.OPEN_SOLAR_SYSTEM_MAP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.get().getScreenManager().setSpacecraftScreenState(new SpacecraftScreenState(
                    this.cameraYrot, this.cameraXrot, this.zoomLevel, this.viewMode, this.navballWidget.getNavBallMode()));

            PSClient.get().getScreenManager().openMapScreen();
            keyPressed = true;
        } else if (PSKeyBinds.CHANGE_SPACECRAFT_VIEW_KEY.matches(pKeyCode, pScanCode)) {
            toggleViewMode();
            keyPressed = true;
        }

        if (keyPressed) {
            return keyPressed;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    private void toggleViewMode() {
        if (PSClient.get().weInSpaceDim()) {
            switch (this.viewMode) {
                case LOCKED -> this.viewMode = ViewMode.NON_ROTATING;
                case NON_ROTATING -> this.viewMode = ViewMode.SURFACE_DOWN;
                case SURFACE_DOWN -> this.viewMode = ViewMode.LOCKED;
            }
            showViewModeMessage(this.viewMode);
        } else {
            switch (this.viewMode) {
                case SURFACE_DOWN, NON_ROTATING -> {
                    this.viewMode = ViewMode.LOCKED;
                    showViewModeMessage(ViewMode.LOCKED);
                }
                case LOCKED -> {
                    this.viewMode = ViewMode.NON_ROTATING;
                    showViewModeMessage(ViewMode.SURFACE_DOWN);
                }
            }
        }
    }

    private void showViewModeMessage(ViewMode mode) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("planetshine.ui.view_mode_set",
                mode.getViewName()), true);
    }

    @Override
    public boolean movePlayerCamera() {
        return true;
    }

    @Override
    protected float getMaxDistanceZoom() {
        return 18;
    }

    @Override
    public Quaterniondc getSpacecraftRotation() {
        return controllingBody.getMCRotation();
    }

    public FacingDirection getFacingDirection() {
        return facingDirection;
    }

    @Override
    public float getGForce() {
        return 0f; //Oof this will be rather hard to calculate on the client side wouldn't it.
    }

    @Override
    public Vector3dc getRelativeVelocity() {
        Vector3dc velocity = controllingBody.getOrbitalElements() != null ?
                controllingBody.getRelativeVelocity() : controllingBody.getMcVelocity();

        return Objects.requireNonNullElseGet(velocity, Vector3d::new);
    }

    @Override
    public double getAltitude() {
        return controllingBody.getAltitude();
    }

    @Override
    public double getAirDensityAtAltitude() {
        if (this.controllingBody.getParent() != null) {
            return AtmosphereCalc.getAirDensity(this.controllingBody.getParent(), this.getAltitude());
        } else {
            return 0.0d;
        }
    }

    @Override
    public OrbitalElementsc getOrbitalElements() {
        return this.controllingBody.getOrbitalElements();
    }

    @Override
    public Vector3dc getRelativePosition() {
        return this.controllingBody.getRelativePos();
    }

    @Override
    public @Nullable PSSpacecraftScreen getSpacecraftScreen() {
        return this;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.saveScreenState();
        PSClient.get().getScreenManager().closeSpacecraftScreen();
    }

    @Override
    public void saveScreenState() {
        PSClient.get().getScreenManager().setSpacecraftScreenState(new SpacecraftScreenState(this.cameraYrot, this.cameraXrot, this.zoomLevel, this.viewMode,  this.navballWidget.getNavBallMode()));
    }

    @Override
    public void removed() {
        super.removed();
    }

    private void loadState(SpacecraftScreenState spacecraftScreenState) {
        if (spacecraftScreenState != null) {
            super.loadRotState(spacecraftScreenState);
            this.viewMode = spacecraftScreenState.getViewMode();
            this.navballWidget.setNavBallMode(spacecraftScreenState.getNavBallMode());
        } else if (Minecraft.getInstance().player != null) {
            this.zoomLevel = 1.1f;
            this.cameraYrot = (float) -Math.toRadians(Minecraft.getInstance().player.getYRot());
            this.cameraXrot = (float) -Math.toRadians(Minecraft.getInstance().player.getXRot());
        }
    }

    public void orbitModeChanged(PSScreenManager.SpacecraftOrbitalState newOrbitState) {
        ViewMode oldViewMode = this.viewMode;

        switch (newOrbitState) {
            case ON_PLANET -> {
                this.viewMode = ViewMode.NON_ROTATING;
                this.navballWidget.setNavBallMode(NavballWidget.NavBallMode.SURFACE);
            }
            case SUB_ORBITAL -> {
                this.viewMode = ViewMode.SURFACE_DOWN;
                this.navballWidget.setNavBallMode(NavballWidget.NavBallMode.SURFACE);
            }
            case IN_ORBIT -> {
                this.viewMode = ViewMode.NON_ROTATING;
                this.navballWidget.setNavBallMode(NavballWidget.NavBallMode.ORBIT);
            }
        }

        if (oldViewMode == ViewMode.LOCKED) {
            this.viewMode = ViewMode.LOCKED;
        }

        this.saveScreenState();
    }

    public static class SpacecraftScreenState extends MouseLookScreen.MouseLookScreenState {
        private final ViewMode viewMode;
        private final NavballWidget.NavBallMode navBallMode;

        public SpacecraftScreenState(float cameraYrot, float cameraXrot, float zoomLevel, ViewMode viewMode, NavballWidget.NavBallMode navBallMode) {
            super(cameraYrot, cameraXrot, zoomLevel);
            this.viewMode = viewMode;
            this.navBallMode = navBallMode;
        }

        public ViewMode getViewMode() {
            return viewMode;
        }

        public NavballWidget.NavBallMode getNavBallMode() {
            return navBallMode;
        }
    }

    public enum FacingDirection {
        North(180.0f),
        South(0.0f),
        East(-90.0f),
        West(90.0f);


        private final float dirAngle;
        FacingDirection(float angle) {
            this.dirAngle = angle;
        }

        public static FacingDirection fromYaw(float yaw) {
            yaw = (float) MiscCalc.wrapDegrees(yaw);

            if (yaw > -45.0f && yaw < 45.0f) {
                return South;
            } else if (yaw > 45.0f && yaw < 135.0f) {
                return West;
            } else if (yaw < -45.0f && yaw > -135.0f) {
                return East;
            } else {
                return North;
            }
        }

        public float getAngle() {
            return this.dirAngle;
        }
    }
}
