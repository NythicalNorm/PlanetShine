package com.nythicalnorm.planetshine.gui;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.gui.screen.PlayerSpacecraftScreen;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PSScreenManager {
    private boolean isMapScreenOpen = false;
    private PlayerSpacecraftScreen openSpacecraftScreen = null;

    public void setMapScreenOpen(boolean open) {
        this.isMapScreenOpen = open;
    }

    public void setOpenSpacecraftScreen(PlayerSpacecraftScreen opened) {
        this.openSpacecraftScreen = opened;
    }

    public boolean doPlanetShineDraw() {
        updateScreenState();
        return Minecraft.getInstance().screen instanceof MapSolarSystemScreen;
    }

    public void updateScreenState() {
        if (Minecraft.getInstance().screen instanceof DeathScreen) {
            if (isMapScreenOpen) {
                closeMapScreen();
            }
            if (openSpacecraftScreen != null) {
                closeSpacecraftScreen();
            }
        }
    }

    public void closeMapScreen() {
        MapRenderer.setScreen(null);
        isMapScreenOpen = false;
    }

    public boolean isSpacecraftScreenOpen() {
        return openSpacecraftScreen != null;
    }

    public PlayerSpacecraftScreen getSpacecraftScreen() {
        return openSpacecraftScreen;
    }

    public void closeSpacecraftScreen() {
        Options minecraftOptions = Minecraft.getInstance().options;
        minecraftOptions.hideGui = false;
        minecraftOptions.setCameraType(CameraType.FIRST_PERSON);
        openSpacecraftScreen = null;
        PSClient.getInstance().get().setControllingBody(null);
    }
}
