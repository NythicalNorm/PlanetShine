package com.nythicalnorm.planetshine.solarsystem.bodies;

import com.google.common.collect.ImmutableList;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.ticker.CelestialBodyTicker;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

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
    protected final ConcurrentMap<OrbitId, EntityOrbitBody<?>> childEntityBodies;
    protected SolarSystem solarSystem;

    //ClientSide
    @OnlyIn(Dist.CLIENT)
    protected @Nullable ResourceLocation mainTexture;

    //ServerSide
    protected ServerCelestialData serverCelestialData;
    protected ImmutableList<CelestialBodyTicker> celestialBodyTickers;

    //calculated on load i.e not serialized or networked
    protected double SOI;
    protected double minInterceptDistance; // basically the minimum distance from the parent body that someone has to be in this body's SOI;
    protected double maxInterceptDistance; // same for the maximum distance

    public CelestialBody(String name, double radius, double mass, Quaternionf rotation, PlanetAtmosphere atmosphericEffects,
                         @Nullable ResourceKey<Level> dimension, Builder<?> bodyBuilder, @Nullable ImmutableList<CelestialBodyTicker> celestialBodyTickers,
                         boolean isClientSide) {
        super(bodyBuilder, isClientSide);
        this.name = name;
        this.displayName = Component.translatable(String.format("planetshine.planets.%s", name));
        this.radius = radius;
        this.mass = mass;
        this.rotation = rotation;
        this.atmosphericEffects = atmosphericEffects;
        this.dimension = dimension;
        this.childEntityBodies = new ConcurrentHashMap<>();
        this.childCelestialBodies = new Object2ObjectOpenHashMap<>();

        if (!isClientSide) {
            this.serverCelestialData = new ServerCelestialData();
            this.celestialBodyTickers = celestialBodyTickers;
        }
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

    @OnlyIn(Dist.CLIENT)
    public @Nullable ResourceLocation getMainTexture() {
        return mainTexture;
    }

    @OnlyIn(Dist.CLIENT)
    public void setMainTexture(@Nullable ResourceLocation mainTexture) {
        this.mainTexture = mainTexture;
    }

    //Server side Only
    public ServerCelestialData getCelestialServerData() {
        return serverCelestialData;
    }

    public void setRotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
    }

    protected void simulate(long TimeElapsed, Vector3dc parentPos) {
        if (orbitalElements != null) {
            this.orbitalElements.ToCartesian(TimeElapsed, this.relativeOrbitalPos, this.relativeVelocity);
            this.absoluteOrbitalPos.set(parentPos).add(relativeOrbitalPos);
        }
    }

    public void simulatePropagate(long TimeElapsed, Vector3dc parentPos, boolean isTimeWarping) {
        simulate(TimeElapsed, parentPos);

        childCelestialBodies.values().forEach((celestialBody ->
                celestialBody.simulatePropagate(TimeElapsed, this.absoluteOrbitalPos, isTimeWarping)));
    }

    public void simulateSpacecraft(long currentTime, boolean timeWarping) {
        this.childEntityBodies.values().forEach((entityOrbitBody ->
                entityOrbitBody.simulate(currentTime, timeWarping)));
    }

    public void addChildPlanet(CelestialBody celestialBody) {
        celestialBody.setParent(this);
        childCelestialBodies.put(celestialBody.getOrbitId(), celestialBody);
    }

    public @Nullable CelestialBody getPlanetChild(OrbitId orbitId) {
        return this.childCelestialBodies.get(orbitId);
    }

    public void addChildBody(EntityOrbitBody<?> entityOrbitBody) {
        entityOrbitBody.setParent(this);
        this.childEntityBodies.put(entityOrbitBody.getOrbitId(), entityOrbitBody);
    }

    public boolean hasChild(OrbitalBody body) {
        if (body instanceof EntityOrbitBody<?> entityOrbitBody) {
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

    public Collection<EntityOrbitBody<?>> getEntityChildren() {
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

    public double getAtmosphereRadius() {
        if (!this.atmosphericEffects.hasAtmosphere()) {
            return 0;
        }
        return this.atmosphericEffects.getAtmosphereHeight() + this.radius;
    }

    public double getMass() {
        return this.mass;
    }

    public double getMinInterceptDistance() {
        return minInterceptDistance;
    }

    public double getMaxInterceptDistance() {
        return maxInterceptDistance;
    }

    public SolarSystem getSolarSystem() { // this is kinda cursed.
        return solarSystem;
    }

    protected void initCalcs(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
        for (CelestialBody orbitBody : childCelestialBodies.values()) {
            if (orbitBody.getOrbitalElements() != null) {
                orbitBody.getOrbitalElements().initCalcs(this.mass);

                double divMass = Math.pow(orbitBody.mass/this.mass, 0.4d);
                orbitBody.SOI = divMass * orbitBody.getOrbitalElements().getSemiMajorAxis();

                orbitBody.minInterceptDistance = orbitBody.getOrbitalElements().getPeriapsis() - orbitBody.SOI;
                orbitBody.maxInterceptDistance = orbitBody.getOrbitalElements().getApoapsis() + orbitBody.SOI;

                orbitBody.initCalcs(solarSystem);
            }
        }
    }

    @GameTickOnly
    public void serverTick(SolarSystem solarSystem, SpaceServerLevel spaceLevel) {
        this.celestialBodyTickers.forEach(ticker -> ticker.onServerTick(this, solarSystem, spaceLevel));
    }

    @PhysTickOnly
    public void physTick(SolarSystem solarSystem) {
        this.celestialBodyTickers.forEach(ticker -> ticker.onPhysTick(this, solarSystem));
    }
}
