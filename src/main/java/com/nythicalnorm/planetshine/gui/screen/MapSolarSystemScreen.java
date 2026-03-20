package com.nythicalnorm.planetshine.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.PSScreenManager;
import com.nythicalnorm.planetshine.gui.widgets.AltitudeWidget;
import com.nythicalnorm.planetshine.gui.widgets.NavballWidget;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class MapSolarSystemScreen extends MouseLookScreen {
    private PSScreenManager screenManager;
    private OrbitalBody[] FocusableBodies;
    private int currentFocusedBodyIndex;
    private final boolean isSpacecraftScreenOpen;
    private MapRenderer mapRenderer;
    protected double radiusZoomLevel = 0f;
    private @Nullable Screen prevScreen;

    public MapSolarSystemScreen(boolean PisSpacecraftScreenOpen) {
        super(Component.empty());
        PSClient.getInstance().ifPresent (psClient -> {
            this.screenManager = psClient.getScreenManager();
            this.mapRenderer = psClient.getMapRenderer();
        });
        this.isSpacecraftScreenOpen = PisSpacecraftScreenOpen;
    }

    public MapSolarSystemScreen(boolean PisSpacecraftScreenOpen, MapState mapState) {
        this(PisSpacecraftScreenOpen);
        this.populateFocusedBodiesList();

        if (mapState != null) {
            loadFromMapState(mapState);
        } else {
            setInitialCameraAngle();
        }
    }

    public MapSolarSystemScreen(boolean isSpacecraftScreenOpen, MapState mapState, @Nullable Screen currentScreen) {
        this(isSpacecraftScreenOpen, mapState);
        this.prevScreen = currentScreen;
    }

    @Override
    protected void init() {
        mapRenderer.setScreen(this);
        this.addRenderableWidget(new TimeWarpWidget(0,0, width, height, Component.empty()));
        if (isSpacecraftScreenOpen) {
            this.addRenderableWidget(new NavballWidget(width/2, height, width, height, Component.empty()));
            // this.addRenderableWidget(new LeftPanelWidget(0, height, width, height, Component.empty()));
            this.addRenderableWidget(new AltitudeWidget(width/2, 0, width, height, Component.empty()));
        }
        super.init();
    }

    protected void loadFromMapState(MapState mapState) {
        super.loadRotState(mapState);
        for (int i = 0; i < FocusableBodies.length; i++) {
            if (FocusableBodies[i].getOrbitId().equals(mapState.getFocusedBody())) {
                changeFocusBody(i);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.depthMask(false);

        PoseStack mapPosestack = new PoseStack();
        Matrix4f projectionMatrix = new Matrix4f().setPerspective(70, (float) graphics.guiWidth()/graphics.guiHeight(), 0.00001f, 10.0f);

        Quaternionf dragCameraRot = new Quaternionf().rotateYXZ(cameraYrot, cameraXrot, 0f); //.mul(yRotQuaternion);
        Vector3d relativeCameraPos = new Vector3d(0d, 0d, zoomLevel * radiusZoomLevel);
        relativeCameraPos.rotate(new Quaterniond(dragCameraRot.x, dragCameraRot.y,dragCameraRot.z,dragCameraRot.w));

        GL11.glEnable(0x864F);
        mapPosestack.pushPose();
        mapPosestack.mulPose(dragCameraRot.conjugate());

        mapRenderer.renderSkybox(mapPosestack, projectionMatrix);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        mapRenderer.renderMapObjects(graphics, mapPosestack, projectionMatrix, relativeCameraPos, FocusableBodies[currentFocusedBodyIndex]);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        mapPosestack.popPose();
        GL11.glDisable(0x864F);

        if (!mapPosestack.clear()) {
            throw new IllegalStateException("popped poses are not closed properly.");
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected float getMaxDistanceZoom() {
        return 1424600000000f/((float) radiusZoomLevel);
    }

    @Override
    public Screen getSpacecraftScreen() {
        if (prevScreen == null) {
            return super.getSpacecraftScreen();
        } else {
            return prevScreen;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (PSKeyBinds.OPEN_SOLAR_SYSTEM_MAP_KEY.matches(pKeyCode, pScanCode)) {
            this.onClose();
            return true;
        }

        if (GLFW.GLFW_KEY_TAB == pKeyCode){
            changeFocusBody(currentFocusedBodyIndex + 1);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (isSpacecraftScreenOpen) {
            screenManager.openSpaceHUDScreen(PSClient.get());
        }
        OrbitalBody currentFocusedBody = FocusableBodies[currentFocusedBodyIndex];
        if (currentFocusedBody != null) {
            screenManager.setMapState(new MapState(this.cameraYrot, this.cameraXrot, this.zoomLevel, currentFocusedBody.getOrbitId()));
        }
        screenManager.closeMapScreen();
    }

    public void changeFocusBody(int newIndex) {
        currentFocusedBodyIndex = newIndex;
        if (currentFocusedBodyIndex >= FocusableBodies.length) {
            currentFocusedBodyIndex = 0;
        }
        if (FocusableBodies[currentFocusedBodyIndex] instanceof CelestialBody celestialBody) {
            radiusZoomLevel = celestialBody.getRadius();
        } else if (FocusableBodies[currentFocusedBodyIndex] instanceof EntityOrbitBody) {
            radiusZoomLevel = 1000000;
        }
        updateMapRenderables();
    }

    public OrbitalBody getFocusedOrbitalBody() {
        return this.FocusableBodies[currentFocusedBodyIndex];
    }

    public void updateMapRenderables() {
        this.mapRenderer.updateMapRenderables(PSClient.get(), FocusableBodies[currentFocusedBodyIndex]);
    }

    private void setInitialCameraAngle() {
        if (FocusableBodies[currentFocusedBodyIndex] instanceof CelestialBody) {
            PSClient psClient = PSClient.get();
            if (psClient.getCurrentPlanet().isPresent()) {
                if (psClient.getCurrentPlanet().get().equals(FocusableBodies[currentFocusedBodyIndex])) {
                    Vector3d playerRelativePos = new Vector3d(psClient.getPlayerOrbit().getRelativePos());
                    playerRelativePos.normalize();
                    cameraYrot = (float) Math.atan2(playerRelativePos.x, playerRelativePos.z);
                    cameraXrot = (float) -Math.asin(playerRelativePos.y);
                }
            }
        }
    }

    private void populateFocusedBodiesList() {
        OrbitalBody currentFocusedBody = null;
        PSClient psClient = PSClient.get();

        if (psClient.isOnPlanet()) {
            currentFocusedBody = psClient.getCurrentPlanet().get();
        } else if (psClient.weInSpaceDim()) {
            currentFocusedBody = psClient.getPlayerOrbit();
        }

        int totalFocusAmount = psClient.getSolarSystem().getAllPlanetaryBodies().size();
        if (currentFocusedBody instanceof EntityOrbitBody) {
            totalFocusAmount += 1;
        }

        //setting the first element to the desired body and later filling in planets that aren't currentfocusedbody
        int index = 0;

        FocusableBodies = new OrbitalBody[totalFocusAmount];
        FocusableBodies[index] = currentFocusedBody;
        currentFocusedBodyIndex = index;

        for (CelestialBody plnt : psClient.getSolarSystem().getAllPlanetaryBodies().values()) {
            if (plnt != currentFocusedBody) {
                index++;
                FocusableBodies[index] = plnt;
            }
        }
        changeFocusBody(0);
    }

    public static class MapState extends MouseLookScreen.MouseLookScreenState {
        private final OrbitId focusedBody;

        public MapState(float cameraYrot, float cameraXrot, float zoomLevel, OrbitId focusedBody) {
            super(cameraYrot, cameraXrot, zoomLevel);
            this.focusedBody = focusedBody;
        }

        public OrbitId getFocusedBody() {
            return focusedBody;
        }
    }
}