package com.nythicalnorm.planetshine.mixin.vs;

import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public class TempFrustumMixin {
    @Unique
    private int loopCount = 0; // Frustum objects are newly created each frame so no need to reset this to zero every tick.

    @Inject(method = "offsetToFullyIncludeCameraCube", at = @At(value = "INVOKE",
        target = "Lorg/joml/FrustumIntersection;intersectAab(FFFFFF)I"), cancellable = true)
    private void checkIfItsExceeded(int i, CallbackInfoReturnable<Frustum> cir) {
        if (loopCount > 10) {
            cir.setReturnValue((Frustum) (Object) this); // put a breakpoint here if you want to see when this happens
            loopCount = 0;
        } else {
            loopCount++;
        }
    }
}
