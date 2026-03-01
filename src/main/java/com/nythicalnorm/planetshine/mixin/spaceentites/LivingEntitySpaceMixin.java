package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public class LivingEntitySpaceMixin {
    @Shadow
    private float speed;

    @ModifyConstant(method = "travel", constant = @Constant(floatValue = 0.91F))
    public float changeFrictionMultiplier(float constant) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        float frictionVal = constant;

        if (livingEntity.level() != null && OrbitalBodyUtils.isSpaceLevel(livingEntity.level()) && !livingEntity.onGround()) {
            frictionVal = 1.0F;
            if (livingEntity instanceof Player player && player.getAbilities().flying) {
                frictionVal = constant;
            }
        }
        return frictionVal;
    }

    @WrapMethod(method = "onBelowWorld")
    public void onBelowWorld(Operation<Void> original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (!OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            original.call();
        }
    }

    @WrapMethod(method = "calculateEntityAnimation")
    public void calculateEntityAnimation(boolean pIncludeHeight, Operation<Void> original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (!OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            original.call(pIncludeHeight);
        }
    }

    @WrapOperation(method = "handleDamageEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;setSpeed(F)V"))
    public void walkAnimSetSpeed1(WalkAnimationState instance, float pSpeed, Operation<Void> original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (!OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            original.call(instance, speed);
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;setSpeed(F)V"))
    public void walkAnimSetSpeed2(WalkAnimationState instance, float pSpeed, Operation<Void> original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (!OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            original.call(instance, speed);
        }
    }

    @WrapOperation(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"))
    private void setFallFlyingFlag(LivingEntity instance, int flag, boolean value, Operation<Void> original) {
        if (OrbitalBodyUtils.isSpaceLevel(instance.level()) && !instance.isPassenger()) {
            original.call(instance, flag, true);
        } else {
            original.call(instance, flag, value);
        }
    }

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean isFallFlyingMoveCalculation(boolean original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            return false;
        } else {
            return original;
        }
    }
}
