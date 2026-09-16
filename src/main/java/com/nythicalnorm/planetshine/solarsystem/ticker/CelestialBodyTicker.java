package com.nythicalnorm.planetshine.solarsystem.ticker;

import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import org.valkyrienskies.core.api.world.PhysLevel;

public interface CelestialBodyTicker {
    default void onPhysTick(CelestialBody celestialBody, SolarSystem solarSystem, PhysLevel physLevel) {

    }
    default void onServerTick(CelestialBody celestialBody, SolarSystem solarSystem, SpaceServerLevel spaceLevel) {

    }
}
