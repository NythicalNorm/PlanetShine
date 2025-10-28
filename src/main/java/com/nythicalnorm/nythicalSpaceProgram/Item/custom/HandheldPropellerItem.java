package com.nythicalnorm.nythicalSpaceProgram.Item.custom;

import com.nythicalnorm.nythicalSpaceProgram.Item.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HandheldPropellerItem extends Item {
    public HandheldPropellerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
       return InteractionResultHolder.sidedSuccess(itemstack, false);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        if (pStack.getItem() == ModItems.HANDHELD_PROPELLER.get()) {
            return 1000;
        }
        return super.getUseDuration(pStack);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack pStack) {
        if (pStack.getItem() == ModItems.HANDHELD_PROPELLER.get()) {
            return UseAnim.BOW;
        }
        else {
            return super.getUseAnimation(pStack);
        }
    }
}
