package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.google.common.collect.ImmutableList;
import com.nythicalnorm.planetshine.solarsystem.*;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.solarsystem.ticker.CelestialBodyTicker;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class PlanetaryBody extends CelestialBody {
    protected final Vector3f NorthPoleDir;
    protected final float NorthPoleInitialAngle;
    protected final long RotationPeriod;
    protected final PlanetDimensionProperties dimensionalProperties;

    public PlanetaryBody(PlanetBuilder planetBuilder, boolean isClientSide) {
        super(planetBuilder.name, planetBuilder.radius, planetBuilder.mass, planetBuilder.rotation,
                planetBuilder.atmosphericEffects, planetBuilder.dimension, planetBuilder,
                ImmutableList.copyOf(planetBuilder.celestialBodyTickers), isClientSide);
        this.NorthPoleDir = new Vector3f(planetBuilder.NorthPoleDir.x, planetBuilder.NorthPoleDir.y, planetBuilder.NorthPoleDir.z);
        this.NorthPoleInitialAngle = planetBuilder.NorthPoleDir.angle;
        this.RotationPeriod = planetBuilder.RotationPeriod;
        this.dimensionalProperties = planetBuilder.dimensionalProperties;
    }

    @Override
    public RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>>> getType() {
        return OrbitalBodyTypeRegistry.PLANETARY_BODY;
    }

    @Override
    protected void simulate(long TimeElapsed, Vector3dc parentPos) {
        super.simulate(TimeElapsed, parentPos);

        float rotationAngle = NorthPoleInitialAngle + (float)((2*Math.PI/RotationPeriod) * (TimeElapsed % RotationPeriod));
        this.rotation.identity().rotationTo(NorthPoleDir.x,NorthPoleDir.y,NorthPoleDir.z, 0f, 1f, 0f);
        Quaternionf rotated = new Quaternionf(new AxisAngle4f(rotationAngle, 0f, 1f, 0f));
        this.rotation.mul(rotated);
    }

    public long getRotationPeriod() {
        return RotationPeriod;
    }

    public PlanetDimensionProperties getDimensionalProperties() {
        return dimensionalProperties;
    }

    public double getRotationPeriodInSeconds() {
        return TimeCalc.timeLongToDouble(RotationPeriod);
    }

    public AxisAngle4f getNorthPoleAxisAngle() {
        return new AxisAngle4f(NorthPoleInitialAngle, NorthPoleDir);
    }

    public Vector3fc getNorthPoleDir() {
        return NorthPoleDir;
    }

    public static class PlanetBuilder extends OrbitalBody.Builder<PlanetaryBody> {
        private String name;
        private double radius = 1000;
        private double mass = 10E24;
        protected Quaternionf rotation = new Quaternionf();
        private AxisAngle4f NorthPoleDir = new AxisAngle4f();
        private long RotationPeriod = 0L;
        private PlanetAtmosphere atmosphericEffects = new PlanetAtmosphere(false, 0, 0, 0, 0,0.0f, 1.0f, 1.0f);
        private @Nullable ResourceKey<Level> dimension = null;
        private final List<CelestialBodyTicker> celestialBodyTickers;
        private PlanetDimensionProperties dimensionalProperties = new PlanetDimensionProperties(true, false, 0, false, false);

        public PlanetBuilder() {
            this.celestialBodyTickers = new ArrayList<>();
        }

        public void setName(String name) {
            this.name = name.toLowerCase().trim();
            this.setId(OrbitId.getIdFromString(name));

            Component planetDisplayName = Component.translatable("planetshine.planets." + name);
            this.setDisplayName(planetDisplayName);
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        public void setRotation(Quaternionf rotation) {
            this.rotation.set(rotation);
        }

        public void setNorthPoleDir(AxisAngle4f northPoleDir) {
            this.NorthPoleDir = northPoleDir;
        }

        public void setNorthPoleDir(float rightAscension, float declination, float startingRotation) {
            rightAscension = (float) Math.toRadians(rightAscension);
            declination = (float) Math.toRadians(declination);

            Vector3f normalizedNorthPoleDir = new Vector3f();
            normalizedNorthPoleDir.x = Mth.sin(declination) * Mth.cos(rightAscension);
            normalizedNorthPoleDir.y = Mth.cos(declination);
            normalizedNorthPoleDir.z = Mth.sin(declination) * Mth.sin(rightAscension);
            NorthPoleDir = new AxisAngle4f(startingRotation, normalizedNorthPoleDir);
        }

        public void setRotationPeriod(long rotationPeriod) {
            RotationPeriod = rotationPeriod;
        }

        public void setAtmosphericEffects(PlanetAtmosphere atmosphericEffects) {
            this.atmosphericEffects = atmosphericEffects;
        }

        public void setDimensionalPrperties(PlanetDimensionProperties planetDimensionProperties) {
            this.dimensionalProperties = planetDimensionProperties;
        }

        public void addCelestialBodyTicker(CelestialBodyTicker starHeaterTicker) {
            this.celestialBodyTickers.add(starHeaterTicker);
        }

        public void setDimension(@Nullable ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }

        @Override
        public PlanetaryBody build() {
            return new PlanetaryBody(this, false);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public PlanetaryBody buildClientSide() {
            return new PlanetaryBody(this, true);
        }
    }
}
