package com.nythicalnorm.planetshine.mixin.ship_gui;

import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Redirect(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
            ordinal = 2, opcode = Opcodes.GETFIELD))
    private Screen canPassInputToPlayerControl(Minecraft mc) {
        if (mc.screen instanceof MouseLookScreen) {
            return null;
        } else {
            return mc.screen;
        }
    }
}
