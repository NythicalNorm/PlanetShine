package com.nythicalnorm.planetshine.solarsystem.ticker;

import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;

public class PlanetAtmosphereTicker implements CelestialBodyTicker {
    private final double atmosphereHeight;

    public PlanetAtmosphereTicker(double atmosphereHeight) {
        this.atmosphereHeight = atmosphereHeight;
    }

    @Override
    public void onServerTick(CelestialBody celestialBody, SolarSystem solarSystem, SpaceServerLevel spaceLevel) {

    }

    @Override
    public void onPhysTick(CelestialBody celestialBody, SolarSystem solarSystem, PhysLevel physLevel) {
        if (celestialBody instanceof PlanetaryBody planetaryBody) {
            planetaryBody.getEntityChildren().forEach(entityOrbitBody -> {
                if (entityOrbitBody.getBody() instanceof LoadedServerShip loadedServerShip && entityOrbitBody.getAltitude() <= atmosphereHeight) {
                    Vector3d dragForce = AtmosphereCalc.getDragForce(planetaryBody, entityOrbitBody);
                    //entityOrbitBody.addVelocityForUpdate(dragForce.div(TimeCalc.PhysTickPerSec));
                    PhysShip physShip = physLevel.getShipById(loadedServerShip.getId());
                    if (physShip != null) {
                        physShip.applyWorldForce(dragForce, new Vector3d()); // physShip.getKinematics().getPosition());
                    }
                }
            });
        }
    }
}
