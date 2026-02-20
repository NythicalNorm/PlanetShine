package com.nythicalnorm.planetshine.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    private VertexBuffer skyBuffer;

    @Shadow
    private boolean doesMobEffectBlockSky(Camera pCamera) {
        return false;
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void NSPrenderSky(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup, CallbackInfo ci) {
        LevelRenderer levelRenderer = (LevelRenderer) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        //long beforeTimes = Util.getNanos();
        PSClient css = PSClient.get();

        if (mc.level == null || css == null) {
            return;
        }
        if (css.doRender()) {
            pSkyFogSetup.run();
            if (!pIsFoggy) {
                FogType fogtype = pCamera.getFluidInCamera();
                if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(pCamera)) {
                    PSRenderer.renderSkybox(mc, levelRenderer, pPoseStack, pPartialTick, pCamera, skyBuffer, css);
                }
            }
            ci.cancel();
        }
        //long diff = Util.getNanos() - beforeTimes;
        //PlanetShine.log("PSRenderer Time: " + diff);
    }

    @ModifyVariable(method = "renderClouds", at = @At("LOAD"), ordinal = 0, argsOnly = true)
    private double changeCloudSpeed(double value) {
        PSClient psClient = PSClient.get();
        if (psClient != null) {
            if (psClient.doRender()) {
                return TimeCalc.TimePerMilliTickToTick(psClient.getCurrentTime()) * 0.03F;
            }
        }

        return value;
    }
}
