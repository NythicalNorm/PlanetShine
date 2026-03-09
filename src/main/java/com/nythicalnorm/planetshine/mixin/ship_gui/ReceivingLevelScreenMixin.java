package com.nythicalnorm.planetshine.mixin.ship_gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ReceivingLevelScreen.class)
public class ReceivingLevelScreenMixin {
    @Inject( method = "render", at = @At("HEAD"))
    public void renderScreen(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
//        if (PSClient.get() != null && PSClient.get().doRender()) {
//            ci.cancel();
//        }
    }
}
