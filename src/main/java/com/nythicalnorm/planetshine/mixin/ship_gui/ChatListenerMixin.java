package com.nythicalnorm.planetshine.mixin.ship_gui;

import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleSystemMessage", at = @At(value = "HEAD"))
    private void handleSystemMessageToPSScreens(Component pMessage, boolean pIsOverlay, CallbackInfo ci) {
        if (pIsOverlay && minecraft.screen instanceof MouseLookScreen mouseLookScreen) {
            mouseLookScreen.setSystemMessage(pMessage);
        }
    }
}
