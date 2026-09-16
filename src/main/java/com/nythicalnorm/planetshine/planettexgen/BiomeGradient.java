package com.nythicalnorm.planetshine.planettexgen;

import com.google.gson.JsonObject;

import java.awt.*;

public class BiomeGradient {
    float minValue;
    float maxValue;
    float minLatitude;
    float maxLatitude;
    Color biomeColor;
    float latitudeOpacity;

    public BiomeGradient(float minValue, float maxValue, float minLatitude, float maxLatitude, float latitudeOpacity, Color biomeColor) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.biomeColor = biomeColor;
        this.latitudeOpacity = latitudeOpacity;
    }

    public static BiomeGradient readFromJson(JsonObject biomeGradient) {
        float min = biomeGradient.get("min").getAsFloat();
        float max = biomeGradient.get("max").getAsFloat();
        String colorStr = biomeGradient.get("color").getAsString();
        Color colorVal = Color.decode(colorStr);

        return new BiomeGradient(min, max, 0.0f, 0.0f, 0.0f, colorVal);
    }

    public void adjustMinMaxValBasedOnGroup(float minGroup, float maxGroup) {
        float range = Math.abs(maxGroup - minGroup);
        this.minValue = minGroup + this.minValue*range;
        this.maxValue = minGroup + this.maxValue*range;
    }

    public boolean isValueInRange(float noiseValue) {
        return noiseValue <= this.maxValue && noiseValue > this.minValue;
    }

    public Color getBiomeColor() {
        return biomeColor;
    }
}
