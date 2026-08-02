package com.nythicalnorm.planetshine.storage;

import net.minecraft.network.FriendlyByteBuf;

public class PSCommonConfig {
    private final boolean OverrideVanillaWorldBorder;
    private final boolean allowTimeWarpOnPlanets;

    public PSCommonConfig(boolean overrideVanillaWorldBorder, boolean allowTimeWarpOnPlanets) {
        this.OverrideVanillaWorldBorder = overrideVanillaWorldBorder;
        this.allowTimeWarpOnPlanets = allowTimeWarpOnPlanets;
    }

    public boolean isOverrideVanillaWorldBorder() {
        return OverrideVanillaWorldBorder;
    }

    public boolean doAllowTimeWarpOnPlanets() {
        return allowTimeWarpOnPlanets;
    }

    public static PSCommonConfig fromByteBuf(FriendlyByteBuf byteBuf) {
        return new PSCommonConfig(
                byteBuf.readBoolean(),
                byteBuf.readBoolean()
        );
    }

    public static PSCommonConfig fromServerConfig() {
        return new PSCommonConfig(
                PlanetShineConfig.isOverrideVanillaWorldBorder(),
                PlanetShineConfig.doAllowTimeWarpOnPlanets()
        );
    }

    public FriendlyByteBuf toByteBuf(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(this.OverrideVanillaWorldBorder);
        byteBuf.writeBoolean(this.allowTimeWarpOnPlanets);
        return byteBuf;
    }
}
