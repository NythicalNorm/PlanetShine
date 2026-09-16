package com.nythicalnorm.planetshine.mixin.daynightcycle.villager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Villager.class)
public class VillagerMixin {
    @WrapOperation(method = "registerBrainGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getDayTime()J"))
    public long getDayTime(Level instance, Operation<Long> original) {
        Villager villager = (Villager) (Object) this;
        if (instance instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            // put 0.0d Z so that they have a consistent schedule period and don't sleep for 6 months if on the North Pole.
            return planetTimeAccessor.ps$getDayTime(villager.getX(), 0.0d);
        }
        return original.call(instance);
    }
}
