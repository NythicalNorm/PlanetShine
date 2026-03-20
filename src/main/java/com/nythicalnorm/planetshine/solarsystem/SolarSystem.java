package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundSetOrbitIntercept;
import com.nythicalnorm.planetshine.solarsystem.bodies.*;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.util.RunnableExecutor;
import com.nythicalnorm.planetshine.util.Stage;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class SolarSystem {
    private final Map<ResourceKey<Level>, CelestialBody> planetDimensions;
    private final Map<OrbitId, CelestialBody> allPlanetaryBodies;
    private final ConcurrentMap<OrbitId, EntityOrbitBody> allSpacecraftBodies;
    private final StarBody rootStar;
    private Stage universeStage;

    public SolarSystem(Map<OrbitId, CelestialBody> pAllPlanetaryBodies, ConcurrentMap<OrbitId,
            EntityOrbitBody> pAllSpacecraftBodies, Map<ResourceKey<Level>, CelestialBody> pPlanetDimensions,
                       StarBody rootStar) {
        this.allPlanetaryBodies = pAllPlanetaryBodies;
        this.allSpacecraftBodies = pAllSpacecraftBodies;
        this.planetDimensions = pPlanetDimensions;
        this.rootStar = rootStar;
    }

    public void setStage(Stage stage) {
        this.universeStage = stage;
    }

    public Stage getUniverseStage() {
        return universeStage;
    }

    @PhysTickOnly
    public void UpdatePlanets(long currentTime, boolean isTimeWarping) {
        rootStar.simulatePlanets(currentTime, isTimeWarping);
    }

    @PhysTickOnly
    public void UpdateSpacecraft(long currentTime, boolean isTimeWarping) {
        this.allSpacecraftBodies.values().forEach((entityOrbitBody ->
                entityOrbitBody.simulate(currentTime, isTimeWarping)));
    }

    @PhysTickOnly // server side only
    public void calculateSpacecraftIntercepts(long timeElapsed, RunnableExecutor gameTickRunnable) {
        for (EntityOrbitBody entityBody : this.allSpacecraftBodies.values()) {
            if (entityBody.isHostOfItsSpace() && !entityBody.isOrbitInterceptsCalculated()) {
                OrbitalCalc.SOIIntercept intercept = entityBody.calculateIntercepts(timeElapsed);
                gameTickRunnable.addRun(() ->
                        PacketHandler.sendToAllClients(new ClientboundSetOrbitIntercept(entityBody.getOrbitId(), intercept))
                );
            }
        }
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

    @PhysTickOnly
    public void entityChangeOrbitalSOIs(EntityOrbitBody spacecraftBody, OrbitId newParentID, OrbitalElementsc orbitalElementsNew) {
        entityChangeOrbitalSOIs(spacecraftBody, getPlanet(newParentID), orbitalElementsNew);
    }

    @PhysTickOnly
    public void entityChangeOrbitalSOIs(EntityOrbitBody spacecraftBody, CelestialBody newOrbitPlanet, OrbitalElementsc orbitalElementsNew) {
        //removing the old reference to the object
        spacecraftBody.removeParent();
        // adding reference to new object
        newOrbitPlanet.addChildBody(spacecraftBody);

        spacecraftBody.setOrbitalElements(orbitalElementsNew);
        // resetting the pre-calculated intercepts and also resetting the periapsisTime
        if (!spacecraftBody.isClientSide()) {
            spacecraftBody.resetIntercepts(this.universeStage.getCurrentTime());
        }
    }

    public void entityJoinedOrbital(OrbitalBody OrbitalDataNew, OrbitId newParentID) {
        this.entityJoinedOrbital(getPlanet(newParentID), OrbitalDataNew);
    }

    public void entityJoinedOrbital(CelestialBody newOrbitPlanet, OrbitalBody orbitalDataNew) {
        if (newOrbitPlanet != null) {
            if (orbitalDataNew instanceof EntityOrbitBody entitySpacecraftBody) {
                this.getAllSpacecraftBodies().computeIfAbsent(entitySpacecraftBody.getOrbitId(), id -> {
                    entitySpacecraftBody.removeParent();
                    newOrbitPlanet.addChildBody(entitySpacecraftBody);
                    entitySpacecraftBody.init();
                    return entitySpacecraftBody;
                });
            }
        } else {
            PlanetShine.logError("EntityBody tried to join non-existent CelestialBody, ignoring Entity");
        }
    }

    @PhysTickOnly // not sure if this is true but better safe than sorry
    public void entityRemoveOrbital(EntityOrbitBody entityOrbitBody) {
        entityOrbitBody.OnRemove();
        entityOrbitBody.removeParent();
        entityOrbitBody.removeHostSpaces();
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

    public AbstractSpaceshipBody getShipFromVSId(long id) { // very sus to store ship id has part of the 128 bit uuid, need to change it
        if (this.allSpacecraftBodies.get(new OrbitId(id)) instanceof AbstractSpaceshipBody serverSpaceshipBody) {
            return serverSpaceshipBody;
        }
        return null;
    }

    @PhysTickOnly
    public void OnPhysTick() {
        for (CelestialBody celestialBody : this.getAllPlanetaryBodies().values()) {
            celestialBody.physTick(this);
        }
    }

    @GameTickOnly
    public void onServerTick(SpaceServerLevel spaceLevel) {
        for (CelestialBody celestialBody : this.getAllPlanetaryBodies().values()) {
            celestialBody.serverTick(this, spaceLevel);
        }
    }
}
