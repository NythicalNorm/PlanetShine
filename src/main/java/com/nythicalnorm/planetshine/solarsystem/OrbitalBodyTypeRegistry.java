package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBodyCodec;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBodyCodec;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.*;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.SpaceshipBodyCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

public class OrbitalBodyTypeRegistry {
    public static final ResourceKey<Registry<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>>> ORBITAL_BODY_REGISTRY_KEY =
            ResourceKey.createRegistryKey(PlanetShine.rl("orbital_bodies"));

    public static final DeferredRegister<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> ORBITAL_BODY_TYPES
            = DeferredRegister.create(ORBITAL_BODY_REGISTRY_KEY, PlanetShine.MODID);

    public static final RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> PLANETARY_BODY =
            ORBITAL_BODY_TYPES.register("planet",
            () -> new OrbitalBodyType<>(new PlanetaryBodyCodec(), PlanetaryBody.PlanetBuilder::new));

    public static final RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> STAR_BODY =
            ORBITAL_BODY_TYPES.register("star",
                    () -> new OrbitalBodyType<>(new StarBodyCodec(), StarBody.StarBuilder::new));

    public static final RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> PLAYER_ORBITAL_BODY =
            ORBITAL_BODY_TYPES.register("player",
                    () -> new OrbitalBodyType<>(new PlayerOrbitCodec(), AbstractPlayerOrbitBody.PlayerOrbitBuilder::new));

    public static final RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> SPACESHIP_BODY =
            ORBITAL_BODY_TYPES.register("vs_ship",
                    () -> new OrbitalBodyType<>(new SpaceshipBodyCodec(), AbstractSpaceshipBody.ShipOrbitBuilder::new));

    public static final RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>>> IRL_SPACECRAFT_BODY =
            ORBITAL_BODY_TYPES.register("irl_ship",
                    () -> new OrbitalBodyType<>(new IRLSpacecraftCodec(), AbstractIrlSpacecraft.IRLSpacecraftBuilder::new));


    public static OrbitalBodyType <? extends OrbitalBody, ? extends OrbitalBody.Builder<?>> getType(ResourceLocation resourceLocation) {
        return RegistryManager.ACTIVE.getRegistry(ORBITAL_BODY_REGISTRY_KEY).getValue(resourceLocation);
    }
}
