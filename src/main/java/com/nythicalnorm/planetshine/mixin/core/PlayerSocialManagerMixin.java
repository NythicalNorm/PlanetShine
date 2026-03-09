package com.nythicalnorm.planetshine.mixin.core;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerSocialManager.class)
public class PlayerSocialManagerMixin {
    @Inject(method = "addPlayer", at = @At(value = "TAIL"))
    public void playerJoined(PlayerInfo pPlayerInfo, CallbackInfo ci) {
        if (PSClient.get() != null) {
            EntityOrbitBody entityOrbitBody = PSClient.get().getSolarSystem().getSpacecraftOrbit(new OrbitId(pPlayerInfo.getProfile().getId()));
            if (entityOrbitBody instanceof ClientPlayerOrbitBody clientPlayerOrbitBody) {
                clientPlayerOrbitBody.playerJoined(pPlayerInfo);
            }
        }
    }

    @Inject(method = "removePlayer", at = @At(value = "TAIL"))
    public void removePlayer(UUID pId, CallbackInfo ci) {
        if (PSClient.get() != null) {
            EntityOrbitBody entityOrbitBody = PSClient.get().getSolarSystem().getSpacecraftOrbit(new OrbitId(pId));
            if (entityOrbitBody instanceof ClientPlayerOrbitBody clientPlayerOrbitBody) {
                clientPlayerOrbitBody.playerLeft();
            }
        }
    }
}
