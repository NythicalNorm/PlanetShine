package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.PhysLevel;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ShipHostSpace extends OrbitHostSpace {
    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastPhysTick;
    protected final ConcurrentLinkedQueue<ServerSpaceshipBody> nonHostShips;

    public ShipHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody<?> entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
        this.velocityForLastPhysTick = new ConcurrentLinkedQueue<>();
        this.nonHostShips = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onPhysTick(PhysLevel world) {
        velocityForLastPhysTick.clear();
    }

    @Override
    public void addShipToHostSpace(ServerSpaceshipBody ship) {
        if (ship != null) {
            ship.setHostOrbitSpace(this);
            if (! this.hostBody.getOrbitId().equals(ship.getOrbitId())) {
                this.nonHostShips.add(ship);
            }
        }
    }

    @Override
    public void applyHostVelocity(Vector3dc addedVel) {
        super.applyHostVelocity(addedVel);
        this.velocityForLastPhysTick.add(addedVel);
    }

    @Override
    public void changeSOI(OrbitId newParent, OrbitalElementsc orbitalElements) {
        super.changeSOI(newParent, orbitalElements);
        SolarSystem solarSystem = PSServer.get().getSolarSystem();

        this.nonHostShips.forEach(spaceshipBody -> {
            OrbitalElements nonHostOrbit = new OrbitalElements(orbitalElements);
            solarSystem.entityChangeOrbitalSOIs(spaceshipBody, newParent, nonHostOrbit);
            PacketHandler.sendToAllClients(new ClientboundOrbitSOIChange(spaceshipBody.getOrbitId(), newParent, nonHostOrbit));
        });
    }

    @Override
    public void removeOrbitBody(EntityOrbitBody<?> entityOrbitBody, boolean isTeleporting) {
        super.removeOrbitBody(entityOrbitBody, isTeleporting);
        if (entityOrbitBody instanceof ServerSpaceshipBody spaceshipBody) {
            this.nonHostShips.remove(spaceshipBody);
        }
    }

    @Override
    public @Nullable EntityOrbitBody<?> findNewHost() {
        if (!this.nonHostShips.isEmpty()) {
            EntityOrbitBody<?> newHost = null;

            int biggestVolumeFound = 0;

            for (ServerSpaceshipBody spaceshipBody : this.nonHostShips) {
                if (spaceshipBody.isBodyEntityLoaded()) {
                    int shipVolume = MiscCalc.getShipVolume(spaceshipBody.getBody());
                    if (shipVolume > biggestVolumeFound) {
                        newHost = spaceshipBody;
                    }
                }
            }

            return newHost;
        }
        else {
            return super.findNewHost();
        }
    }

    @Override
    public void handleHostSpaceHandover(EntityOrbitBody<?> orbitBody, OrbitHostSpace newHost) {
        Vector3dc originDifference = getHostPosDifference(this, newHost, orbitBody);
        ShipTeleporter shipTeleporter = PSServer.get().getHostSpaceManager().getShipTeleporter();

        this.nonHostShips.forEach(serverSpaceshipBody -> {
            Ship ship = serverSpaceshipBody.getBody();

            if (!serverSpaceshipBody.isBodyEntityLoaded()) {// This is a part where we chack if it has joints connected somewhere i think, && !player.isPassenger()) {
                Vector3d newPos = new Vector3d(ship.getTransform().getPosition().x() + originDifference.x(),
                        ship.getTransform().getPosition().y() + originDifference.y(),
                        ship.getTransform().getPosition().z() + originDifference.z());

                shipTeleporter.teleportShipInSpaceDim(ship, newPos, serverSpaceshipBody.isHostOfItsSpace());
            }
            newHost.addShipToHostSpace(serverSpaceshipBody);
        });
        super.handleHostSpaceHandover(orbitBody, newHost);
    }
}