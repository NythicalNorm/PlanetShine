package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin {

    @WrapMethod(method = "calculateViewVector")
    public Vec3 getViewVector(float pXRot, float pYRot, Operation<Vec3> original) {
        if (this instanceof SpaceRotationAccessor spaceRotationAccessor) {
            return spaceRotationAccessor.planetShine$getRotatedViewVector(pXRot, pYRot);
        }
        return original.call(pXRot, pYRot);
    }
}
