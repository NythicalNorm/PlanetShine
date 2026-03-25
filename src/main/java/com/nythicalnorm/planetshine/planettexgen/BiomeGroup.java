package com.nythicalnorm.planetshine.planettexgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BiomeGroup {
    private final String name;
    private final  BiomeGradient[] biomeGradients;
    private final float minValue;
    private final float maxValue;

    public BiomeGroup(String name, float minValue, float maxValue, BiomeGradient[] biomeGradients) {
        this.name = name;
        this.biomeGradients = biomeGradients;
        this.minValue = minValue;
        this.maxValue = maxValue;

        for (BiomeGradient biomeGradient : this.biomeGradients) {
            biomeGradient.adjustMinMaxValBasedOnGroup(this.minValue, this.maxValue);
        }
    }

    public static BiomeGroup readFromJson(JsonObject biomeGroup) {
        String name = biomeGroup.get("name").getAsString();
        float min = biomeGroup.get("min").getAsFloat();
        float max = biomeGroup.get("max").getAsFloat();
        List<BiomeGradient> biomeGradientList = new ArrayList<>();
        JsonArray biomeGradients = biomeGroup.get("biome_gradients").getAsJsonArray();

        biomeGradients.forEach(element -> {
            biomeGradientList.add(BiomeGradient.readFromJson(element.getAsJsonObject()));
        });

        return new BiomeGroup(name, min, max, biomeGradientList.toArray(new BiomeGradient[0]));
    }

    public BiomeGradient[] getBiomeGradients() {
        return biomeGradients;
    }

    public boolean isValueInRange(float noiseValue) {
        return noiseValue <= this.maxValue && noiseValue > this.minValue;
    }
}
