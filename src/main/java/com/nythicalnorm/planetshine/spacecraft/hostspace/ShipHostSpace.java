package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ShipHostSpace extends OrbitHostSpace{
    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastPhysTick;
    protected final ConcurrentLinkedQueue<ServerShip> nonHostShips;

    public ShipHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
        this.velocityForLastPhysTick = new ConcurrentLinkedQueue<>();
        this.nonHostShips = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onPhysTick() {
        velocityForLastPhysTick.clear();
    }

    public void addShipToHostSpace(LoadedServerShip ship) {
        nonHostShips.add(ship);
    }

    @Override
    public void applyHostVelocity(Vector3dc addedVel) {
        super.applyHostVelocity(addedVel);
        this.velocityForLastPhysTick.add(addedVel);
    }
}
