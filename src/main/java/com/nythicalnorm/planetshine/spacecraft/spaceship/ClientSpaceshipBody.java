package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;

public class ClientSpaceshipBody extends AbstractSpaceshipBody {
    public ClientSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, true);
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return PSClient.get().getCurrentHostSpace();
    }
}
