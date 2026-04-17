package com.nythicalnorm.planetshine.gui;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.*;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@OnlyIn(Dist.CLIENT)
public class PSScreenManager {
    private boolean isMapScreenOpen = false;
    private boolean isSpacecraftScreenOpen = false;
    private MapSolarSystemScreen.MapState mapState = null;
    private PSSpacecraftScreen.SpacecraftScreenState spacecraftScreenState = null;

    public MapSolarSystemScreen.MapState getMapState() {
        return mapState;
    }

    public void setMapState(MapSolarSystemScreen.MapState mapState) {
        this.mapState = mapState;
    }

    public void setSpacecraftScreenState(PSSpacecraftScreen.SpacecraftScreenState spacecraftScreenState) {
        this.spacecraftScreenState = spacecraftScreenState;
    }

    public void playerChangeDimension() {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen mouseLookScreen) {
            mouseLookScreen.onClose();
        }
    }

    public boolean isNotDrawPlanetShine() {
        return Minecraft.getInstance().screen instanceof MapSolarSystemScreen;
    }

    public void updateScreenState() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DeathScreen) {
            if (isMapScreenOpen) {
                closeMapScreen();
            }
            if (isSpacecraftScreenOpen) {
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
        return isSpacecraftScreenOpen;
    }

    public void closeSpacecraftScreen() {
        Options minecraftOptions = Minecraft.getInstance().options;
        minecraftOptions.hideGui = false;
        minecraftOptions.setCameraType(CameraType.FIRST_PERSON);
        isSpacecraftScreenOpen = false;
    }

    public void openMapScreen() {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof ISpacecraftOrbitDataDisplay || currentScreen instanceof ISpacecraftControlStateDisplay) {
            Minecraft.getInstance().setScreen(new MapSolarSystemScreen(isSpacecraftScreenOpen, mapState, currentScreen));
        } else {
            Minecraft.getInstance().setScreen(new MapSolarSystemScreen(isSpacecraftScreenOpen, mapState));
        }
        this.isMapScreenOpen = true;
    }

    public void openSpaceHUDScreen(PSClient psClient) {
        if (VSGameUtilsKt.getShipMountedTo(Minecraft.getInstance().player) instanceof ClientShip) {
            EntityOrbitBody<?> entityOrbitBody = psClient.getControllingBody();
            if (entityOrbitBody != null) {
                Minecraft.getInstance().setScreen(new PSSpacecraftScreen(Component.empty(), entityOrbitBody, spacecraftScreenState));
                this.isSpacecraftScreenOpen = true;
            }
        }
    }

    public @Nullable Screen getSpacecraftScreen() {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen mouseLookScreen) {
            return mouseLookScreen.getSpacecraftScreen();
        } else {
            return null;
        }
    }
}
