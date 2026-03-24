package com.nythicalnorm.planetshine.storage;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.planettexgen.PlanetGradient;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PSDataPackManager {
    private static PlanetDataResolver.PlanetLoadedData planetLoadedData;
    private static Map<String, PlanetGradient> PLANET_GRADIENT_MAP;
    private static final String PSCommonData = "ps_common_data";

    public static void planetDatapackLoaded(PlanetDataResolver.PlanetLoadedData pPlanetLoadedData) {
        if (PSServer.getInstance().isEmpty()) {
            planetLoadedData = pPlanetLoadedData;
        } else {
            PlanetShine.log("Datapack reloaded, but planets can't be changed during runtime with datapacks.");
        }
    }

    public static void textureGenDatapackLoaded(Map<String, PlanetGradient> planetGradientMap) {
        PLANET_GRADIENT_MAP = planetGradientMap;
    }

    public static @Nullable PlanetGradient getPlanetGradient(String name) {
        if (PLANET_GRADIENT_MAP != null) {
            return PLANET_GRADIENT_MAP.get(name);
        } else {
            return null;
        }
    }

    public static void loadServerDataAndStartSolarSystem(MinecraftServer pServer) {
        Map<OrbitId, CelestialBody> AllPlanetaryBodies = new Object2ObjectOpenHashMap<>();
        Map<ResourceKey<Level>, CelestialBody> PlanetDimensions = new Object2ObjectOpenHashMap<>();
        ConcurrentMap<OrbitId, EntityOrbitBody<?>> AllSpacecraftBodies = new ConcurrentHashMap<>();

        StarBody rootStar;

        if (planetLoadedData != null) {
            rootStar = planetLoadedData.rootStar();
            loadPlanetData(AllPlanetaryBodies, PlanetDimensions);
        } else {
            throw new IllegalStateException("Can't start Solar System server because no planet data is loaded");
        }
        // load spacecraft data here

        SolarSystem solarSystem = new SolarSystem(AllPlanetaryBodies, AllSpacecraftBodies, PlanetDimensions, rootStar);
        new PSServer(pServer, solarSystem);
    }

    public static PSCommonSaveData createOrLoadSaveData(DimensionDataStorage dataStorage) {
       return dataStorage.computeIfAbsent(PSCommonSaveData::load, PSCommonSaveData::new, PSCommonData);
    }

    private static void loadPlanetData(Map<OrbitId, CelestialBody> pAllPlanetaryBodies, Map<ResourceKey<Level>, CelestialBody> pPlanetDimensions) {
        for (Map.Entry<String, CelestialBody> entry : planetLoadedData.tempPlanetaryBodyMap().entrySet()) {
            String[] childPlanets = planetLoadedData.tempChildPlanetsMap().get(entry.getKey());
            for (String planet : childPlanets) {
                entry.getValue().addChildPlanet(planetLoadedData.tempPlanetaryBodyMap().get(planet));
            }

            if (entry.getValue().getDimension() != null) {
                pPlanetDimensions.put(entry.getValue().getDimension(), entry.getValue());
            }

            pAllPlanetaryBodies.put(entry.getValue().getOrbitId(), entry.getValue());
        }

        planetLoadedData = null;
    }
}
