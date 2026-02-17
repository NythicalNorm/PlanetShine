package com.nythicalnorm.voxelspaceprogram.mixin.daynightcycle;

import com.nythicalnorm.voxelspaceprogram.PSClient;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.DaylightData;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.PlanetTimeAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientLevel.class)
public class ClientLevelMixin implements PlanetTimeAccessor {

    @Override
    public boolean ps$DaylightDataExists() {
        return PSClient.get() != null && PSClient.get().isOnPlanet();
    }

    @Override
    public void ps$setDaylightData(DaylightData daylightData) {
        // don't need this
    }

    @Override
    public float ps$getSunAngle(double x, double z) {
        if (ps$DaylightDataExists()) {
            return PSClient.get().getDaylightRegion().getSunAngle();
        } else {
            ClientLevel clientLevel = (ClientLevel) (Object) this;
            return clientLevel.dimensionType().timeOfDay(clientLevel.dayTime());
        }
    }

    @Override
    public int ps$getDarknessAmount(double x, double z) {
        if (ps$DaylightDataExists()) {
            return PSClient.get().getDaylightRegion().getDarknessAmount();
        } else {
            ClientLevel clientLevel = (ClientLevel) (Object) this;
            return clientLevel.getSkyDarken();
        }
    }

    @Override
    public boolean ps$isDay(double x, double z) {
        if (ps$DaylightDataExists()) {
            return PSClient.get().getDaylightRegion().isDay();
        } else {
            ClientLevel clientLevel = (ClientLevel) (Object) this;
            return clientLevel.isDay();
        }
    }
}