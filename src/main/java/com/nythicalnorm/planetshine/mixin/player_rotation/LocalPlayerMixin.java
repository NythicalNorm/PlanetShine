package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    public float changeXRot(LocalPlayer instance, Operation<Float> original) {
        if (this instanceof SpaceRotationAccessor spaceRotator && spaceRotator.planetShine$canRotateRoll()) {
            return spaceRotator.planetshine$getAdjustedX();
        }

        return original.call(instance);
    }

    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    public float changeYRot(LocalPlayer instance, Operation<Float> original) {
        if (this instanceof SpaceRotationAccessor spaceRotator && spaceRotator.planetShine$canRotateRoll()) {
            return spaceRotator.planetshine$getAdjustedY();
        }

        return original.call(instance);
    }
}
