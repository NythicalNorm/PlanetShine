package com.nythicalnorm.planetshine.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.PSScreenManager;
import com.nythicalnorm.planetshine.gui.widgets.AltitudeWidget;
import com.nythicalnorm.planetshine.gui.widgets.NavballWidget;
import com.nythicalnorm.planetshine.rendering.map.MapIconRenderable;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.gui.widgets.TimeWarpWidget;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import com.nythicalnorm.planetshine.util.ProjectionUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class MapSolarSystemScreen extends MouseLookScreen {
    private PSScreenManager screenManager;
    private OrbitalBody[] focusableBodies;
    private int currentFocusedBodyIndex;
    private final boolean isSpacecraftScreenOpen;
    private MapRenderer mapRenderer;
    protected double radiusZoomLevel = 0f;
    private @Nullable Screen prevScreen;

    protected Matrix4f projectionMatrix;
    protected Matrix4f lastCameraModelView;
    protected Vector3f relativeCameraPos;

    public MapSolarSystemScreen(boolean PisSpacecraftScreenOpen, @Nullable MapState mapState) {
        super(Component.empty());
        if (PSClient.get() != null) {
            PSClient psClient = PSClient.get();
            this.screenManager = psClient.getScreenManager();
            this.mapRenderer = psClient.getMapRenderer();
            this.populateFocusedBodiesList(psClient);
        }
        this.isSpacecraftScreenOpen = PisSpacecraftScreenOpen;
        this.projectionMatrix = new Matrix4f();
        this.relativeCameraPos = new Vector3f();

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
        this.addRenderableWidget(new TimeWarpWidget(0,0, Component.empty()));
        if (isSpacecraftScreenOpen) {
            this.addRenderableWidget(new NavballWidget(width/2, height, width, height, Component.empty()));
            // this.addRenderableWidget(new LeftPanelWidget(0, height, width, height, Component.empty()));
            this.addRenderableWidget(new AltitudeWidget(width/2, 0, width, height, Component.empty()));
        }
        super.init();
    }

    protected void loadFromMapState(MapState mapState) {
        super.loadRotState(mapState);
        for (int i = 0; i < focusableBodies.length; i++) {
            if (focusableBodies[i].getOrbitId().equals(mapState.getFocusedBody())) {
                changeFocusBody(i);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.depthMask(false);

        PoseStack mapPosestack = new PoseStack();
        projectionMatrix = projectionMatrix.setPerspective(70, (float) graphics.guiWidth()/graphics.guiHeight(), 1000f, 8e10f);

        Quaternionf dragCameraRot = new Quaternionf().rotateYXZ(cameraYrot, cameraXrot, 0f); //.mul(yRotQuaternion);
        relativeCameraPos.set(0d, 0d, zoomLevel * radiusZoomLevel);
        relativeCameraPos.rotate(new Quaternionf(dragCameraRot.x, dragCameraRot.y, dragCameraRot.z, dragCameraRot.w));

        relativeCameraPos.set(MapRenderer.toMapCoordinate(relativeCameraPos));
        GL11.glEnable(0x864F);
        mapPosestack.pushPose();
        mapPosestack.mulPose(dragCameraRot.conjugate());
        mapRenderer.renderSkybox(mapPosestack, projectionMatrix);

        mapPosestack.translate(-relativeCameraPos.x, -relativeCameraPos.y, -relativeCameraPos.z);
        this.lastCameraModelView = mapPosestack.last().pose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        mapRenderer.renderMapObjects(graphics, mapPosestack, projectionMatrix, focusableBodies[currentFocusedBodyIndex]);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        mapPosestack.popPose();
        GL11.glDisable(0x864F);

        if (!mapPosestack.clear()) {
            throw new IllegalStateException("popped poses are not closed properly.");
        }

        this.renderToolTip(graphics, mouseX, mouseY);
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
    protected boolean mouseLeftDoubleClicked(double pMouseX, double pMouseY) {
        // check for all the icons
        OrbitalBody orbitalBody = this.findEntityOrbitBodyHoveringOver((int) pMouseX, (int) pMouseY);

        if (orbitalBody != null) {
            this.setFocusBody(orbitalBody);
            return true;
        }

        // raycast for checking celestial bodies themselves
        Vector3d rayDir = ProjectionUtils.screenToWorldRay((float) pMouseX, (float) pMouseY, width, height, projectionMatrix, lastCameraModelView);
        Vector3d cameraPos = new Vector3d(relativeCameraPos);
        OrbitalBody body = this.getFocusedOrbitalBody();

        rayDir.normalize();
        cameraPos = cameraPos.add(body.getAbsolutePos());

        CelestialBody celestialBody = ProjectionUtils.raycastPlanets(cameraPos, rayDir, PSClient.get().getSolarSystem());
        if (celestialBody != null) {
            this.setFocusBody(celestialBody);
            return true;
        }

        return false;
    }

    private void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHoveringOver(mouseX, mouseY, PSClient.get().getPlayerOrbit())) {
            renderSpacecraftTooltip(guiGraphics, mouseX, mouseY, PSClient.get().getPlayerOrbit());
            return;
        }

        OrbitalBody hoverOverBody = findEntityOrbitBodyHoveringOver(mouseX, mouseY);
        if (hoverOverBody != null) {
            renderSpacecraftTooltip(guiGraphics, mouseX, mouseY, hoverOverBody);
        }
    }

    private void renderSpacecraftTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, OrbitalBody entityOrbitBody) {
        Component displayName = entityOrbitBody.getDisplayName();
        if (displayName != null) {
            guiGraphics.renderTooltip(this.font, displayName, mouseX, mouseY);
        }
    }

    private @Nullable OrbitalBody findEntityOrbitBodyHoveringOver(int mouseX, int mouseY) {
        for (OrbitalBody orbitalBody : this.focusableBodies) {
            if (isHoveringOver(mouseX, mouseY, orbitalBody)) {
                return orbitalBody;
            }
        }

        return null;
    }

    private boolean isHoveringOver(int mouseX, int mouseY, OrbitalBody entityOrbitBody) {
        if (entityOrbitBody instanceof MapIconRenderable mapIconRenderable && mapIconRenderable.shouldDraw()) {
            Vector2ic iconPos = mapIconRenderable.getLatestMapPos();
            if (iconPos == null) {
                return false;
            }

            int xDiff = Math.abs(mouseX - iconPos.x());
            int yDiff = Math.abs(mouseY - iconPos.y());
            return xDiff < 6 && yDiff < 6;
        }

        return false;
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
        OrbitalBody currentFocusedBody = focusableBodies[currentFocusedBodyIndex];
        if (currentFocusedBody != null) {
            screenManager.setMapState(new MapState(this.cameraYrot, this.cameraXrot, this.zoomLevel, currentFocusedBody.getOrbitId()));
        }
        screenManager.closeMapScreen();
    }

    protected void setFocusBody(OrbitalBody body) {
        for (int i = 0; i < focusableBodies.length; i++) {
            if (body.equals(focusableBodies[i])) {
                changeFocusBody(i);
                return;
            }
        }
    }

    public void changeFocusBody(int newIndex) {
        currentFocusedBodyIndex = newIndex;
        if (currentFocusedBodyIndex >= focusableBodies.length) {
            currentFocusedBodyIndex = 0;
        }
        if (focusableBodies[currentFocusedBodyIndex] instanceof CelestialBody celestialBody) {
            radiusZoomLevel = celestialBody.getRadius();
        } else if (focusableBodies[currentFocusedBodyIndex] instanceof EntityOrbitBody) {
            radiusZoomLevel = 1000000;
        }
        updateMapRenderables();
    }

    public OrbitalBody getFocusedOrbitalBody() {
        return this.focusableBodies[currentFocusedBodyIndex];
    }

    public void updateMapRenderables() {
        this.mapRenderer.updateMapRenderables(PSClient.get(), focusableBodies[currentFocusedBodyIndex]);
    }

    private void setInitialCameraAngle() {
        if (focusableBodies[currentFocusedBodyIndex] instanceof CelestialBody) {
            PSClient psClient = PSClient.get();
            if (psClient.getCurrentPlanet().isPresent()) {
                if (psClient.getCurrentPlanet().get().equals(focusableBodies[currentFocusedBodyIndex])) {
                    Vector3d playerRelativePos = new Vector3d(psClient.getPlayerOrbit().getRelativePos());
                    playerRelativePos.normalize();
                    cameraYrot = (float) Math.atan2(playerRelativePos.x, playerRelativePos.z);
                    cameraXrot = (float) -Math.asin(playerRelativePos.y);
                }
            }
        }
    }

    private void populateFocusedBodiesList(PSClient psClient) {
        int index = 0;
        focusableBodies = new OrbitalBody[psClient.getSolarSystem().getAllPlanetaryBodies().size() +
                psClient.getSolarSystem().getAllEntitiesOrbitsList().size()];

        for (CelestialBody plnt : psClient.getSolarSystem().getAllPlanetaryBodies().values()) {
            focusableBodies[index] = plnt;
            index++;
        }

        for (EntityOrbitBody<?> entityOrbitBody : psClient.getSolarSystem().getAllSpacecraftBodies().values()) {
            focusableBodies[index] = entityOrbitBody;
            index++;
        }

        if (psClient.isOnPlanet()) {
            this.setFocusBody(psClient.getCurrentPlanet().get());
        } else if (psClient.weInSpaceDim()) {
            this.setFocusBody(psClient.getPlayerOrbit());
        }
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