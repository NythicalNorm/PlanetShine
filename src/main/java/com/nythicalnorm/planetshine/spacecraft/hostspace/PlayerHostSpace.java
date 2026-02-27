package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import org.joml.Vector2ic;

public class PlayerHostSpace extends OrbitHostSpace {

    public PlayerHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
    }
}
