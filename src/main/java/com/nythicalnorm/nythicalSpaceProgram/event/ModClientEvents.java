package com.nythicalnorm.nythicalSpaceProgram.event;

import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.PlanetRenderer;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.PlanetShine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NythicalSpaceProgram.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void OnLeveRenderedStartEvent(RenderLevelStageEvent.RegisterStageEvent event) {
        NythicalSpaceProgram.LOGGER.debug("Setting Up Planet Rendering: ");
        long  beforeTimes = System.nanoTime();
        PlanetRenderer.setupModels();
        PlanetShine.setupBuffers();
        NythicalSpaceProgram.LOGGER.debug("Set Up Complete Took : " + (System.nanoTime()-beforeTimes)/1000000f + " milliseconds");
    }
}
