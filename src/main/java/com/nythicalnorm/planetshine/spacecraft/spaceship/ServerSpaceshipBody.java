package com.nythicalnorm.planetshine.spacecraft.spaceship;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSpaceshipBody extends AbstractSpaceshipBody {
    public ServerSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, false);
        velocityApplyQueue = new ConcurrentLinkedQueue<>();
    }
}
