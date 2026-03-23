package com.nythicalnorm.planetshine.mixin.daynightcycle;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetTimeAccessor;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientLevel.class)
public class ClientLevelMixin implements PlanetTimeAccessor {

    @Override
    public boolean ps$DaylightDataExists() {
        return PSClient.get() != null && (PSClient.get().isOnPlanet() || PSClient.get().weInSpaceDim());
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

    @WrapMethod(method = "getSkyDarken")
    private float getSpaceSkyDarken(float pPartialTick, Operation<Float> original) {
        if (SpaceUtils.isSpaceLevel((Level) (Object) this)) {
            return Mth.clamp(1.0f - PSClient.get().getDaylightRegion().getSunOcclusion(), 0.2f, 1f);
        } else {
            return original.call(pPartialTick);
        }
    }
}