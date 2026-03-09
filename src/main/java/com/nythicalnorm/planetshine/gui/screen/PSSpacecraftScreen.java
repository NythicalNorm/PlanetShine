package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.widgets.AltitudeWidget;
import com.nythicalnorm.planetshine.gui.widgets.NavballWidget;
import com.nythicalnorm.planetshine.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.joml.Quaterniondc;

public class PSSpacecraftScreen extends MouseLookScreen implements ISpacecraftOrbitDataDisplay {
    private final EntityOrbitBody controllingBody;
    private final Options minecraftOptions;

    public PSSpacecraftScreen(Component pTitle, EntityOrbitBody controllingBody) {
        super(pTitle);
        this.controllingBody = controllingBody;
        this.minecraftOptions = Minecraft.getInstance().options;
    }

    @Override
    protected void init() {
        super.init();
        minecraftOptions.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraftOptions.hideGui = true;
        zoomLevel = 1.1f;

        this.cameraYrot = (float) -Math.toRadians(Minecraft.getInstance().player.getYRot());
        this.cameraXrot = (float) -Math.toRadians(Minecraft.getInstance().player.getXRot());

        PSClient.get().getScreenManager().setOpenSpacecraftScreen(this);
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
            Minecraft.getInstance().setScreen(new MapSolarSystemScreen(true));
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
    public double getVelocity() {
        return controllingBody.getRelativeVelocity().length();
    }

    @Override
    public double getAltitude() {
        return controllingBody.getAltitude();
    }

    @Override
    public void onClose() {
        super.onClose();
        PSClient.get().getScreenManager().closeSpacecraftScreen();
        super.onClose();
    }
}
