package com.nythicalnorm.planetshine;

import com.mojang.logging.LogUtils;
import com.nythicalnorm.planetshine.Item.PSCreativeModeTab;
import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.block.PSBlocks;
import com.nythicalnorm.planetshine.commands.PSArguments;
import com.nythicalnorm.planetshine.event.VSServerEvents;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypeRegistry;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.sound.PSSounds;
import com.nythicalnorm.planetshine.storage.PSDataPackManager;
import com.nythicalnorm.planetshine.storage.PlanetShineConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.slf4j.Logger;

@Mod(PlanetShine.MODID)
public class PlanetShine
{
    public static final String MODID = "planetshine";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlanetShine(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        PSItems.register(modEventBus);
        PSBlocks.register(modEventBus);
        PSSounds.register(modEventBus);
        PSArguments.register(modEventBus);
        OrbitalBodyTypeRegistry.ORBITAL_BODY_TYPES.register(context.getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::commonSetup);
        PSCreativeModeTab.register(modEventBus);

        VSServerEvents.addListeners();

        context.registerConfig(ModConfig.Type.SERVER, PlanetShineConfig.CONFIG_SPEC);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(PacketHandler::register);
    }

    public static void log(String msg){
        LOGGER.debug(msg);
    }
    public static void logError(String msg){
        LOGGER.error(msg);
    }
    public static void logWarn(String msg){
        LOGGER.warn(msg);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents
    {
        @SubscribeEvent
        public static void createRegistries(NewRegistryEvent event) {
            event.create(new RegistryBuilder<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>>()
                    .setName(PlanetShine.rl("orbital_bodies")));
        }
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event)
    {
        PSDataPackManager.loadServerDataAndStartSolarSystem(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        PSServer.getInstance().ifPresent(PSServer::serverStarted);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PSServer.close();
    }
}
