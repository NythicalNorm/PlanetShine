package com.nythicalnorm.planetshine.mixin.vs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 2000)
public class VSGameRendererMixinMixin {
    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.renderer.MixinGameRenderer", name = "setupCameraWithMountedShip")
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At(value = "HEAD"), cancellable = true,
            require = 0)
    public void cancelCameraSetup(LevelRenderer instance, PoseStack ignore, Vec3 vec3, Matrix4f matrix4f, Operation<Void> prepareCullFrustum, float partialTicks, long finishTimeNano, PoseStack matrixStack, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera() && spacecraftScreen.isNonRotView()) {
            ci.cancel();
        }
    }
}
