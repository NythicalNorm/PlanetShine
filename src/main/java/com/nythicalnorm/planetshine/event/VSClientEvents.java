package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ClientSpaceshipBody;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.valkyrienskies.core.api.events.ShipLoadEventClient;
import org.valkyrienskies.core.api.events.ShipUnloadEventClient;
import org.valkyrienskies.mod.api.ValkyrienSkies;

@OnlyIn(Dist.CLIENT)
public class VSClientEvents {
    public static void addListeners() {
        ValkyrienSkies.api().getShipLoadEventClient().on(VSClientEvents::onClientShipLoad);
        ValkyrienSkies.api().getShipUnloadEventClient().on(VSClientEvents::onClientShipUnload);
    }

    private static void onClientShipLoad(ShipLoadEventClient event) {
        if (PSClient.get() != null) {
            ClientSpaceshipBody clientSpaceshipBody = (ClientSpaceshipBody) PSClient.get().getSolarSystem().getSpaceshipFromVSId(event.getShip().getId());
            if (clientSpaceshipBody != null) {
                clientSpaceshipBody.setBody(event.getShip());
            }
        }
    }

    private static void onClientShipUnload(ShipUnloadEventClient event) {
        if (PSClient.get() != null) {
            ClientSpaceshipBody clientSpaceshipBody = (ClientSpaceshipBody) PSClient.get().getSolarSystem().getSpaceshipFromVSId(event.getShip().getId());
            if (clientSpaceshipBody != null) {
                clientSpaceshipBody.setBody(null);
            }
        }
    }
}
