package com.nythicalnorm.planetshine.solarsystem.ticker;

import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.util.calculations.AtmosphereCalc;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
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
        if (celestialBody instanceof PlanetaryBody planetaryBody) {
            planetaryBody.getEntityChildren().forEach(entityOrbitBody -> {
                if (entityOrbitBody.getBody() instanceof ServerPlayer serverPlayer && entityOrbitBody.getAltitude() <= atmosphereHeight) {
                    Vector3d dragForce = AtmosphereCalc.getDragForce(planetaryBody, entityOrbitBody).div(20.0d);
                    dragForce.div(291.7d); // f = ma, value is mass of steve is this i guess

                    if (entityOrbitBody.isHostOfItsSpace()) {
                        entityOrbitBody.addVelocityForUpdate(dragForce);
                    } else {
                        serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().add(new Vec3(dragForce.x(), dragForce.y(), dragForce.z())));
                        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                    }
                }
            });
        }
    }

    @Override
    public void onPhysTick(CelestialBody celestialBody, SolarSystem solarSystem, PhysLevel physLevel) {
        if (celestialBody instanceof PlanetaryBody planetaryBody) {
            planetaryBody.getEntityChildren().forEach(entityOrbitBody -> {
                if (entityOrbitBody.getBody() instanceof LoadedServerShip loadedServerShip && entityOrbitBody.getAltitude() <= atmosphereHeight) {
                    Vector3d dragForce = AtmosphereCalc.getDragForce(planetaryBody, entityOrbitBody);
                    PhysShip physShip = physLevel.getShipById(loadedServerShip.getId());
                    if (physShip != null) {
                        physShip.applyWorldForce(dragForce, new Vector3d());
                    }
                }
            });
        }
    }
}
