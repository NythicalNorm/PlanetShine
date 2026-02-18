package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.PlayerSpacecraftScreen;
import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;
import com.nythicalnorm.planetshine.util.KeyBindings;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PlanetShine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        ItemProperties.register(PSItems.HANDHELD_THRUSTER.get(), ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID, "inuse"),
                (pStack, pLevel, pEntity, pSeed) -> {
            if (pEntity != null && pEntity.getUseItem().getItem() == PSItems.HANDHELD_THRUSTER.get() && pEntity.isUsingItem()) {
                return 1f;
            }
            return 0f;
        });
    }

    @SubscribeEvent
    public static void OnKeyInput (InputEvent.Key event) {
        if (KeyBindings.INC_TIME_WARP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(true)));
        } else if (KeyBindings.DEC_TIME_WARP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent((psClient ->
                    psClient.TryChangeTimeWarp(false)));
        } else if (KeyBindings.OPEN_SOLAR_SYSTEM_MAP_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent(psClient -> {
                if (psClient.doRender()) {
                    Minecraft.getInstance().setScreen(new MapSolarSystemScreen(false));
                }
            });
        }
        else if (KeyBindings.USE_PLAYER_JETPACK_KEY.consumeClick()) {
            PSClient.getInstance().ifPresent(psClient -> {
                if (psClient.doRender()) {
                    Minecraft.getInstance().setScreen(new PlayerSpacecraftScreen(Minecraft.getInstance().player, psClient));
                    psClient.setControllingBody(psClient.getPlayerOrbit());
                }
            });
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
        PSClient.getInstance().ifPresent(css -> {
            css.getPlayerOrbit().setPlayer(event.getNewPlayer());

            if (!OrbitalBodyUtils.isSpaceLevel(event.getNewPlayer().level())) {
                css.orbitRemove(css.getPlayerOrbit().getOrbitId());
            }
        });
    }
}
