package com.nythicalnorm.planetshine.mixin.daynightcycle.isDay;

import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public class MobMixin {
    @Redirect(method = "isSunBurnTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    public boolean isDay(Level instance) {
        Mob mob = (Mob) (Object) this;
        if (mob.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
            return planetTimeAccessor.ps$isDay(mob.getX(), mob.getZ());
        }
        return instance.isDay();
    }
}
