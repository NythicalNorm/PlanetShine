package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin {
    @WrapMethod(method = "getDragAir")
    public double getDragAir(Operation<Double> original) {
        double overworldDrag = original.call();
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
