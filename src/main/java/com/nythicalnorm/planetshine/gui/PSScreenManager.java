package com.nythicalnorm.planetshine.gui;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.gui.screen.PSSpacecraftScreen;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@OnlyIn(Dist.CLIENT)
public class PSScreenManager {
    private boolean isMapScreenOpen = false;
    private Screen openSpacecraftScreen = null;
    private MapSolarSystemScreen.MapState mapState = null;

    public void setMapScreenOpen(boolean open) {
        this.isMapScreenOpen = open;
    }

    public MapSolarSystemScreen.MapState getMapState() {
        return mapState;
    }

    public void setMapState(MapSolarSystemScreen.MapState mapState) {
        this.mapState = mapState;
    }

    public void resetMapState() {
        this.mapState = null;
    }

    public void setOpenSpacecraftScreen(Screen opened) {
        this.openSpacecraftScreen = opened;
    }

    public boolean isNotDrawPlanetShine() {
        updateScreenState();
        return Minecraft.getInstance().screen instanceof MapSolarSystemScreen;
    }

    public void updateScreenState() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DeathScreen) {
            if (isMapScreenOpen) {
                closeMapScreen();
            }
            if (openSpacecraftScreen != null) {
                closeSpacecraftScreen();
            }
        } else if (screen instanceof PSSpacecraftScreen &&
                VSGameUtilsKt.getShipMountedTo(Minecraft.getInstance().player) == null) {
            screen.onClose();
        }
    }

    public void closeMapScreen() {
        PSClient.get().getMapRenderer().setScreen(null);
        isMapScreenOpen = false;
    }

    public boolean isMapScreenOpen() {
        return isMapScreenOpen;
    }

    public boolean isSpacecraftScreenOpen() {
        return openSpacecraftScreen != null;
    }

    public Screen getSpacecraftScreen() {
        return openSpacecraftScreen;
    }

    public void closeSpacecraftScreen() {
        Options minecraftOptions = Minecraft.getInstance().options;
        minecraftOptions.hideGui = false;
        minecraftOptions.setCameraType(CameraType.FIRST_PERSON);
        openSpacecraftScreen = null;
    }

    public void openSpaceHUDScreen(PSClient psClient) {
        if (VSGameUtilsKt.getShipMountedTo(Minecraft.getInstance().player) instanceof ClientShip) {
            EntityOrbitBody entityOrbitBody = psClient.getControllingBody();
            if (entityOrbitBody != null) {
                Minecraft.getInstance().setScreen(new PSSpacecraftScreen(Component.empty(), entityOrbitBody));
            }
        }
    }
}
