package com.nythicalnorm.planetshine.mixin.daynightcycle.villager;

import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {
    public WanderingTraderMixin(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyArg(
            method = "registerGoals",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",
                    ordinal = 1),
            index = 1
    )
    public <T extends Mob> Goal replaceUseItemGoal(Goal pGoal) {
        return new UseItemGoal<>((T) this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, (trader) -> {
            if (this.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
                return !planetTimeAccessor.ps$isDay(trader.getX(), trader.getZ()) && !trader.isInvisible();
            } else {
                return this.level().isNight() && !trader.isInvisible();
            }
        });
    }

    @ModifyArg(
            method = "registerGoals",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",
                    ordinal = 2),
            index = 1
    )
    public <T extends Mob> Goal replaceUseItemGoal2(Goal pGoal) {
        return new UseItemGoal<>((T) this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, (trader) -> {
            if (this.level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()) {
                return planetTimeAccessor.ps$isDay(trader.getX(), trader.getZ()) && trader.isInvisible();
            } else {
                return this.level().isDay() && trader.isInvisible();
            }
        });
    }
}
