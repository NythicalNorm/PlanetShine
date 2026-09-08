package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PrimedTnt.class)
public class PrimedTntMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.98D, ordinal = 0))
    public double changeFrictionMultiplier(double constant) {
        PrimedTnt primedTnt = ((PrimedTnt)(Object) this);

        if (primedTnt.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue((float) constant, primedTnt.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        PrimedTnt primedTnt = ((PrimedTnt)(Object) this);

        if (primedTnt.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, primedTnt.level());
        } else {
            return constant;
        }
    }
}
