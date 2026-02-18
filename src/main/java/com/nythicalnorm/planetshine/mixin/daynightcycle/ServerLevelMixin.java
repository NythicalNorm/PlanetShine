package com.nythicalnorm.planetshine.mixin.daynightcycle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetTimeAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements PlanetTimeAccessor {
    @Unique
    @Nullable DaylightData ps$daylightData;

    @Final
    @Shadow
    List<ServerPlayer> players;

    @Final
    @Shadow
    private SleepStatus sleepStatus;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickStart(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        if (ps$daylightData != null) {
            ps$daylightData.tickStart();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickEnd(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        if (ps$daylightData != null) {
            ps$daylightData.tickEnd();
        }
    }

    @Override
    public boolean ps$DaylightDataExists() {
        return ps$daylightData != null;
    }

    @Override
    public float ps$getSunAngle(double x, double z) {
        if (ps$daylightData != null) {
            return ps$daylightData.getOrCalculateRegionAt(x, z).getSunAngle();
        } else {
            ServerLevel serverLevel = (ServerLevel) (Object) this;
            return serverLevel.dimensionType().timeOfDay(serverLevel.dayTime());
        }
    }

    @Override
    public int ps$getDarknessAmount(double x, double z) {
        if (ps$daylightData != null) {
            return ps$daylightData.getOrCalculateRegionAt(x, z).getDarknessAmount();
        } else {
            ServerLevel serverLevel = (ServerLevel) (Object) this;
            return serverLevel.getSkyDarken();
        }
    }

    @Override
    public boolean ps$isDay(double x, double z) {
        if (ps$daylightData != null) {
            return ps$daylightData.getOrCalculateRegionAt(x, z).isDay();
        } else {
            ServerLevel serverLevel = (ServerLevel) (Object) this;
            return serverLevel.isDay();
        }
    }

    @Override
    public void ps$setDaylightData(DaylightData daylightData) {
        this.ps$daylightData = daylightData;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"))
    public void wakeUpAllPlayers(ServerLevel instance, Operation<Void> original) {
        if (((PlanetTimeAccessor)instance).ps$DaylightDataExists()) {
            boolean isDayAtAnyPlayerPos = false;
            for(Player player : players) {
                if (player.isSleeping() && ps$isDay(player.position().x(), player.position().z())) {
                    isDayAtAnyPlayerPos = true;
                }
            }
            PSServer.get().setSleepTimeWarping(!isDayAtAnyPlayerPos);
            if (isDayAtAnyPlayerPos) {
                original.call(instance);
            }
        } else {
            original.call(instance);
        }
    }

    @Inject(method = "updateSleepingPlayerList", at = @At(value = "TAIL"))
    public void updatePlayerSleep(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (((PlanetTimeAccessor)level).ps$DaylightDataExists()) {
            int i = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
            if (!sleepStatus.areEnoughSleeping(i)) {
                PSServer.get().setSleepTimeWarping(false);
            }
        }
    }
}
