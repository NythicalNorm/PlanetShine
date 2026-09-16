package com.nythicalnorm.planetshine.mixin.ship_gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.gui.screen.MouseLookScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.primitives.AABBi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private Vec3 position;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    private Entity entity;

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

    // stole some stuff from VS, it seems they themselves stole from minecraft Soooo. it's probably fine.
    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"))
    private double getMaxZoom(Camera instance, double ogZoom, Operation<Double> original) {
        if (Minecraft.getInstance().screen instanceof MouseLookScreen spacecraftScreen && spacecraftScreen.movePlayerCamera() &&
                spacecraftScreen.getViewMode() == MouseLookScreen.ViewMode.NON_ROTATING && spacecraftScreen.getMinecraft().level != null) {
            Minecraft minecraft = Minecraft.getInstance();
            ShipMountedToData shipMountedToData = VSGameUtilsKt.getShipMountedToData(minecraft.player, 0f);
            if (shipMountedToData != null) {
                final ClientShip clientShip = (ClientShip) shipMountedToData.getShipMountedTo();
                final AABBi boundingBox = (AABBi) clientShip.getShipAABB();
                double dist = ((boundingBox.lengthX() + boundingBox.lengthY() + boundingBox.lengthZ()) / 3.0) * 1.5;
                dist = dist > 4 ? dist : 4;
                double maxZoom = spacecraftScreen.getCameraZoomLevel(dist);

                for (int i = 0; i < 8; ++i) {
                    float f = (float) ((i & 1) * 2 - 1);
                    float g = (float) ((i >> 1 & 1) * 2 - 1);
                    float h = (float) ((i >> 2 & 1) * 2 - 1);
                    f *= 0.1F;
                    g *= 0.1F;
                    h *= 0.1F;
                    final Vec3 vec3 = this.position.add(f, g, h);
                    final Vec3 vec32 =
                            new Vec3(this.position.x - (double) this.forwards.x() * maxZoom + (double) f + (double) h,
                                    this.position.y - (double) this.forwards.y() * maxZoom + (double) g,
                                    this.position.z - (double) this.forwards.z() * maxZoom + (double) h);
                    final HitResult hitResult = RaycastUtilsKt.clipIncludeShips(minecraft.level,
                            new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity), true, clientShip.getId());
                    if (hitResult.getType() != HitResult.Type.MISS) {
                        final double e = hitResult.getLocation().distanceTo(this.position);
                        if (e < maxZoom) {
                            maxZoom = e; // never-nesters in shambles.
                        }
                    }
                }
                return maxZoom;
            }
        }
        return original.call(instance, ogZoom);
    }
}
