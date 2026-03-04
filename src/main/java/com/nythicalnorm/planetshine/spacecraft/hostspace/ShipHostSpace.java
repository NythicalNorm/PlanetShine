package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import org.joml.Vector2ic;
import org.joml.Vector3dc;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ShipHostSpace extends OrbitHostSpace{
    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastPhysTick;
    protected final ConcurrentLinkedQueue<ServerSpaceshipBody> nonHostShips;

    public ShipHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
        this.velocityForLastPhysTick = new ConcurrentLinkedQueue<>();
        this.nonHostShips = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onPhysTick() {
        velocityForLastPhysTick.clear();
    }

    public void addShipToHostSpace(ServerSpaceshipBody ship) {
        nonHostShips.add(ship);
    }

    @Override
    public void applyHostVelocity(Vector3dc addedVel) {
        super.applyHostVelocity(addedVel);
        this.velocityForLastPhysTick.add(addedVel);
    }

    @Override
    public void changeSOI(OrbitId newParent, OrbitalElements orbitalElements) {
        super.changeSOI(newParent, orbitalElements);
        SolarSystem solarSystem = PSServer.get().getSolarSystem();

        this.nonHostShips.forEach(spaceshipBody -> {
            solarSystem.entityChangeOrbitalSOIs(spaceshipBody, newParent, orbitalElements);
            PacketHandler.sendToAllClients(new ClientboundOrbitSOIChange(spaceshipBody.getOrbitId(), newParent, orbitalElements));
        });
    }
}
