package com.nythicalnorm.planetshine.storage;

import net.minecraftforge.common.ForgeConfigSpec;

public class PlanetShineConfig {
    public static final ForgeConfigSpec CONFIG_SPEC = buildConfig();
    private static ForgeConfigSpec.ConfigValue<Double> TeleportToGroundHeight;
    private static ForgeConfigSpec.ConfigValue<Double> TeleportToSpaceHeight;
    private static ForgeConfigSpec.ConfigValue<Double> PlanetTextureResolution;
    private static ForgeConfigSpec.ConfigValue<Boolean> OverrideVanillaWorldBorder;

    public static double getTeleportToGroundHeight() {
        try {
            return TeleportToGroundHeight.get();
        } catch (Exception e) {
            return 500.0d;
        }
    }

    public static double getTeleportToSpaceHeight() {
        try {
            return TeleportToSpaceHeight.get();
        } catch (Exception e) {
            return 1000.0d;
        }
    }

    public static double getPlanetTextureResolution() {
        try {
            return PlanetTextureResolution.get();
        } catch (Exception e) {
            return 2048.0d;
        }
    }

    public static boolean getOverrideVanillaWorldBorder() {
        try {
            return OverrideVanillaWorldBorder.get();
        } catch (Exception e) {
            return true;
        }
    }

    private static ForgeConfigSpec buildConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        TeleportToGroundHeight = builder.define("TeleportToGroundHeight", 500.0d);
        TeleportToSpaceHeight = builder.define("TeleportToSpaceHeight", 1000.0d);
        PlanetTextureResolution = builder.define("PlanetTextureResolution", 2048.0d);
        OverrideVanillaWorldBorder = builder.define("OverrideVanillaWorldBorder", true);
        return builder.build();
    }
}
