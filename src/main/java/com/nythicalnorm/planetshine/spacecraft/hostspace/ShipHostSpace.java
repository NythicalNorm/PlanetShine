package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import org.joml.Vector2ic;

public class ShipHostSpace extends OrbitHostSpace{
    public ShipHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
    }
}
