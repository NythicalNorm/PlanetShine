package com.nythicalnorm.planetshine.network;

import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundStateVectorChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import org.joml.Vector3dc;

import java.util.concurrent.ConcurrentHashMap;

public class OrbitalBodyUpdater {
    private final ConcurrentHashMap<OrbitId, Runnable> updatesToSend;

    public OrbitalBodyUpdater() {
        updatesToSend = new ConcurrentHashMap<>();
    }

    public void sendUpdates() {
        for (Runnable runnable : this.updatesToSend.values()) {
            runnable.run();
        }

        this.updatesToSend.clear();
    }

    public void addOrbitalUpdate(OrbitId id, OrbitalElementsc orbitalElements) {
        this.updatesToSend.put(id, () -> PacketHandler.sendToAllClients(new ClientboundOrbitChange(id, orbitalElements)));
    }

    public void addStateVectorUpdate(OrbitId id, Vector3dc relativeOrbitalPos, Vector3dc relativeVelocity) {
        this.updatesToSend.put(id, () -> PacketHandler.sendToAllClients(new ClientboundStateVectorChange(id, relativeOrbitalPos, relativeVelocity)));
    }
}
