package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.dimensions.SpaceDimensionalSpecialEffects;
import com.nythicalnorm.planetshine.rendering.generators.QuadSphereModelGenerator;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.rendering.shaders.PSShaders;
import com.nythicalnorm.planetshine.util.PSKeyBinds;
import net.minecraft.Util;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PlanetShine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(PSItems.HANDHELD_THRUSTER.get(), ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID, "inuse"),
                (pStack, pLevel, pEntity, pSeed) -> {
                    if (pEntity != null && pEntity.getUseItem().getItem() == PSItems.HANDHELD_THRUSTER.get() && pEntity.isUsingItem()) {
                        return 1f;
                    }
                    return 0f;
                });

        VSClientEvents.addListeners();
    }

    @SubscribeEvent
    public static void OnLevelRenderedStartEvent(RenderLevelStageEvent.RegisterStageEvent event) {
        PlanetShine.log("Baking Planet Models: ");
        long  beforeTimes = Util.getMillis();
        QuadSphereModelGenerator.setupModels();
        PSRenderer.setupBuffers();
        PlanetShine.log("Setup Complete Took : " + (Util.getMillis()-beforeTimes) + " milliseconds");
    }

    @SubscribeEvent
    public static void OnDimensionalSpecialEffectsRegister(RegisterDimensionSpecialEffectsEvent event) {
        event.register(PlanetShine.rl("ps_space_dimension_effects"), new SpaceDimensionalSpecialEffects());
    }

    @SubscribeEvent
    public static void OnKeyRegister (RegisterKeyMappingsEvent event) {
        event.register(PSKeyBinds.INC_TIME_WARP_KEY);
        event.register(PSKeyBinds.DEC_TIME_WARP_KEY);
        event.register(PSKeyBinds.OPEN_SOLAR_SYSTEM_MAP_KEY);
        event.register(PSKeyBinds.OPEN_SPACECRAFT_HUD_KEY);
        event.register(PSKeyBinds.CHANGE_SPACECRAFT_VIEW_KEY);
    }

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException
    {
        PSShaders.registerShaders(event);
    }
}
