package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import net.minecraft.client.Minecraft;
import org.valkyrienskies.core.api.world.ClientShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class ClientSpaceshipBody extends AbstractSpaceshipBody {
    public ClientSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, true);
    }

    @Override
    public void init() {
        super.init();
        if (this.getBody() != null) {
            return;
        }
        ClientShipWorld clientShipWorld = ValkyrienSkies.api().getClientShipWorld(Minecraft.getInstance());
        if (clientShipWorld != null) {
            this.setBody(clientShipWorld.getLoadedShips().getById(this.id.getShipID()));
        }
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return PSClient.get().getCurrentHostSpace();
    }
}
