package com.nythicalnorm.planetshine.mixin.vs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 2000)
public abstract class VSGameRendererMixinMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double pFov);

    @Shadow
    private float fov;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract double getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);

    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.renderer.MixinGameRenderer", name = "setupCameraWithMountedShip")
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At(value = "HEAD"), cancellable = true,
            require = 0)
    public void cancelCameraSetup(LevelRenderer instance, PoseStack ignore, Vec3 vec3, Matrix4f matrix4f, Operation<Void> prepareCullFrustum, float partialTicks, long finishTimeNano, PoseStack matrixStack, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera() &&
                spacecraftScreen.getViewMode() == MouseLookScreen.ViewMode.NON_ROTATING) {

            ci.cancel();
            prepareCullFrustum.call(instance, matrixStack, mainCamera.getPosition(),
                    this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov().get())));
        }
    }

    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.renderer.MixinGameRenderer", name = "setupCameraWithMountedShip")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "NEW", target = "(Lorg/joml/Quaterniondc;)Lorg/joml/Quaternionf;"))
    private Quaternionf setSurfaceDownMatrix(Quaterniondc source, Operation<Quaternionf> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera() &&
                spacecraftScreen.getViewMode() == MouseLookScreen.ViewMode.SURFACE_DOWN) {
            return original.call(PSClient.get().getPlayerOrbit().getSurfaceDownRot().conjugate(new Quaterniond()));
        } else {
            return original.call(source);
        }
    }

}
