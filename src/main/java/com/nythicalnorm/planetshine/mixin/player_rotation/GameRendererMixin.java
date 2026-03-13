package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "NEW", target = "(Lorg/joml/Matrix3fc;)Lorg/joml/Matrix3f;"))
    public void changeCameraRotation(float pPartialTicks, long pFinishTimeNano, PoseStack pPoseStack, CallbackInfo ci) {
        if  (mainCamera.getEntity() instanceof SpaceRotationAccessor spaceRotationAccessor &&
                spaceRotationAccessor.planetShine$canRotateRoll()) {
            pPoseStack.mulPose(spaceRotationAccessor.planetShine$getSpaceRotationOffset());
        }
    }
}
