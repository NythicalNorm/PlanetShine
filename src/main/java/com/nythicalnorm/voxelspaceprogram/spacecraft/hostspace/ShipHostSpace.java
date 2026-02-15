package com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import org.joml.Vector3d;

public class ShipHostSpace extends OrbitHostSpace{
    public ShipHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
    }
}
