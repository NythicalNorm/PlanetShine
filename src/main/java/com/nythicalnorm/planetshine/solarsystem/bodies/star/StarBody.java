package com.nythicalnorm.planetshine.solarsystem.bodies.star;

import com.google.common.collect.ImmutableList;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.solarsystem.ticker.StarHeaterTicker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class StarBody extends CelestialBody {
    public StarBody(StarBuilder starBuilder, boolean isClientSide) {
        super(starBuilder.name, starBuilder.radius, starBuilder.mass, starBuilder.rotation,
                starBuilder.atmosphericEffects, null, starBuilder,
                ImmutableList.of(new StarHeaterTicker(1e12d, 3.828e26)),
                isClientSide);
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.STAR_BODY;
    }

    public void simulatePlanets(long currentTime, boolean isTimeWarping) {
        this.simulatePropagate(currentTime, new Vector3d(0d, 0d, 0d), isTimeWarping);
    }

    @Override
    public void initCalcs(SolarSystem solarSystem) {
        this.SOI = Double.POSITIVE_INFINITY;
        this.parent = null;
        super.initCalcs(solarSystem);
    }

    public static class StarBuilder extends OrbitalBody.Builder<StarBody> {
        private String name;
        private double radius = 1000;
        private double mass = 10E24;
        protected Quaternionf rotation = new Quaternionf();
        private PlanetAtmosphere atmosphericEffects = new PlanetAtmosphere(false, 0, 0, 0, 0.0f, 1.0f, 1.0f);

        public StarBuilder() {

        }

        public void setName(String name) {
            this.name = name.toLowerCase().trim();
            this.setId(OrbitId.getIdFromString(name));
        }

        public void setRotation(Quaternionf rotation) {
            this.rotation.set(rotation);
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        public void setAtmosphericEffects(PlanetAtmosphere atmosphericEffects) {
            this.atmosphericEffects = atmosphericEffects;
        }

        @Override
        public StarBody build() {
            return new StarBody(this, false);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public StarBody buildClientSide() {
            return new StarBody(this, true);
        }
    }
}
