package com.nythicalnorm.planetshine.mixin.vs;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;

@Mixin(value = Camera.class, priority = 1500)
public abstract class VSCameraMixinMixin { // Yes I am going to name all mixin^2 mixins this way.

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

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
    @ModifyVariable(
            method = "@MixinSquared:Handler",
            at = @At("HEAD"),
            require = 0, argsOnly = true)
    public double modifyMaxZoom(double original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera()) {
            return spacecraftScreen.getCameraZoomLevel(original);
        }
        return original;
    }

    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.MixinCamera", name = "setRotationWithShipTransform")
    @WrapMethod(method = "@MixinSquared:Handler")
    private void setSurfaceDownRotation(float yaw, float pitch, ShipTransform renderTransform, Operation<Void> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen screen && screen.movePlayerCamera() && screen.getViewMode() == MouseLookScreen.ViewMode.SURFACE_DOWN) {
            final Quaternionf originalRotation =
                    new Quaternionf().rotateY((float) Math.toRadians(-yaw)).rotateX((float) Math.toRadians(pitch)).normalize();
            this.xRot = pitch;
            this.yRot = yaw;
            this.rotation.set(new Quaternionf(PSClient.get().getPlayerOrbit().getSurfaceDownRot()).mul(originalRotation));
            this.forwards.set(0.0F, 0.0F, 1.0F);
            this.rotation.transform(this.forwards);
            this.up.set(0.0F, 1.0F, 0.0F);
            this.rotation.transform(this.up);
            this.left.set(1.0F, 0.0F, 0.0F);
            this.rotation.transform(this.left);
        } else {
            original.call(yaw, pitch, renderTransform);
        }
    }

//    @TargetHandler(mixin = "org.valkyrienskies.mod.mixin.client.MixinCamera", name = "setupWithShipMounted")
//    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/valkyrienskies/core/api/ships/properties/ShipTransform;getShipCoordinatesToWorldCoordinatesRotation()Lorg/joml/Quaterniondc;"))
//    private Quaterniondc setSurfaceDownRotForShip(ShipTransform instance, Operation<Quaterniondc> original) {
//        if (Minecraft.getInstance().screen instanceof MouseLookScreen screen && screen.movePlayerCamera() &&
//                screen.getViewMode() == MouseLookScreen.ViewMode.SURFACE_DOWN) {
//            return new Quaterniond(); //new Quaterniond(PSClient.get().getPlayerOrbit().getSurfaceDownRot());
//        } else {
//            return original.call(instance);
//        }
//    }
}
