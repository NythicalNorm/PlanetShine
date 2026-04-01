package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.commands.AddHorizonsSpacecraftCommand;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.commands.RemoveHorizonsSpacecraftCommand;
import com.nythicalnorm.planetshine.commands.UpdateHorizonsSpacecraftCommand;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.storage.PlanetDataResolver;
import com.nythicalnorm.planetshine.storage.PlanetTextureResolver;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = PlanetShine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeServerEvents {
    @SubscribeEvent
    public static void OnTick(TickEvent.ServerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && PSServer.get() != null) {
            PSServer.get().OnGameTick();
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        //new PSTeleportCommand(event.getDispatcher());
        new AddHorizonsSpacecraftCommand(event.getDispatcher());
        new RemoveHorizonsSpacecraftCommand(event.getDispatcher());
        new UpdateHorizonsSpacecraftCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void OnResourceReload(AddReloadListenerEvent event) {
        event.addListener(new PlanetDataResolver());
        event.addListener(new PlanetTextureResolver());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            if (PSServer.get() != null) {
                CelestialBody planetaryBody = PSServer.get().getSolarSystem().getDimensionOfPlanet(serverLevel.dimension());
                if (planetaryBody != null && serverLevel instanceof CelestialBodyAccessor celestialBodyAccessor) {
                    celestialBodyAccessor.ps$setCelestialBody(planetaryBody);
                    planetaryBody.getCelestialServerData().setServerLevel(serverLevel);
                }
            }
        }
    }

    @SubscribeEvent
    public static void OnLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof Level level) {
            if (SpaceUtils.isSpaceLevel(level) && PSServer.get() != null) {
                PSServer.get().saveSolarSys();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        PSServer.getInstance().ifPresent(psServer -> psServer.playerJoined((ServerPlayer) event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        PSServer.getInstance().ifPresent(psServer -> psServer.playerLeft(event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath() && event.getEntity() instanceof ServerPlayer playerNew) {
            PSServer.getInstance().ifPresent(psServer -> psServer.playerCloned(playerNew));
        }
    }

    @SubscribeEvent
    public static void OnSleepingTimeCheckEvent(SleepingTimeCheckEvent event) {
        if (event.getEntity().level() instanceof PlanetTimeAccessor planetTimeAccessor && planetTimeAccessor.ps$DaylightDataExists()){
            if (planetTimeAccessor.ps$isDay(event.getEntity().position().x(), event.getEntity().position().z())) {
                event.setResult(Event.Result.DENY);
            }
            else {
                event.setResult(Event.Result.ALLOW);
            }
        }
        else {
            event.setResult(Event.Result.DEFAULT);
        }
    }
}
