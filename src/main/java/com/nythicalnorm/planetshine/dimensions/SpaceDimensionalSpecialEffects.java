package com.nythicalnorm.planetshine.dimensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SpaceDimensionalSpecialEffects extends DimensionSpecialEffects {
    public SpaceDimensionalSpecialEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 pFogColor, float pBrightness) {
        return pFogColor.scale(0.15F);
    }

    @Override
    public boolean isFoggyAt(int pX, int pY) {
        return false;
    }

    @Override
    public float @Nullable [] getSunriseColor(float pTimeOfDay, float pPartialTicks) {
        return null;
    }

    @Override
    public boolean renderClouds(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack,
                                double camX, double camY, double camZ, @NotNull Matrix4f projectionMatrix) {
        return false;
    }

    @Override
    public boolean renderSky(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack,
                             @NotNull Camera camera, @NotNull Matrix4f projectionMatrix, boolean isFoggy, @NotNull Runnable setupFog) {
        return false;
    }

    @Override
    public boolean renderSnowAndRain(@NotNull ClientLevel level, int ticks, float partialTick,
                                     @NotNull LightTexture lightTexture, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public boolean tickRain(@NotNull ClientLevel level, int ticks, @NotNull Camera camera) {
        return false;
    }
}
