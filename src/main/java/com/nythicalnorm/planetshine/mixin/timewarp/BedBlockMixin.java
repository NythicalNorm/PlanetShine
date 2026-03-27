package com.nythicalnorm.planetshine.mixin.timewarp;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import net.minecraft.core.BlockPos;
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
        if (pPlayer.isCrouching()) {
            if (pLevel.isClientSide()) {
                ClientPacketHandler.openTimeWarpMapScreen();
            }
            return InteractionResult.CONSUME;
        }
        return original.call(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
