package com.nythicalnorm.planetshine.planettexgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class PlanetGradient {
    RandomSource randomSource;
    BiomeGroup[] biomes;
    PlanetFeatures features;

    public PlanetGradient(BiomeGroup[] biomes) {
        this.biomes = biomes;
    }

    public RandomSource getRandomSource() {
        return randomSource;
    }

    public void setRandomSource(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public @Nullable String getBiomeNameAt(int x, int y, int z, CelestialBody body) {
        Vector3d pos = PlanetCalc.getPlanetRelativeNonRotatingPosition(x, y, z, body.getRadius(), true);
        pos.normalize();
        return PlanetMapGen.getBiomeNameAt(pos, this);
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
