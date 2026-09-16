package com.nythicalnorm.planetshine.mixin.daynightcycle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;dot(Lorg/joml/Vector3fc;)F"))
    private static float setupColor(Vector3f instance, Vector3fc v) {
        Vector3d sunPos = PSRenderer.getSunPosOverworld();
        if (sunPos != null) {
            if (PSClient.get().getDaylightRegion().isOngoingEclipse()) {
                return -1.0f;
            }
            return -instance.dot((float) sunPos.x, (float) sunPos.y, (float) sunPos.z);
        }
        return instance.dot(v);
    }

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"))
    private static float[] getSunriseColor(DimensionSpecialEffects instance, float f4, float v, Operation<float[]> original) {
        if (PSClient.get() != null && PSClient.get().doRender()) {
            if (PSClient.get().isOnPlanet() && !PSClient.get().getCurrentPlanet().get().getAtmosphere().hasAtmosphere()) {
                return null;
            }
        }

        return original.call(instance, f4, v);
    }
}
