package com.nythicalnorm.voxelspaceprogram.event;

import com.nythicalnorm.voxelspaceprogram.VoxelSpaceProgram;
import com.nythicalnorm.voxelspaceprogram.commands.NSPTeleportCommand;
import com.nythicalnorm.voxelspaceprogram.PSServer;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBodyAccessor;
import com.nythicalnorm.voxelspaceprogram.storage.PlanetDataResolver;
import com.nythicalnorm.voxelspaceprogram.util.OrbitalBodyUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = VoxelSpaceProgram.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeServerEvents {
    @SubscribeEvent
    public static void OnTick(TickEvent.ServerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && PSServer.get() != null) {
            PSServer.get().OnGameTick();
        }
    }

    @SubscribeEvent
    public static void onCommandsRegiser(RegisterCommandsEvent event) {
        new NSPTeleportCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void OnResourceReload(AddReloadListenerEvent event) {
        event.addListener(new PlanetDataResolver());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            if (PSServer.get() != null) {
                CelestialBody planetaryBody = PSServer.get().getSolarSystem().getDimensionOfPlanet(serverLevel.dimension());
                if (planetaryBody != null && serverLevel instanceof CelestialBodyAccessor celestialBodyAccessor) {
                    celestialBodyAccessor.setCelestialBody(planetaryBody);
                }
            }
        }
    }

    @SubscribeEvent
    public static void OnLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof Level level) {
            if (OrbitalBodyUtils.isSpaceLevel(level) && PSServer.get() != null) {
                PSServer.get().saveSolarSys();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        VoxelSpaceProgram.log("Hello");
        PSServer.getInstance().ifPresent(psServer -> psServer.playerJoined(event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            PSServer.getInstance().ifPresent(psServer -> psServer.playerCloned(serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        PSServer.getInstance().ifPresent(psServer -> psServer.playerDimChanged(event.getEntity(), event.getTo()));
    }
}
