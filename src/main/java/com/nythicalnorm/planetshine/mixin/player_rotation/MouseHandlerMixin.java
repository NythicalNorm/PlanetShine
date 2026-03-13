package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    public void turnPlayerRot(LocalPlayer instance, double yRot, double xRot, Operation<Void> original) {
        SpaceRotationAccessor spaceRotationAccessor = (SpaceRotationAccessor) instance;

        if (spaceRotationAccessor.planetShine$canRotateRoll() && PSKeyBinds.PLAYER_SPACE_ROTATE_KEY.isDown()) {
            spaceRotationAccessor.planetShine$rotateRoll(Math.toRadians(yRot), instance.getXRot(), instance.getYRot());
        } else {
            original.call(instance, yRot, xRot); // don't know why its showing an error, it compiles
        }
    }
}
