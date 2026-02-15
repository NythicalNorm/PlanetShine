package com.nythicalnorm.voxelspaceprogram.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.voxelspaceprogram.util.OrbitalBodyUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public class LivingEntitySpaceMixin {
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

//    @WrapOperation(
//            method = "travel",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
//    public void playerMoveCheck(LivingEntity instance, MoverType moverType, Vec3 vec3, Operation<Void> original) {
//        if (instance.level().isClientSide() && instance instanceof Player player && ((PlayerOrbitAccessor)player).getOrbit() != null &&
//                ((PlayerOrbitAccessor)player).getOrbit().isHostOfItsSpace()) {
//            ((ClientPlayerOrbitBody)((PlayerOrbitAccessor)player).getOrbit()).processMove(vec3);
//        } else {
//            original.call(instance, moverType, vec3);
//        }
//    }
//
//    @WrapOperation(
//            method = "handleRelativeFrictionAndCalculateMovement",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
//    public void playerMoveCheck2(LivingEntity instance, MoverType moverType, Vec3 vec3, Operation<Void> original) {
//        if (instance.level().isClientSide() && instance instanceof Player player && ((PlayerOrbitAccessor)player).getOrbit() != null &&
//                ((PlayerOrbitAccessor)player).getOrbit().isHostOfItsSpace()) {
//            ((ClientPlayerOrbitBody)((PlayerOrbitAccessor)player).getOrbit()).processMove(vec3);
//        } else {
//            original.call(instance, moverType, vec3);
//        }
//    }

    @WrapMethod(method = "onBelowWorld")
    public void onBelowWorld(Operation<Void> original) {
        LivingEntity livingEntity = ((LivingEntity)(Object) this);
        if (!OrbitalBodyUtils.isSpaceLevel(livingEntity.level())) {
            original.call();
        }
    }
}
