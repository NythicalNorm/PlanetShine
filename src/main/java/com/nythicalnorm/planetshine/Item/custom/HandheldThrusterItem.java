package com.nythicalnorm.planetshine.Item.custom;

import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.sound.PSSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HandheldThrusterItem extends Item {
    public HandheldThrusterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        pPlayer.startUsingItem(pUsedHand);
        pLevel.playSeededSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                PSSounds.HANDHELD_PROPELLER_START.get(), SoundSource.AMBIENT, 1f, 1f,0);
       return InteractionResultHolder.sidedSuccess(itemstack, false);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        if (pStack.getItem() == PSItems.HANDHELD_THRUSTER.get()) {
            return 1000;
        }
        return super.getUseDuration(pStack);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack pStack) {
        if (pStack.getItem() == PSItems.HANDHELD_THRUSTER.get()) {
            return UseAnim.BOW;
        }
        else {
            return super.getUseAnimation(pStack);
        }
    }
}
