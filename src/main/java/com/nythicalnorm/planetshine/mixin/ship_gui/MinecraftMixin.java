package com.nythicalnorm.planetshine.mixin.ship_gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public Screen screen;

    @WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"))
    public void keyboardReleaseAll(Operation<Void> original) {
        if (this.screen instanceof MouseLookScreen mouseLookScreen) {
            if (mouseLookScreen.resetKeysOnScreenOpen()) {
                original.call();
            }
        } else {
            original.call();
        }
    }
}
