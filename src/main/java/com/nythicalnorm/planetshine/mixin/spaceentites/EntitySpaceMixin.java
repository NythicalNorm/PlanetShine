package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntitySpaceMixin {
    @Shadow
    private Level level;

    @ModifyReturnValue(method = "isNoGravity", at = @At(value = "TAIL"))
    public boolean isNoGravity(boolean original) {
        if (OrbitalBodyUtils.isSpaceLevel(level)) {
            return true;
        } else {
            return original;
        }
    }

    @ModifyExpressionValue(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isNoGravity()Z"))
    private boolean saveNoGravity(boolean original) {
        if (!OrbitalBodyUtils.isSpaceLevel(level)) {
            return original;
        } else {
            return false;
        }
    }
}