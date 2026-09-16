package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.98D, ordinal = 0))
    public double changeFrictionMultiplier(double constant) {
        FallingBlockEntity fallingBlock = ((FallingBlockEntity)(Object) this);

        if (fallingBlock.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue((float) constant, fallingBlock.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        FallingBlockEntity fallingBlock = ((FallingBlockEntity)(Object) this);

        if (fallingBlock.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, fallingBlock.level());
        } else {
            return constant;
        }
    }
}
