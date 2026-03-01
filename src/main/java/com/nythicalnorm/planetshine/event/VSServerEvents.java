package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PSServer;
import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.events.ShipLoadEvent;
import org.valkyrienskies.core.api.events.TickEndEvent;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class VSServerEvents {
    public static void addListeners() {
        ValkyrienSkies.api().getPhysTickEvent().on(VSServerEvents::onPhysTick);
        ValkyrienSkies.api().getTickEndEvent().on(VSServerEvents::onTickEnd);
        ValkyrienSkies.api().getShipLoadEvent().on(VSServerEvents::onShipLoadEvent);
    }

    private static void onPhysTick(PhysTickEvent event) {
       if (PSServer.get() != null) {
           PSServer solarSys = PSServer.get();
           if (event.getWorld().getDimension().equals(solarSys.getSpaceLevelString())) {
               solarSys.OnPhysTick(event.getDelta());
           }
       }
    }

    private static void onTickEnd(TickEndEvent event){
    }

    private static void onShipLoadEvent(ShipLoadEvent shipLoadEvent) {
        if (PSServer.get() != null) {
            PSServer.get().onShipLoad(shipLoadEvent.getShip());
        }
    }
}