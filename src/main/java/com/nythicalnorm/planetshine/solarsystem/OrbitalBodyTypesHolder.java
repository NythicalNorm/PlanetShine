package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBodyCodec;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBodyCodec;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.*;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.SpaceshipBodyCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class OrbitalBodyTypesHolder {
    private static final Map<String, OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> AllCelestialBodyTypes = new Object2ObjectOpenHashMap<>();

    public static final OrbitalBodyType<PlanetaryBody, PlanetaryBody.PlanetBuilder> PLANETARY_BODY =
            registerOrbitalBody(new OrbitalBodyType<>("planet", new PlanetaryBodyCodec(), PlanetaryBody.PlanetBuilder::new));

    public static final OrbitalBodyType<StarBody, StarBody.StarBuilder> STAR_BODY =
            registerOrbitalBody(new OrbitalBodyType<>("star", new StarBodyCodec(), StarBody.StarBuilder::new));

    public static final OrbitalBodyType<AbstractPlayerOrbitBody, AbstractPlayerOrbitBody.PlayerOrbitBuilder> PLAYER_ORBITAL_BODY =
            registerOrbitalBody(new OrbitalBodyType<>("player", new PlayerOrbitCodec(), AbstractPlayerOrbitBody.PlayerOrbitBuilder::new));

    public static final OrbitalBodyType<AbstractSpaceshipBody, AbstractSpaceshipBody.ShipOrbitBuilder> SPACESHIP_BODY =
            registerOrbitalBody(new OrbitalBodyType<>("vs_ship", new SpaceshipBodyCodec(), AbstractSpaceshipBody.ShipOrbitBuilder::new));


    public static <T extends OrbitalBody, M extends OrbitalBody.Builder<T>> OrbitalBodyType<T, M> registerOrbitalBody(OrbitalBodyType<T, M> orbitalBodyType) {
        AllCelestialBodyTypes.put(orbitalBodyType.getTypeName(), orbitalBodyType);
        return orbitalBodyType;
    }

    public static OrbitalBodyType <? extends OrbitalBody, ? extends OrbitalBody.Builder<?>> getType(String name) {
        return AllCelestialBodyTypes.get(name);
    }

    public static String getOrbitalBodyTypeName(OrbitalBody orbitalBody) {
        return orbitalBody.getType().getTypeName();
    }
}
