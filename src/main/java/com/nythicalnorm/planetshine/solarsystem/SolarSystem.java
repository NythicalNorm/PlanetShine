package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.solarsystem.bodies.*;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class SolarSystem {
    private final Map<ResourceKey<Level>, CelestialBody> planetDimensions;
    private final Map<OrbitId, CelestialBody> allPlanetaryBodies;
    private final ConcurrentMap<OrbitId, EntityOrbitBody> allSpacecraftBodies;
    private final StarBody rootStar;

    public SolarSystem(Map<OrbitId, CelestialBody> pAllPlanetaryBodies, ConcurrentMap<OrbitId, EntityOrbitBody> pAllSpacecraftBodies, Map<ResourceKey<Level>, CelestialBody> pPlanetDimensions, StarBody rootStar) {
        this.allPlanetaryBodies = pAllPlanetaryBodies;
        this.allSpacecraftBodies = pAllSpacecraftBodies;
        this.planetDimensions = pPlanetDimensions;
        this.rootStar = rootStar;
    }

    public void UpdatePlanets(long currentTime, boolean isTimeWarping) {
        rootStar.simulatePlanets(currentTime, isTimeWarping);
    }

    public Map<OrbitId, EntityOrbitBody> getAllSpacecraftBodies() {
        return allSpacecraftBodies;
    }

    public Map<ResourceKey<Level>, CelestialBody> getPlanetDimensions() {
        return planetDimensions;
    }

    public @Nullable CelestialBody getPlanet(String key) {
        for (CelestialBody planetaryBody : allPlanetaryBodies.values()) {
            if (planetaryBody.getName().equals(key)) {
                return planetaryBody;
            }
        }
        return null;
    }

    public CelestialBody getPlanet(OrbitId planetID) {
        return allPlanetaryBodies.get(planetID);
    }

    public EntityOrbitBody getSpacecraftOrbit(OrbitId spacecraftBodyAddress) {
        return allSpacecraftBodies.get(spacecraftBodyAddress);
    }

    public void playerChangeOrbitalSOIs(OrbitalBody spacecraftBody, OrbitId newParentID, OrbitalElements orbitalElementsNew) {
        playerChangeOrbitalSOIs(spacecraftBody, getPlanet(newParentID), orbitalElementsNew);
    }

    public void playerChangeOrbitalSOIs(OrbitalBody spacecraftBody, CelestialBody newOrbitPlanet, OrbitalElements orbitalElementsNew) {
        //removing the old reference to the object
        spacecraftBody.removeParent();
        // adding reference to new object
        newOrbitPlanet.addChildBody(spacecraftBody);

        spacecraftBody.setOrbitalElements(orbitalElementsNew);
    }

    public void playerJoinedOrbital(OrbitalBody OrbitalDataNew, OrbitId newParentID) {
        playerJoinedOrbital(getPlanet(newParentID), OrbitalDataNew);
    }

    public void playerJoinedOrbital(CelestialBody newOrbitPlanet, OrbitalBody orbitalDataNew) {
        if (newOrbitPlanet != null) {
            //temp default Rotation
            orbitalDataNew.setRotation(new Quaternionf());
            newOrbitPlanet.addChildBody(orbitalDataNew);

            if (orbitalDataNew instanceof EntityOrbitBody entitySpacecraftBody) {
                getAllSpacecraftBodies().putIfAbsent(entitySpacecraftBody.getOrbitId(), entitySpacecraftBody);
            }
        }
    }

    public void entityRemoveOrbital(EntityOrbitBody entityOrbitBody) {
        entityOrbitBody.removeParent();
        entityOrbitBody.setHostSpace(null);
        this.allSpacecraftBodies.remove(entityOrbitBody.getOrbitId());
    }

    public List<String> getAllPlanetNames() {
        List<String> planetNames = new ArrayList<>();
        for (CelestialBody planetaryBody : allPlanetaryBodies.values()) {
            planetNames.add(planetaryBody.getName());
        }
        return planetNames;
    }

    public Map<OrbitId, CelestialBody> getAllPlanetaryBodies() {
        return allPlanetaryBodies;
    }

    public List<CelestialBody> getAllPlanetOrbitsList() {
        return allPlanetaryBodies.values().stream().toList();
    }

    public CelestialBody getOverworldPlanet() {
        return planetDimensions.get(Level.OVERWORLD);
    }

    public StarBody getRootStar() {
        return rootStar;
    }

    public CelestialBody getDimensionOfPlanet(ResourceKey<Level> dim) {
        return planetDimensions.get(dim);
    }
}
