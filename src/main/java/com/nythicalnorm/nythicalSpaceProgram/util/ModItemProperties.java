package com.nythicalnorm.nythicalSpaceProgram.util;

import com.nythicalnorm.nythicalSpaceProgram.Item.ModItems;
import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.HANDHELD_PROPELLER.get(), ResourceLocation.fromNamespaceAndPath(NythicalSpaceProgram.MODID, "inuse"),
                (pStack, pLevel, pEntity, pSeed) -> {
            if (pEntity == null) {
                return 0f;
            }
            if (pEntity.getUseItem().getItem() == ModItems.HANDHELD_PROPELLER.get() && pEntity.isUsingItem()) { //getUseItem().getItem() == ModItems.HANDHELD_PROPELLER.get()) {
                return 1f;
            }
            else {
                return 0f;
            }
        });
    }
}
