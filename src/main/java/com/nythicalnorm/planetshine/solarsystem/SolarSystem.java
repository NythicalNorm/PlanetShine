package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.bodies.*;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    public void playerChangeOrbitalSOIs(EntityOrbitBody spacecraftBody, OrbitId newParentID, OrbitalElements orbitalElementsNew) {
        playerChangeOrbitalSOIs(spacecraftBody, getPlanet(newParentID), orbitalElementsNew);
    }

    public void playerChangeOrbitalSOIs(EntityOrbitBody spacecraftBody, CelestialBody newOrbitPlanet, OrbitalElements orbitalElementsNew) {
        //removing the old reference to the object
        spacecraftBody.removeParent();
        // adding reference to new object
        newOrbitPlanet.addChildBody(spacecraftBody);

        spacecraftBody.setOrbitalElements(orbitalElementsNew);
    }

    public void entityJoinedOrbital(OrbitalBody OrbitalDataNew, OrbitId newParentID) {
        entityJoinedOrbital(getPlanet(newParentID), OrbitalDataNew);
    }

    public void entityJoinedOrbital(CelestialBody newOrbitPlanet, OrbitalBody orbitalDataNew) {
        if (newOrbitPlanet != null) {
            if (orbitalDataNew instanceof EntityOrbitBody entitySpacecraftBody &&
                    getAllSpacecraftBodies().putIfAbsent(entitySpacecraftBody.getOrbitId(), entitySpacecraftBody) == null) {
                entitySpacecraftBody.removeParent();
                newOrbitPlanet.addChildBody(entitySpacecraftBody);
            }
        } else {
            PlanetShine.logError("EntityBody tried to join non-existent CelestialBody, ignoring Entity");
        }
    }

    public void entityRemoveOrbital(EntityOrbitBody entityOrbitBody) {
        entityOrbitBody.removeParent();
        entityOrbitBody.setHostSpace(null);
        entityOrbitBody.setOrbitalElements(null);
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

    public List<EntityOrbitBody> getAllEntitiesOrbitsList() {
        return allSpacecraftBodies.values().stream().toList();
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
