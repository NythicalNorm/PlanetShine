package com.nythicalnorm.nythicalSpaceProgram.event;

import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NythicalSpaceProgram.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {
//    @SubscribeEvent
//    public static void onItemUseStart(LivingEntityUseItemEvent event) {
//        if (event.getItem().getItem() == ModItems.HANDHELD_PROPELLER.get()) {
//            event.getItem().getOrCreateTag().putFloat("inuse", 1);
//        }
//    }
//
//
//    @SubscribeEvent
//    public static void onItemUseFinish(LivingEntityUseItemEvent event) {
//        if (event.getItem().getItem() == ModItems.HANDHELD_PROPELLER.get()) {
//            event.getItem().getOrCreateTag().putFloat("inuse", 0);
//        }
//    }
}
