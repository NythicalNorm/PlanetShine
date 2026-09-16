package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import org.joml.Vector3d;

public interface OrbitHostAccessor {
    OrbitId getOrbitIdOfHost();
    Vector3d getOriginPos();
    EntityOrbitBody<?> getHostBody();

    boolean isUnloadedHostSpace();
}
