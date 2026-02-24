package com.nythicalnorm.planetshine.solarsystem.bodies;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class CelestialBody extends OrbitalBody {
    protected final String name;
    protected final double radius;
    protected final double mass;
    protected final Quaternionf rotation;
    protected final PlanetAtmosphere atmosphericEffects;
    protected final @Nullable ResourceKey<Level> dimension;
    protected final Map<OrbitId, CelestialBody> childCelestialBodies;
    protected final ConcurrentMap<OrbitId, EntityOrbitBody> childEntityBodies;

    //calculated on load
    private double SOI;

    public CelestialBody(String name, double radius, double mass, Quaternionf rotation, PlanetAtmosphere atmosphericEffects, @Nullable ResourceKey<Level> dimension, Builder<?> bodyBuilder) {
        super(bodyBuilder);
        this.name = name;
        this.displayName = Component.translatable(String.format("planetshine.planets.%s", name));
        this.radius = radius;
        this.mass = mass;
        this.rotation = rotation;
        this.atmosphericEffects = atmosphericEffects;
        this.dimension = dimension;
        this.childEntityBodies = new ConcurrentHashMap<>();
        this.childCelestialBodies = new Object2ObjectOpenHashMap<>();
    }

    public String getName() {
        return name;
    }

    public @Nullable ResourceKey<Level> getDimension() {
        return dimension;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
    }

    protected void simulate(long TimeElapsed, Vector3dc parentPos) {
        if (orbitalElements != null) {
            Vector3d[] stateVectors = orbitalElements.ToCartesian(TimeElapsed);
            this.relativeOrbitalPos.set(stateVectors[0]);
            this.relativeVelocity.set(stateVectors[1]);

            this.absoluteOrbitalPos.set(parentPos).add(relativeOrbitalPos);
        }
    }

    @Override
    public void simulatePropagate(long TimeElapsed, Vector3dc parentPos, boolean isTimeWarping) {
        simulate(TimeElapsed, parentPos);

        childCelestialBodies.values().forEach((celestialBody ->
                celestialBody.simulatePropagate(TimeElapsed, absoluteOrbitalPos, isTimeWarping)));

        childEntityBodies.values().forEach((entityOrbitBody ->
                entityOrbitBody.simulatePropagate(TimeElapsed, absoluteOrbitalPos, isTimeWarping)));
    }

    public void addChildPlanet(CelestialBody celestialBody) {
        celestialBody.setParent(this);
        childCelestialBodies.put(celestialBody.getOrbitId(), celestialBody);
    }

    public void addChildBody(EntityOrbitBody entityOrbitBody) {
        entityOrbitBody.setParent(this);
        this.childEntityBodies.put(entityOrbitBody.getOrbitId(), entityOrbitBody);
    }

    public boolean hasChild(OrbitalBody body) {
        if (body instanceof EntityOrbitBody entityOrbitBody) {
            return childEntityBodies.containsValue(entityOrbitBody);
        } else if (body instanceof CelestialBody celestialBody){
            return childCelestialBodies.containsValue(celestialBody);
        }
        return false;
    }

    public void removeChild(OrbitId oldAddress) {
        this.childEntityBodies.remove(oldAddress);
        this.childCelestialBodies.remove(oldAddress);
    }

    public Collection<CelestialBody> getPlanetChildren() {
        if (childCelestialBodies != null) {
            return childCelestialBodies.values();
        }
        return null;
    }

    public Collection<EntityOrbitBody> getEntityChildren() {
        if (childEntityBodies != null) {
            return childEntityBodies.values();
        }
        return null;
    }

    public double getRadius(){
        return radius;
    }

    public PlanetAtmosphere getAtmosphere() {
        return atmosphericEffects;
    }

    public double getAccelerationDueToGravity() {
        double val = OrbitalElements.UniversalGravitationalConstant*this.mass;
        return val/(radius*radius);
    }

    public double getEntityAccelerationDueToGravity() {
        return getAccelerationDueToGravity() * 0.1d * 0.08d;
    }

    public double getSphereOfInfluence() {
        return SOI;
    }

    public void setSphereOfInfluence(double SOI) {
        this.SOI = SOI;
    }

    public double getAtmosphereRadius() {
        if (!this.atmosphericEffects.hasAtmosphere()) {
            return 0;
        }
        return this.atmosphericEffects.getAtmosphereHeight() + this.radius;
    }

    public double getMass() {
        return this.mass;
    }

    protected void initCalcs() {
        for (CelestialBody orbitBody : childCelestialBodies.values()) {
            if (orbitBody.getOrbitalElements() != null) {
                orbitBody.getOrbitalElements().initCalcs(this.mass);

                double soi = Math.pow(orbitBody.mass/this.mass, 0.4d);
                soi = soi * orbitBody.getOrbitalElements().getSemiMajorAxis();
                orbitBody.setSphereOfInfluence(soi);
                orbitBody.initCalcs();

                if (orbitBody instanceof ServerCelestialBody serverCelestialBody) {
                    serverCelestialBody.initServerPlanet();
                }
            }
        }
    }
}
