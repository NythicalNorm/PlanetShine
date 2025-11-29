package com.nythicalnorm.nythicalSpaceProgram.planet;

import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.dimensions.SpaceDimension;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Optional;

public class PlanetDimensions {
    private static final HashMap<ResourceKey<Level>, String> planetDimensions = new HashMap<>();

    public static void registerPlanetDim(String name, ResourceKey<Level> planetLevel) {
        planetDimensions.put(planetLevel, name);
    }

    public static boolean isDimensionPlanet(ResourceKey<Level> dim) {
        if (dim == null) {
            return false;
        }
        return planetDimensions.containsKey(dim);
    }

    //server side call only
    public static PlanetaryBody getDimensionPlanet(DimensionType dim) {
        for (ResourceKey<Level> level : planetDimensions.keySet()) {
            if (level == null) {
                continue;
            }
            if (NythicalSpaceProgram.getSolarSystem().getServer().getLevel(level).dimensionType() == dim) {
                return Planets.getPlanet(planetDimensions.get(level));
            }
        }
        return null;
    }

    public static Optional<PlanetaryBody> getDimPlanet(ResourceKey<Level> dim) {
        if (planetDimensions.containsKey(dim)) {
            return Optional.of(Planets.PLANETARY_BODIES.get(planetDimensions.get(dim)));
        }
        return Optional.empty();
    }

    public static boolean isDimensionSpace(ResourceKey<Level> dim) {
        return dim == SpaceDimension.SPACE_LEVEL_KEY;
    }
}
