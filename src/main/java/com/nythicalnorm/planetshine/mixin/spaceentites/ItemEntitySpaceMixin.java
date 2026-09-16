package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
public class ItemEntitySpaceMixin {
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.98F))
    public float changeFrictionMultiplier(float constant) {
        ItemEntity itemEntity = ((ItemEntity)(Object) this);

        if (itemEntity.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue(constant, itemEntity.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.98D, ordinal = 0))
    public double changeFrictionMultiplier2(double constant) {
        ItemEntity itemEntity = ((ItemEntity)(Object) this);

        if (itemEntity.level() != null) {
            return AtmosphereCalc.getEntityFrictionValue((float) constant, itemEntity.level());
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        ItemEntity itemEntity = ((ItemEntity)(Object) this);

        if (itemEntity.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, itemEntity.level());
        } else {
            return constant;
        }
    }
}
