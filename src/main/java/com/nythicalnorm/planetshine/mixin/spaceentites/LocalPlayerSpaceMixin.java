package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerSpaceMixin {
    @Inject(method = "serverAiStep", at = @At(value = "TAIL"))
    public void serverAIstep(CallbackInfo ci) {
        LocalPlayer player = ((LocalPlayer)(Object) this);

        if (player.level() != null && OrbitalBodyUtils.isSpaceLevel(player.level()) && !player.onGround() &&
                !player.getAbilities().flying) {
            player.xxa = 0.0f;
            player.zza = 0.0f;
        }
    }

    @Inject(method = "move", at = @At(value = "HEAD"), cancellable = true)
    public void playerMoveCheck(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        PlayerOrbitAccessor playerOrbit = (PlayerOrbitAccessor) this;

        if (playerOrbit.getOrbitalBody() != null && playerOrbit.getOrbitalBody().isHostOfItsSpace()) {
            ((ClientPlayerOrbitBody)playerOrbit.getOrbitalBody()).processHostMove(pPos);
            ((Player)(Object)this).setDeltaMovement(Vec3.ZERO);
            ci.cancel();
        }
    }

    @Inject(method = "sendPosition", at = @At(value = "HEAD"))
    public void sendVelocity(CallbackInfo ci) {
        PlayerOrbitAccessor playerOrbit = (PlayerOrbitAccessor) this;
        if (playerOrbit.getOrbitalBody() != null && playerOrbit.getOrbitalBody().getOrbitalElements() != null && playerOrbit.getOrbitalBody().isHostOfItsSpace()) {
            ((ClientPlayerOrbitBody)playerOrbit.getOrbitalBody()).sendMovementPacket();
        }
    }
}
