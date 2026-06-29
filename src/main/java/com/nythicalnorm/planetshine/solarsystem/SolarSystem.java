package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.PSServer;
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
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;
import org.valkyrienskies.core.api.world.PhysLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SolarSystem {
    private final Map<ResourceKey<Level>, CelestialBody> planetDimensions;
    private final Map<OrbitId, CelestialBody> allPlanetaryBodies;
    private final ConcurrentMap<OrbitId, EntityOrbitBody<?>> allSpacecraftBodies;
    private final ConcurrentMap<Long, AbstractSpaceshipBody> allVSships;
    private final StarBody rootStar;
    private UniverseStage universeStage;

    public SolarSystem(Map<OrbitId, CelestialBody> pAllPlanetaryBodies,
                       ConcurrentMap<OrbitId, EntityOrbitBody<?>> pAllSpacecraftBodies,
                       Map<ResourceKey<Level>, CelestialBody> pPlanetDimensions,
                       StarBody rootStar) {
        this.allPlanetaryBodies = pAllPlanetaryBodies;
        this.allSpacecraftBodies = pAllSpacecraftBodies;
        this.planetDimensions = pPlanetDimensions;
        this.rootStar = rootStar;
        this.allVSships = this.populateVSListFromSpacecrafts(this.allSpacecraftBodies);
    }

    private ConcurrentMap<Long, AbstractSpaceshipBody> populateVSListFromSpacecrafts(ConcurrentMap<OrbitId, EntityOrbitBody<?>> allSpacecraftBodies) {
        ConcurrentMap<Long, AbstractSpaceshipBody> vsShips = new ConcurrentHashMap<>();

        allSpacecraftBodies.values().forEach(entityOrbitBody -> {
            if (entityOrbitBody instanceof AbstractSpaceshipBody spaceshipBody) {
                vsShips.put(spaceshipBody.getOrbitId().getShipID(), spaceshipBody);
            }
        });
        return vsShips;
    }

    public void setStage(UniverseStage universeStage) {
        this.universeStage = universeStage;
    }

    public UniverseStage getUniverseStage() {
        return universeStage;
    }

    @PhysTickOnly
    public void UpdatePlanets(long currentTime, boolean isTimeWarping) {
        rootStar.simulatePlanets(currentTime, isTimeWarping);
    }

    @PhysTickOnly
    public void UpdateSpacecraft(long currentTime, boolean isTimeWarping, float deltaTime) {
        this.allSpacecraftBodies.values().forEach((entityOrbitBody ->
                entityOrbitBody.simulate(currentTime, isTimeWarping, deltaTime)));
    }

    @PhysTickOnly // server side only
    public void calculateSpacecraftIntercepts(long timeElapsed) {
        for (EntityOrbitBody<?> entityBody : this.allSpacecraftBodies.values()) {
            if (entityBody.isHostOfItsSpace() && !entityBody.isOrbitInterceptsCalculated()) {
                OrbitalCalc.SOIIntercept intercept = entityBody.calculateIntercepts(timeElapsed);
                PSServer.addGameTickRunnable(() ->
                        PacketHandler.sendToAllClients(new ClientboundSetOrbitIntercept(entityBody.getOrbitId(), intercept))
                );
            }
        }
    }

    public Map<OrbitId, EntityOrbitBody<?>> getAllSpacecraftBodies() {
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

    public EntityOrbitBody<?> getSpacecraftOrbit(OrbitId spacecraftBodyAddress) {
        return allSpacecraftBodies.get(spacecraftBodyAddress);
    }

    @PhysTickOnly
    public void entityChangeOrbitalSOIs(EntityOrbitBody<?> spacecraftBody, OrbitId newParentID, OrbitalElementsc orbitalElementsNew) {
        entityChangeOrbitalSOIs(spacecraftBody, getPlanet(newParentID), orbitalElementsNew);
    }

    @PhysTickOnly
    public void entityChangeOrbitalSOIs(EntityOrbitBody<?> spacecraftBody, CelestialBody newOrbitPlanet, OrbitalElementsc orbitalElementsNew) {
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
            if (orbitalDataNew instanceof EntityOrbitBody<?> entitySpacecraftBody) {
                this.getAllSpacecraftBodies().computeIfAbsent(entitySpacecraftBody.getOrbitId(), id -> {
                    entitySpacecraftBody.removeParent();
                    newOrbitPlanet.addChildBody(entitySpacecraftBody);
                    entitySpacecraftBody.init();
                    if (entitySpacecraftBody instanceof AbstractSpaceshipBody spaceshipBody) {
                        this.allVSships.put(entitySpacecraftBody.getOrbitId().getShipID(), spaceshipBody);
                    }
                    return entitySpacecraftBody;
                });
            }
        } else {
            PlanetShine.logError("EntityBody tried to join non-existent CelestialBody, ignoring Entity");
        }
    }

    @GameTickOnly
    public void entityRemoveOrbital(EntityOrbitBody<?> entityOrbitBody, boolean isTeleporting) {
        this.allSpacecraftBodies.remove(entityOrbitBody.getOrbitId());
        if (entityOrbitBody instanceof AbstractSpaceshipBody) {
            this.allVSships.remove(entityOrbitBody.getOrbitId().getShipID());
        }

        entityOrbitBody.OnRemove();
        entityOrbitBody.removeParent();
        entityOrbitBody.removeHostSpace(isTeleporting);
        entityOrbitBody.setOrbitalElements(null);
        entityOrbitBody.setIntercept(null);
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
    public ConcurrentMap<Long, AbstractSpaceshipBody> getAllVSships() {
        return this.allVSships;
    }

    public List<CelestialBody> getAllPlanetOrbitsList() {
        return allPlanetaryBodies.values().stream().toList();
    }

    public List<EntityOrbitBody<?>> getAllEntitiesOrbitsList() {
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

    public @Nullable AbstractSpaceshipBody getSpaceshipFromVSId(long id) {
        return this.allVSships.get(id);
    }

    @PhysTickOnly
    public void OnPhysTick(PhysLevel world) {
        for (CelestialBody celestialBody : this.getAllPlanetaryBodies().values()) {
            celestialBody.physTick(this, world);
        }
    }

    @GameTickOnly
    public void onServerTick(SpaceServerLevel spaceLevel) {
        for (CelestialBody celestialBody : this.getAllPlanetaryBodies().values()) {
            celestialBody.serverTick(this, spaceLevel);
        }
    }
}
