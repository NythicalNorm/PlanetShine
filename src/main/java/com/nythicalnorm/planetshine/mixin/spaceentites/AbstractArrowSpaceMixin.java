package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractArrow.class)
public class AbstractArrowSpaceMixin {
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.99F, ordinal = 0))
    public float changeFrictionMultiplier(float constant) {
        AbstractArrow abstractArrow = ((AbstractArrow)(Object) this);

        if (abstractArrow.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue(constant, abstractArrow.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.05F, ordinal = 0))
    public float changeGravityMultiplier(float constant) {
        AbstractArrow abstractArrow = ((AbstractArrow)(Object) this);

        if (abstractArrow.level() != null) {
            return SpaceUtils.getEntityPlanetGravity(constant, abstractArrow.level());
        } else {
            return constant;
        }
    }
}
