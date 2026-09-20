package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"))
    public float getViewXRot(Entity instance, float pPartialTicks, Operation<Float> original) {
        if (instance instanceof SpaceRotationAccessor spaceRotationAccessor && spaceRotationAccessor.planetShine$canRotateRoll()) {
            return spaceRotationAccessor.planetshine$getAdjustedX();
        }
        return original.call(instance, pPartialTicks);
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"))
    public float getViewYRot(Entity instance, float pPartialTicks, Operation<Float> original) {
        if (instance instanceof SpaceRotationAccessor spaceRotationAccessor && spaceRotationAccessor.planetShine$canRotateRoll()) {
            return spaceRotationAccessor.planetshine$getAdjustedY();
        }
        return original.call(instance, pPartialTicks);
    }
}
