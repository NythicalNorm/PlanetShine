package com.nythicalnorm.planetshine.mixin.daynightcycle.villager;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UpdateActivityFromSchedule.class)
public class UpdateActivityFromScheduleMixin {
    @ModifyReturnValue(method = "create", at = @At(value = "RETURN"))
    private static BehaviorControl<LivingEntity> createUpdateActivity(BehaviorControl<LivingEntity> original) {
        return BehaviorBuilder.create((livingEntityInstance) -> {
            return livingEntityInstance.point((serverLevel, livingEntity, time) -> {
                // put 0.0d Z so that they have a consistent schedule period and don't sleep for 6 months if on the North Pole.
                livingEntity.getBrain().updateActivityFromSchedule(
                        ((PlanetTimeAccessor)serverLevel).ps$getDayTime(livingEntity.getX(), 0.0d),
                        serverLevel.getGameTime());
                return true;
            });
        });
    }
}
