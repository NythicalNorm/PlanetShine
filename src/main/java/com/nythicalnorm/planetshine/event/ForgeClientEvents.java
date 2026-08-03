package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PlanetShine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void OnKeyInput (InputEvent.Key event) {
        if (PSKeyBinds.INC_TIME_WARP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(true, false)));
        } else if (PSKeyBinds.DEC_TIME_WARP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(false, false)));
        } else if (PSKeyBinds.OPEN_SOLAR_SYSTEM_MAP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent(psClient -> {
                if (psClient.doRender()) {
                    psClient.getScreenManager().openMapScreen();
                }
            });
        }
        else if (PSKeyBinds.OPEN_SPACECRAFT_HUD_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent(psClient ->
                    psClient.getScreenManager().openPSSpacecraftScreen(true));
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide() && event.getLevel() instanceof ClientLevel clientLevel) {
            PSClient.getInstance().ifPresent(css -> css.onClientLevelLoad(clientLevel));
        }
    }

    @SubscribeEvent
    public static void clientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PSClient.getInstance().ifPresent(PSClient::tick);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(ClientPlayerNetworkEvent.LoggingOut event) {
        PSClient.close();
    }

    @SubscribeEvent
    public static void onPlayerCloned(ClientPlayerNetworkEvent.Clone event) {
        PSClient.getInstance().ifPresent(psClient -> {
            //psClient.getScreenManager().playerChangeDimension();
            psClient.getPlayerOrbit().setBody(event.getNewPlayer());

            if (!SpaceUtils.isSpaceLevel(event.getNewPlayer().level())) {
                psClient.orbitRemove(psClient.getPlayerOrbit().getOrbitId());
            }
        });
    }
}
