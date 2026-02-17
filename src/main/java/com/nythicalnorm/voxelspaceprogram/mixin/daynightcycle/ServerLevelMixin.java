package com.nythicalnorm.voxelspaceprogram.mixin.daynightcycle;

import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.PlanetTimeAccessor;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements PlanetTimeAccessor {
    @Unique
    @Nullable DaylightData ps$daylightData;

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
}
