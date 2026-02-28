package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerPlayerOrbitBody extends AbstractPlayerOrbitBody {
    public ServerPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, false);
        this.velocityApplyQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean isPlayerLoggedIn() {
        return this.player != null;
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return this.orbitHostSpace.get();
    }
}
