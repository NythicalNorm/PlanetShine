package com.nythicalnorm.planetshine.mixin.vs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Camera.class, priority = 1500)
public class VSCameraMixinMixin {
    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.MixinCamera", name = "setupWithShipMounted")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"),
            require = 0)
    public float getViewYrot(Entity instance, float pPartialTick, Operation<Float> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getViewYrot();
        }
        return original.call(instance, pPartialTick);
    }

    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.MixinCamera", name = "setupWithShipMounted")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"),
            require = 0)
    public float getViewXrot(Entity instance, float pPartialTicks, Operation<Float> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getViewXrot();
        }
        return original.call(instance, pPartialTicks);
    }

    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.MixinCamera", name = "getMaxZoomIgnoringMountedShip")
    @ModifyReturnValue(
            method = "@MixinSquared:Handler",
            at = @At("RETURN"),
            require = 0)
    public double modifyMaxZoom(double original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getZoomLevel() * original;
        }
        return original;
    }
}
