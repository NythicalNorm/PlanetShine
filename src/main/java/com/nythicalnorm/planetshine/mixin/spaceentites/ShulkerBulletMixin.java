package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ShulkerBullet.class)
public class ShulkerBulletMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04d, ordinal = 0))
    public double changeGravityMultiplier(double constant) {
        ShulkerBullet shulkerBullet = ((ShulkerBullet)(Object) this);

        if (shulkerBullet.level() != null) {
            return SpaceUtils.getEntityPlanetGravity((float) constant, shulkerBullet.level());
        } else {
            return constant;
        }
    }
}
