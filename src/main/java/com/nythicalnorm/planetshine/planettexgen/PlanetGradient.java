package com.nythicalnorm.planetshine.planettexgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PlanetGradient {
    BiomeGroup[] biomes;
    PlanetFeatures features;

    public PlanetGradient(BiomeGroup[] biomes) {
        this.biomes = biomes;
    }

    public static PlanetGradient readFromJson(JsonObject jsonObject) {
        JsonArray biomeGroups = jsonObject.get("biome_groups").getAsJsonArray();

        List<BiomeGroup> biomeGroupList = new ArrayList<>();

        biomeGroups.forEach(element -> {
            biomeGroupList.add(BiomeGroup.readFromJson(element.getAsJsonObject()));
        });

        return new PlanetGradient(biomeGroupList.toArray(new BiomeGroup[0]));
    }
}
