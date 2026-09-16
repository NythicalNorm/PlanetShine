package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Boat.class)
public class BoatMixin {
    @ModifyConstant(method = "floatBoat", constant = @Constant(floatValue = 0.9F, ordinal = 2))
    public float changeFrictionMultiplier(float constant) {
        Boat boat = ((Boat)(Object) this);

        if (boat.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue(constant, boat.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "floatBoat", constant = @Constant(doubleValue = (double) -0.04F, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        Boat boat = ((Boat)(Object) this);

        if (boat.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, boat.level());
        } else {
            return constant;
        }
    }
}
