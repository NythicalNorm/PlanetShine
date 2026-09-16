package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrowableProjectile.class)
public class ThrowableProjectileMixin {
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.99F))
    public float changeFrictionMultiplier(float constant) {
        ThrowableProjectile projectile = ((ThrowableProjectile)(Object) this);

        if (projectile.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue(constant, projectile.level());
        } else {
            return constant;
        }
    }

    @WrapMethod(method = "getGravity")
    public float getAdjustedGravity(Operation<Float> original) {
        ThrowableProjectile projectile = ((ThrowableProjectile)(Object) this);
        float overworldGravity = original.call();

        if (projectile.level() != null) {
            return SpaceUtils.getEntityPlanetGravity(overworldGravity, projectile.level());
        }
        return overworldGravity;
    }
}
