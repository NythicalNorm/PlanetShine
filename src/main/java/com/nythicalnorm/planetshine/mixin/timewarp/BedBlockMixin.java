package com.nythicalnorm.planetshine.mixin.timewarp;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BedBlock.class)
public class BedBlockMixin { // temporary solution to be survival friendly, will come up with a better solution to the problem eventually
    @WrapMethod(method = "use")
    private InteractionResult useBed(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, Operation<InteractionResult> original) {
        if (!pLevel.isClientSide()) {
            ((ServerPlayer)pPlayer).sendSystemMessage(Component.literal(
                    "Time travel into the future is required to skip the time, luckily you can mimic this by sleeping irl"), true);
        }

        return InteractionResult.FAIL;
    }
}
