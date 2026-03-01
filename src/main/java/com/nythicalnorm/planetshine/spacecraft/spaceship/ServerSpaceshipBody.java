package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSpaceshipBody extends AbstractSpaceshipBody {
    public ServerSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, false);
        velocityApplyQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void init() {
        if (this.getShip() != null) {
            return;
        }
        ServerShipWorld serverShipWorld = ValkyrienSkies.api().getServerShipWorld(PSServer.get().getMCServer());
        if (serverShipWorld != null) {
            this.setShip(serverShipWorld.getLoadedShips().getById(this.id.getShipID()));
        }
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return this.orbitHostSpace.get();
    }
}
