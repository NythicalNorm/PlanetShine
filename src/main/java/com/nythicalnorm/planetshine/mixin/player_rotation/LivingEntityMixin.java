package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "stopRiding", at = @At(value = "RETURN"))
    public void stopRiding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof SpaceRotationAccessor spaceRotationAccessor &&
                entity.level() != null && entity.level().isClientSide() &&
                entity != entity.getVehicle() && entity.getVehicle() != null
        ) {
            if (ValkyrienSkies.isBlockInShipyard(entity.level(), entity.getVehicle().blockPosition())) {
                Ship ship = ValkyrienSkies.getShipManagingBlock(entity.level(), entity.getVehicle().blockPosition());
                spaceRotationAccessor.planetShine$setSpaceRotationOffset(new Quaternionf(ship.getTransform().getRotation()));
            }
        }
    }
}
