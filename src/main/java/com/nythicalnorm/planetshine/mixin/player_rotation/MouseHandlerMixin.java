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
    public void turnPlayerRot(LocalPlayer instance, double yMove, double xMove, Operation<Void> original) {
        SpaceRotationAccessor spaceRotationAccessor = (SpaceRotationAccessor) instance;

        if (spaceRotationAccessor.planetShine$canRotateRoll()) {
            if (PSKeyBinds.PLAYER_SPACE_ROTATE_KEY.isDown()){
                spaceRotationAccessor.planetShine$rotateRoll(Math.toRadians(yMove));
            } else{
                spaceRotationAccessor.planetshine$updateAdjustedRots(yMove, xMove);
            }
            spaceRotationAccessor.setEntityMCRots(instance, yMove, xMove);
        } else {
            original.call(instance, yMove, xMove); // don't know why its showing an error, it compiles
        }
    }
}
