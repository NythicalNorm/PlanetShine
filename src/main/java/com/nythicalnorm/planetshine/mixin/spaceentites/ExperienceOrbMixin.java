package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.98F))
    public float changeFrictionMultiplier(float constant) {
        ExperienceOrb experienceOrb = ((ExperienceOrb)(Object) this);

        if (experienceOrb.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue(constant, experienceOrb.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.98D, ordinal = 0))
    public double changeFrictionMultiplier2(double constant) {
        ExperienceOrb experienceOrb = ((ExperienceOrb)(Object) this);

        if (experienceOrb.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue((float) constant, experienceOrb.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.03d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        ExperienceOrb experienceOrb = ((ExperienceOrb)(Object) this);

        if (experienceOrb.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, experienceOrb.level());
        } else {
            return constant;
        }
    }
}
