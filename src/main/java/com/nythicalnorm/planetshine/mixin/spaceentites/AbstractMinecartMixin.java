package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin {
    @WrapOperation(method = "comeOffTrack",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getDragAir()D"))
    public double getDragAir(AbstractMinecart instance, Operation<Double> original) {
        double overworldDrag = original.call(instance);
        AbstractMinecart minecart = ((AbstractMinecart)(Object) this);
        if (minecart.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue((float) overworldDrag, minecart.level());
        } else {
            return overworldDrag;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        AbstractMinecart minecart = ((AbstractMinecart)(Object) this);

        if (minecart.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, minecart.level());
        } else {
            return constant;
        }
    }
}
