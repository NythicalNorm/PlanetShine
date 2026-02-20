package com.nythicalnorm.planetshine.planettexgen.lod_tex;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Map;
import java.util.Objects;

public class BiomeColorHolder {
    private static final Map<Biome, Integer> BIOME_COLOR_MAP = new Object2ObjectOpenHashMap<>();

    public static void init(RegistryAccess registryAccess) {
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        //Oceans
        putColor(biomeRegistry, Biomes.OCEAN, 0x3F76E4);
        putColor(biomeRegistry, Biomes.DEEP_OCEAN, 0x3F76E4);
        putColor(biomeRegistry, Biomes.WARM_OCEAN, 0x43D5EE);
        putColor(biomeRegistry, Biomes.LUKEWARM_OCEAN, 0x45ADF2);
        putColor(biomeRegistry, Biomes.DEEP_LUKEWARM_OCEAN, 0x45ADF2);
        putColor(biomeRegistry, Biomes.COLD_OCEAN, 0x3D57D6);
        putColor(biomeRegistry, Biomes.DEEP_COLD_OCEAN, 0x3D57D6);
        putColor(biomeRegistry, Biomes.FROZEN_OCEAN, 0x83a8e9);
        putColor(biomeRegistry, Biomes.DEEP_FROZEN_OCEAN, 0x83a8e9);
        putColor(biomeRegistry, Biomes.MUSHROOM_FIELDS, 0x6d646a);

        //Highlands
        putColor(biomeRegistry, Biomes.JAGGED_PEAKS, 0x8d9191);
        putColor(biomeRegistry, Biomes.FROZEN_PEAKS, 0xedfafa);
        putColor(biomeRegistry, Biomes.STONY_PEAKS, 0x5d5d5d);
        putColor(biomeRegistry, Biomes.MEADOW, 0x506d4c);
        putColor(biomeRegistry, Biomes.CHERRY_GROVE, 0xa1567e);
        putColor(biomeRegistry, Biomes.GROVE, 0xedfafa);
        putColor(biomeRegistry, Biomes.SNOWY_SLOPES, 0xe4eced);
        putColor(biomeRegistry, Biomes.WINDSWEPT_HILLS, 0x485f47);
        putColor(biomeRegistry, Biomes.WINDSWEPT_GRAVELLY_HILLS, 0x7e7978);
        putColor(biomeRegistry, Biomes.WINDSWEPT_FOREST, 0x4d664c);

        //Woodland Biomes
        putColor(biomeRegistry, Biomes.FOREST, 0x3f7e20);
        putColor(biomeRegistry, Biomes.FLOWER_FOREST, 0x346b1a);
        putColor(biomeRegistry, Biomes.TAIGA, 0x294129);
        putColor(biomeRegistry, Biomes.OLD_GROWTH_PINE_TAIGA, 0x5f3f16);
        putColor(biomeRegistry, Biomes.OLD_GROWTH_SPRUCE_TAIGA, 0x5f3f16);
        putColor(biomeRegistry, Biomes.SNOWY_TAIGA, 0x5f3f16);
        putColor(biomeRegistry, Biomes.BIRCH_FOREST, 0x577942);
        putColor(biomeRegistry, Biomes.OLD_GROWTH_BIRCH_FOREST, 0x5f3f16);
        putColor(biomeRegistry, Biomes.DARK_FOREST, 0x284e19);
        putColor(biomeRegistry, Biomes.JUNGLE, 0x0d3502);
        putColor(biomeRegistry, Biomes.SPARSE_JUNGLE, 0x1a5303);
        putColor(biomeRegistry, Biomes.BAMBOO_JUNGLE, 0x507f18);

        // Wetland biomes
        putColor(biomeRegistry, Biomes.RIVER, 0x3F76E4);
        putColor(biomeRegistry, Biomes.FROZEN_RIVER, 0x708bb9);
        putColor(biomeRegistry, Biomes.SWAMP, 0x232317);
        putColor(biomeRegistry, Biomes.MANGROVE_SWAMP, 0x465712);
        putColor(biomeRegistry, Biomes.BEACH, 0xdad0a5);
        putColor(biomeRegistry, Biomes.STONY_SHORE, 0x787878);

        //Flatland biomes
        putColor(biomeRegistry, Biomes.PLAINS, 0x5d793c);
        putColor(biomeRegistry, Biomes.SUNFLOWER_PLAINS, 0x5d793c);
        putColor(biomeRegistry, Biomes.SNOWY_PLAINS, 0xe7e9e8);
        putColor(biomeRegistry, Biomes.ICE_SPIKES, 0x6887bb);

        // Aridland biomes
        putColor(biomeRegistry, Biomes.DESERT, 0xdacfa3);
        putColor(biomeRegistry, Biomes.SAVANNA, 0x69632e);
        putColor(biomeRegistry, Biomes.SAVANNA_PLATEAU, 0x6f6930);
        putColor(biomeRegistry, Biomes.WINDSWEPT_SAVANNA, 0x694b32);
        putColor(biomeRegistry, Biomes.BADLANDS, 0xbb6521);
        putColor(biomeRegistry, Biomes.WOODED_BADLANDS, 0x654831);
        putColor(biomeRegistry, Biomes.ERODED_BADLANDS, 0x975d44);
    }

    private static void putColor(Registry<Biome> biomeRegistry, ResourceKey<Biome> biomeResourceKey, int color) {
        BIOME_COLOR_MAP.put(biomeRegistry.get(biomeResourceKey), color);
    }

    public static int getColorForBiome(Biome biome) {
        if (biome != null) {
            return Objects.requireNonNullElse(BIOME_COLOR_MAP.get(biome), 0);
        }

        return 0;
    }
}
