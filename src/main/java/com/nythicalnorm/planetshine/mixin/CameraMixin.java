package com.nythicalnorm.planetshine.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"))
    public float getViewYrot(Entity instance, float pPartialTick, Operation<Float> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getViewYrot();
        }
        return original.call(instance, pPartialTick);
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"))
    public float getViewXrot(Entity instance, float pPartialTicks, Operation<Float> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getViewXrot();
        }
        return original.call(instance, pPartialTicks);
    }
}
