package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractArrow.class)
public class AbstractArrowSpaceMixin {
    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.99F))
    public float changeFrictionMultiplier(float constant) {
        AbstractArrow abstractArrow = ((AbstractArrow)(Object) this);

        if (abstractArrow.level() != null && SpaceUtils.isSpaceLevel(abstractArrow.level())) {
            return 1.0F;
        } else {
            return constant;
        }
    }
}
