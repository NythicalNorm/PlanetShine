package com.nythicalnorm.planetshine.gui.widgets;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftControlStateDisplay;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftOrbitDataDisplay;
import com.nythicalnorm.planetshine.rendering.generators.QuadSphereModelGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
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

    public NavballWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }


    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int xPos = getX() - 47;
        int yPos = getY() - 86;
        Screen spacecraftScreen = PSClient.get().getScreenManager().getSpacecraftScreen();

        if (spacecraftScreen instanceof ISpacecraftOrbitDataDisplay orbitDataDisplay) {
            this.renderNavBall(orbitDataDisplay, pGuiGraphics);
            Vector3dc bodyVelocity = orbitDataDisplay.getVelocityVector();
            this.renderNavballIcons(pGuiGraphics, xPos, yPos);

            pGuiGraphics.blit(NAVBALL_GUI_TEXTURE, xPos, yPos, 0, 0, 94, 86);
            this.renderRelativeVelocity(pGuiGraphics, xPos, yPos,(int) bodyVelocity.length());
            this.renderGForceBar(pGuiGraphics, xPos, yPos);
        }

        if (spacecraftScreen instanceof ISpacecraftControlStateDisplay spacecraftDataDisplay) {
            renderThrottleBar(pGuiGraphics, xPos, yPos, spacecraftDataDisplay);
            renderButtons(pGuiGraphics, xPos, yPos, spacecraftDataDisplay);
        }
    }

    private void renderNavballIcons(@NotNull GuiGraphics pGuiGraphics, int xPos, int yPos) {

    }

    private void renderNavBall(ISpacecraftOrbitDataDisplay orbitData, GuiGraphics pGuiGraphics) {
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
            Quaternionf setupRot = new Quaternionf().rotateYXZ(Mth.HALF_PI, 0f, Mth.HALF_PI);
            navballPosestack.mulPose(setupRot);
            navballPosestack.mulPose(new Quaternionf().set(rotation).invert());
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
}
