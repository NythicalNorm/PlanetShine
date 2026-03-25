package com.nythicalnorm.planetshine.mixin.worldborder;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin {
//    @WrapOperation(method = "collideBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;getCollisionShape()Lnet/minecraft/world/phys/shapes/VoxelShape;"))
//    private static VoxelShape getWorldBorderShape(WorldBorder instance, Operation<VoxelShape> original, @Local(argsOnly = true) Entity entity) {
//        if (entity.level() instanceof CelestialBodyAccessor celestialBodyAccessor){
//            CelestialBody celestialBody = celestialBodyAccessor.ps$getCelestialBody();
//
//        } else {
//            return original.call(instance);
//        }
//    }
}
