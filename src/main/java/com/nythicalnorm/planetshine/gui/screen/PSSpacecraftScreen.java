package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.widgets.AltitudeWidget;
import com.nythicalnorm.planetshine.gui.widgets.NavballWidget;
import com.nythicalnorm.planetshine.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Objects;

public class PSSpacecraftScreen extends MouseLookScreen implements ISpacecraftOrbitDataDisplay {
    private final EntityOrbitBody<?> controllingBody;
    private final Options minecraftOptions;

    public PSSpacecraftScreen(Component pTitle, EntityOrbitBody<?> controllingBody, SpacecraftScreenState spacecraftScreenState) {
        super(pTitle);
        this.controllingBody = controllingBody;
        this.minecraftOptions = Minecraft.getInstance().options;
        this.loadState(spacecraftScreenState);
    }

    @Override
    protected void init() {
        super.init();
        minecraftOptions.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraftOptions.hideGui = true;

        this.addRenderableWidget(new TimeWarpWidget(0,0, width, height, Component.empty()));
        this.addRenderableWidget(new NavballWidget(width/2, height, width, height, Component.empty()));
        this.addRenderableWidget(new AltitudeWidget(width/2, 0, width, height, Component.empty()));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean keyPressed = false;

        if (PSKeyBinds.OPEN_SPACECRAFT_HUD_KEY.matches(pKeyCode, pScanCode)) {
            this.onClose();
            keyPressed = true;
        } else if (PSKeyBinds.OPEN_SOLAR_SYSTEM_MAP_KEY.matches(pKeyCode, pScanCode)) {
            PSClient.get().getScreenManager().setSpacecraftScreenState(new SpacecraftScreenState(
                    this.cameraYrot, this.cameraXrot, this.zoomLevel, this.isNonRotView));

            PSClient.get().getScreenManager().openMapScreen();
            keyPressed = true;
        } else if (PSKeyBinds.CHANGE_SPACECRAFT_VIEW_KEY.matches(pKeyCode, pScanCode)) {
            this.isNonRotView = !this.isNonRotView;
            keyPressed = true;
        }

        if (keyPressed) {
            return keyPressed;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
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
    public OrbitalElementsc getOrbitalElements() {
        return this.controllingBody.getOrbitalElements();
    }

    @Override
    public Vector3dc getRelativePosition() {
        return this.controllingBody.getRelativePos();
    }

    @Override
    public void onClose() {
        super.onClose();
        PSClient.get().getScreenManager().setSpacecraftScreenState(new SpacecraftScreenState(this.cameraYrot, this.cameraXrot, this.zoomLevel, this.isNonRotView));
        PSClient.get().getScreenManager().closeSpacecraftScreen();
        super.onClose();
    }

    private void loadState(SpacecraftScreenState spacecraftScreenState) {
        if (spacecraftScreenState != null) {
            super.loadRotState(spacecraftScreenState);
            this.isNonRotView = spacecraftScreenState.isNonRotView();
        } else {
            this.zoomLevel = 1.1f;
            this.cameraYrot = (float) -Math.toRadians(Minecraft.getInstance().player.getYRot());
            this.cameraXrot = (float) -Math.toRadians(Minecraft.getInstance().player.getXRot());
        }
    }

    public static class SpacecraftScreenState extends MouseLookScreen.MouseLookScreenState {
        private final boolean isNonRotView;

        public SpacecraftScreenState(float cameraYrot, float cameraXrot, float zoomLevel, boolean isNonRotView) {
            super(cameraYrot, cameraXrot, zoomLevel);
            this.isNonRotView = isNonRotView;
        }

        public boolean isNonRotView() {
            return isNonRotView;
        }
    }
}
