package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrushableBlock.class)
public class BrushableBlockSpaceMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FallingBlock;isFree(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean inSpaceDim(boolean original, @Local(ordinal = 0, argsOnly = true) ServerLevel level) {
        if (SpaceUtils.isSpaceLevel(level)) {
            return false;
        } else {
            return original;
        }
    }

    @ModifyExpressionValue(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FallingBlock;isFree(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean inSpaceDim2(boolean original, @Local(ordinal = 0, argsOnly = true) Level level) {
        if (SpaceUtils.isSpaceLevel(level)) {
            return false;
        } else {
            return original;
        }
    }
}
