package com.nythicalnorm.planetshine.gui;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.*;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@OnlyIn(Dist.CLIENT)
public class PSScreenManager {
    private boolean isMapScreenOpen = false;
    private boolean isSpacecraftScreenOpen = false;
    private MapSolarSystemScreen.MapState mapState = null;
    private PSSpacecraftScreen.SpacecraftScreenState spacecraftScreenState = null;
    private boolean waitingForReopening = false;
    private final PSClient psClient;
    private SpacecraftOrbitalState currentOrbitalState = SpacecraftOrbitalState.ON_PLANET;
    private PSSpacecraftScreen.FacingDirection prevDimensionFacingDirection = null;

    public PSScreenManager(PSClient psClient) {
        this.psClient = psClient;
    }

    public MapSolarSystemScreen.MapState getMapState() {
        return mapState;
    }

    public void setMapState(MapSolarSystemScreen.MapState mapState) {
        this.mapState = mapState;
    }

    public void setSpacecraftScreenState(PSSpacecraftScreen.SpacecraftScreenState spacecraftScreenState) {
        this.spacecraftScreenState = spacecraftScreenState;
    }

    public PSSpacecraftScreen.SpacecraftScreenState getSpacecraftScreenState() {
        return spacecraftScreenState;
    }

    public void prepareForDimensionChange() {
        PSSpacecraftScreen spacecraftScreen = this.getSpacecraftScreen();

        if (spacecraftScreen != null) {
            spacecraftScreen.saveScreenState();
            this.waitingForReopening = true;
        }

        if (Minecraft.getInstance().screen instanceof MapSolarSystemScreen mapSolarSystemScreen) {
            mapSolarSystemScreen.saveScreenState();
            this.waitingForReopening = true;
        }
    }

    public boolean isNotDrawPlanetShine() {
        return Minecraft.getInstance().screen instanceof MapSolarSystemScreen;
    }

    public void updateScreenState() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DeathScreen) {
            if (this.isMapScreenOpen) {
                closeMapScreen();
            }
            if (this.isSpacecraftScreenOpen) {
                closeSpacecraftScreen();
            }
        } else if (this.isSpacecraftScreenOpen && Minecraft.getInstance().player != null &&
                VSGameUtilsKt.getShipMountedTo(Minecraft.getInstance().player) != null) {
            if (waitingForReopening) {
                this.openPSSpacecraftScreen(false);
                if (isMapScreenOpen) {
                    this.openMapScreen();
                }
                this.waitingForReopening = false;
            }
            this.updateOrbitalState(psClient.getPlayerOrbit());
        }
    }

    private void updateOrbitalState(@NotNull ClientPlayerOrbitBody playerOrbit) {
        SpacecraftOrbitalState newOrbitState;
        if (playerOrbit.getOrbitalElements() == null || playerOrbit.getParent() == null) {
            return;
        }

        if (psClient.isOnPlanet()) {
            newOrbitState = SpacecraftOrbitalState.ON_PLANET;
        } else if (((playerOrbit.getOrbitalElements().getPeriapsis() - playerOrbit.getParent().getRadius())
                > playerOrbit.getParent().getAtmosphere().getSafeAltitude()) || playerOrbit.getOrbitalElements().isHyperbolic()) {
            newOrbitState = SpacecraftOrbitalState.IN_ORBIT;
        } else {
            newOrbitState = SpacecraftOrbitalState.SUB_ORBITAL;
        }

        if (currentOrbitalState != newOrbitState) {
             PSSpacecraftScreen spacecraftScreen = this.getSpacecraftScreen();
            if (spacecraftScreen != null) {
                spacecraftScreen.orbitModeChanged(newOrbitState);
                this.currentOrbitalState = newOrbitState;
            }
        }
    }

    public void closeMapScreen() {
        psClient.getMapRenderer().setScreen(null);
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

    public void openPSSpacecraftScreen(boolean newOpening) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && VSGameUtilsKt.getShipMountedTo(player) instanceof ClientShip) {
            EntityOrbitBody<?> entityOrbitBody = psClient.getControllingBody();
            float yaw = player.getViewYRot(0.0f);
            if (entityOrbitBody != null) {
                PSSpacecraftScreen.FacingDirection facingDirection;
                if (this.prevDimensionFacingDirection != null && !newOpening) {
                    facingDirection = this.prevDimensionFacingDirection;
                    player.setYRot(facingDirection.getAngle());
                } else {
                    facingDirection = PSSpacecraftScreen.FacingDirection.fromYaw(yaw);
                    this.prevDimensionFacingDirection = facingDirection;
                }

                PSSpacecraftScreen spacecraftScreen = new PSSpacecraftScreen(Component.empty(), entityOrbitBody, this,
                        facingDirection);
                Minecraft.getInstance().setScreen(spacecraftScreen);
                this.isSpacecraftScreenOpen = true;
            }
        }
    }

    public @Nullable PSSpacecraftScreen getSpacecraftScreen() {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen mouseLookScreen) {
            return mouseLookScreen.getSpacecraftScreen();
        } else {
            return null;
        }
    }

    public enum SpacecraftOrbitalState {
        ON_PLANET,
        SUB_ORBITAL,
        IN_ORBIT
    }
}
