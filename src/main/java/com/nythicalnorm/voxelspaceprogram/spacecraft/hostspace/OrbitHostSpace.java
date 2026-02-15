package com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace;

import com.nythicalnorm.voxelspaceprogram.PSServer;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class OrbitHostSpace {
    protected final OrbitId orbitIdOfHost;
    protected final Vector3d originPos;
    protected final EntityOrbitBody hostBody;
    protected final List<Entity> nonHostEntities;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        nonHostEntities = new ArrayList<>();
        this.hostBody = entityOrbitBody;
    }

    public void OnGameTick() {

    }

    public Vector3d getOriginPos() {
        return originPos;
    }

    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    // This is overrided for different behaviours in PlayerHostSpace & ShipHostSpace
    public void applyHostVelocity(Vector3d addedVel) {
        if (!PSServer.get().isTimeWarping()) {
            hostBody.addVelocityForUpdate(addedVel);
        }
    }
}
