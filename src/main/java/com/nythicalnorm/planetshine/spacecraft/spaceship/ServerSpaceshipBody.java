package com.nythicalnorm.planetshine.spacecraft.spaceship;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ServerSpaceshipBody extends AbstractSpaceshipBody {
    public ServerSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, false);
        velocityApplyQueue = new ConcurrentLinkedDeque<>();
    }
}
