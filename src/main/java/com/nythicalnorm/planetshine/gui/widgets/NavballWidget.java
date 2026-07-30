package com.nythicalnorm.planetshine.gui.widgets;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftControlStateDisplay;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftOrbitDataDisplay;
import com.nythicalnorm.planetshine.gui.screen.PSSpacecraftScreen;
import com.nythicalnorm.planetshine.rendering.generators.QuadSphereModelGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class NavballWidget extends AbstractWidget {
    private static final ResourceLocation NAVBALL_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/navballwidget.png");

    private static final ResourceLocation NAVBALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/navball.png");

    private static final ResourceLocation NAVBALL_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/navball_icons.png");

    private static final Vector2i LEVEL_INDICATION = new Vector2i(0, 0);
    private static final Vector2i PROGRADE = new Vector2i(16, 0);
    private static final Vector2i RETROGRADE = new Vector2i(32, 0);
    private static final Vector2i RADIAL_OUT = new Vector2i(48, 0);
    private static final Vector2i RADIAL_IN = new Vector2i(0, 16);
    private static final Vector2i NORMAL = new Vector2i(16, 16);
    private static final Vector2i ANTI_NORMAL = new Vector2i(32, 16);
    private static final Vector2i MANEUVER = new Vector2i(48, 16);
    private static final Vector2i TARGET_RETROGRADE = new Vector2i(0, 32);
    private static final Vector2i TARGET_PROGRADE = new Vector2i(16, 32);
    private static final float NAVBALL_ICONS_SCALE = 1.0f / 8.0f;

    private static final Quaternionf NAVBALL_INITIAL_ROT = new Quaternionf().rotateYXZ(Mth.HALF_PI, 0f, Mth.HALF_PI);

    private NavBallMode navBallMode;

    public NavballWidget(int pX, int pY, Component pMessage) {
        super(pX - 47, pY - 86, 94, 86, pMessage);
        this.initNavBallMode();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int xPos = getX();
        int yPos = getY();
        PSSpacecraftScreen spacecraftScreen = PSClient.get().getScreenManager().getSpacecraftScreen();

        if (spacecraftScreen != null) {
            Quaternionf invertedSpacecraftRot = new Quaternionf().set(spacecraftScreen.getSpacecraftRotation()).invert();

            this.renderNavBall(spacecraftScreen, pGuiGraphics, invertedSpacecraftRot);

            pGuiGraphics.blit(NAVBALL_GUI_TEXTURE, xPos, yPos, 0, 0, 94, 86);
            this.renderNavballIcons(pGuiGraphics, spacecraftScreen, xPos, yPos, invertedSpacecraftRot);

            this.renderRelativeVelocity(pGuiGraphics, xPos, yPos,(int) spacecraftScreen.getRelativeVelocity().length());
            this.renderGForceBar(pGuiGraphics, xPos, yPos);
        }

        if (spacecraftScreen instanceof ISpacecraftControlStateDisplay spacecraftDataDisplay) {
            renderThrottleBar(pGuiGraphics, xPos, yPos, spacecraftDataDisplay);
            renderButtons(pGuiGraphics, xPos, yPos, spacecraftDataDisplay);
        }
    }

    private void renderNavballIcons(@NotNull GuiGraphics graphics, ISpacecraftOrbitDataDisplay orbitData,
                                    int xPos, int yPos, Quaternionf invertedSpacecraftRot) {
        graphics.pose().pushPose();
        graphics.pose().translate(xPos + 47, yPos + 51, 0.0f);
        graphics.pose().scale(NAVBALL_ICONS_SCALE, NAVBALL_ICONS_SCALE, NAVBALL_ICONS_SCALE);

        Quaternionf spacecraftRotation = new Quaternionf().rotationX(Mth.HALF_PI);
        spacecraftRotation.mul(invertedSpacecraftRot);

        Vector3dc relativePosition = orbitData.getRelativePosition();
        boolean isInSpaceDim = PSClient.get().weInSpaceDim();

        if (!isInSpaceDim) {
            relativePosition = new Vector3d(0.0d, 1.0d, 0.0d);
        }

        // Normal
        Vector3f normalOriginal = new Vector3f().set(orbitData.getRelativeVelocity()).cross(
                (float) -relativePosition.x(),
                (float) -relativePosition.y(),
                (float) -relativePosition.z()
                , new Vector3f());
        normalOriginal.normalize();
        this.vectorToIcon(graphics, new Vector3f(normalOriginal), spacecraftRotation, NORMAL);

        // Anti-Normal
        Vector3f antiNormal = normalOriginal.negate();
        this.vectorToIcon(graphics, antiNormal, spacecraftRotation, ANTI_NORMAL);

        if (this.navBallMode != NavBallMode.SURFACE) {
            // Radial OUT
            this.vectorToIcon(graphics, new Vector3f().set(relativePosition), spacecraftRotation, RADIAL_OUT);

            // Radial IN
            this.vectorToIcon(graphics, new Vector3f().set(relativePosition).negate(), spacecraftRotation, RADIAL_IN);
        }

        // Prograde
        this.vectorToIcon(graphics, new Vector3f().set(orbitData.getRelativeVelocity()), spacecraftRotation, PROGRADE);

        // Retrograde
        this.vectorToIcon(graphics, new Vector3f().set(orbitData.getRelativeVelocity()).negate(), spacecraftRotation, RETROGRADE);

        //Level Indication
        this.drawIcon(graphics, LEVEL_INDICATION, new Vector3f());
        graphics.pose().popPose();
    }

    private void vectorToIcon(GuiGraphics graphics,Vector3f vector, Quaternionf rotation, Vector2i texturePos) {
        vector.normalize();
        vector.rotate(rotation);
        this.drawIcon(graphics, texturePos, vector);
    }

    private void drawIcon(GuiGraphics graphics, Vector2i icon, Vector3fc pos) {
        if (pos.z() < 0.0f) {
            return;
        }

        int navballMult = (int) (0.5f / NAVBALL_ICONS_SCALE);
        int centerDist = Math.round(256f * NAVBALL_ICONS_SCALE);
        int xPos = -Math.round(pos.y() * navballMult * 62f);
        int yPos = -Math.round(pos.x() * navballMult * 62f);

        graphics.blit(
                NAVBALL_ICONS_TEXTURE,
                xPos - centerDist, yPos - centerDist,
                icon.x() * navballMult, icon.y() * navballMult,
                64, 64
        );
    }

    private void renderNavBall(ISpacecraftOrbitDataDisplay orbitData, GuiGraphics pGuiGraphics, Quaternionf invertedSpacecraftRot) {
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();

        PoseStack navballPosestack = new PoseStack();
        Window gameWindow =  Minecraft.getInstance().getWindow();

        // 70f is the pixel distance from the center of the sphere's place to the bottom
        float Yheight = 70f/gameWindow.getGuiScaledHeight();

        float aspectRatio = (float) pGuiGraphics.guiWidth() / pGuiGraphics.guiHeight();
        float Orthosize = 1f;
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(-Orthosize*aspectRatio, Orthosize*aspectRatio, -Orthosize, Orthosize, 0.001f, 10.0f);

        navballPosestack.translate(0f,-1f+Yheight,-1f);

        float navballScale = (float) gameWindow.getGuiScale() * (124f/gameWindow.getHeight());
        navballPosestack.scale(navballScale, navballScale, navballScale);
        Quaterniondc rotation = orbitData.getSpacecraftRotation();

        if (rotation != null) {
            navballPosestack.mulPose(NAVBALL_INITIAL_ROT);
            navballPosestack.mulPose(invertedSpacecraftRot);

            if (PSClient.get().weInSpaceDim() && this.navBallMode == NavBallMode.SURFACE) {
//                Vector3d playerRelativePos = new Vector3d(PSClient.get().getPlayerOrbit().getRelativePos());
//                Vector3f normalOriginal = new Vector3f().set(orbitData.getRelativeVelocity()).cross(
//                        (float) -playerRelativePos.x(),
//                        (float) -playerRelativePos.y(),
//                        (float) -playerRelativePos.z()
//                        , new Vector3f());
//                normalOriginal.normalize();
//
//                playerRelativePos.normalize();
//                Quaternionf spaceToSurfaceRot = new Quaternionf().lookAlong(normalOriginal.x(), normalOriginal.y(), normalOriginal.z(),
//                        (float) playerRelativePos.x, (float) playerRelativePos.y, (float) playerRelativePos.z);
                navballPosestack.mulPose(new Quaternionf(PSClient.get().getPlayerOrbit().getSurfaceDownRot()));
            }
        }

        QuadSphereModelGenerator.getSphereBuffer().bind();
        RenderSystem.setShaderTexture(0, NAVBALL_TEXTURE);
        ShaderInstance shad = GameRenderer.getPositionTexShader();

        QuadSphereModelGenerator.getSphereBuffer().drawWithShader(navballPosestack.last().pose(), projectionMatrix, shad);
        VertexBuffer.unbind();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
    }

    private void renderButtons(GuiGraphics pGuiGraphics, int xPos, int yPos, ISpacecraftControlStateDisplay spacecraftScreen) {
        if (spacecraftScreen.isRCS()) {
            pGuiGraphics.blit(NAVBALL_GUI_TEXTURE, xPos + 17, yPos + 18, 96, 0, 12, 6);
        }
        if (spacecraftScreen.isSAS()) {
            pGuiGraphics.blit(NAVBALL_GUI_TEXTURE, xPos + 65, yPos + 18,96,8, 12, 6);
        }
    }

    private void renderRelativeVelocity(GuiGraphics pGuiGraphics, int xPos, int yPos, int speed) {
        Component orbitalSpeedComp = Component.translatable("planetshine.screen.orbital_speed", speed);
        pGuiGraphics.drawString(Minecraft.getInstance().font, orbitalSpeedComp,xPos + 22, yPos + 5, 0x00ff2b, false);
    }

    private void renderThrottleBar(GuiGraphics graphics, int xPos, int yPos, ISpacecraftControlStateDisplay spacecraftScreen) {
        int barHeight = Math.round(Mth.lerp(spacecraftScreen.getThrottleSetting(), 0, 70));
        int pointerHeight = Mth.clamp(barHeight,1, 67);
        //drawing the blue bar
        graphics.blit(NAVBALL_GUI_TEXTURE, xPos + 3, yPos + 72 - barHeight,95,105 - barHeight,9, barHeight);
        //drawing the arrow
        graphics.blit(NAVBALL_GUI_TEXTURE, xPos - 3, yPos + 66 - pointerHeight,95,23,14,12);
    }

    private void renderGForceBar(GuiGraphics graphics, int xPos, int yPos) {
        double acceleration = 0d; // (Math.abs(this.velocityLastTick - velocity)) * (TimeCalc.PhysTickPerSec / OrbitalCalc.ACCELERATION_DUE_TO_GRAVITY_EARTH);
        int barHeight = (int) Math.round(acceleration * 14);
        graphics.blit(NAVBALL_GUI_TEXTURE, xPos + 80, yPos + 72 - barHeight,110,72 - barHeight,13, barHeight);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (this.navBallMode == NavBallMode.SURFACE) {
            this.navBallMode = NavBallMode.ORBIT;
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("planetshine.ui.navball_mode_set", "ORBIT"), true);
        } else if (this.navBallMode == NavBallMode.ORBIT) {
            this.navBallMode = NavBallMode.SURFACE;
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("planetshine.ui.navball_mode_set", "SURFACE"), true);
        }
    }

    private void initNavBallMode() {
        if (PSClient.get().weInSpaceDim() && !PSClient.get().isInsideAtmosphereInSpaceDim()) {
            this.navBallMode = NavBallMode.ORBIT;
        } else {
            this.navBallMode = NavBallMode.SURFACE;
        }
    }

    public NavBallMode getNavBallMode() {
        return navBallMode;
    }

    public void setNavBallMode(NavBallMode navBallMode) {
        this.navBallMode = navBallMode;
    }

    public enum NavBallMode {
        SURFACE,
        ORBIT,
        TARGET
    }
}
