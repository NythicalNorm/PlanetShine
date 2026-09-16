package com.nythicalnorm.planetshine.storage;

import net.minecraft.network.FriendlyByteBuf;

public class PSCommonConfig {
    private final boolean OverrideVanillaWorldBorder;
    private final boolean allowTimeWarpOnPlanets;
    private final boolean doChangeMCDayTimeValue;

    public PSCommonConfig(boolean overrideVanillaWorldBorder, boolean allowTimeWarpOnPlanets, boolean doChangeMCDayTimeValue) {
        this.OverrideVanillaWorldBorder = overrideVanillaWorldBorder;
        this.allowTimeWarpOnPlanets = allowTimeWarpOnPlanets;
        this.doChangeMCDayTimeValue = doChangeMCDayTimeValue;
    }

    public boolean isOverrideVanillaWorldBorder() {
        return OverrideVanillaWorldBorder;
    }

    public boolean doAllowTimeWarpOnPlanets() {
        return allowTimeWarpOnPlanets;
    }

    public boolean doChangeMCDayTimeValue() {
        return doChangeMCDayTimeValue;
    }

    public static PSCommonConfig fromByteBuf(FriendlyByteBuf byteBuf) {
        return new PSCommonConfig(
                byteBuf.readBoolean(),
                byteBuf.readBoolean(),
                byteBuf.readBoolean()
        );
    }

    public static PSCommonConfig fromServerConfig() {
        return new PSCommonConfig(
                PlanetShineConfig.isOverrideVanillaWorldBorder(),
                PlanetShineConfig.doAllowTimeWarpOnPlanets(),
                PlanetShineConfig.doChangeMCDayTimeValue()
        );
    }

    public FriendlyByteBuf toByteBuf(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(this.OverrideVanillaWorldBorder);
        byteBuf.writeBoolean(this.allowTimeWarpOnPlanets);
        byteBuf.writeBoolean(this.doChangeMCDayTimeValue);
        return byteBuf;
    }
}
