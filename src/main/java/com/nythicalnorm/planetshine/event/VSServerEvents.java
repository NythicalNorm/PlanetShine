package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.util.SpaceUtils;
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
           PSServer psServer = PSServer.get();
           if (event.getWorld().getDimension().equals(SpaceUtils.getSpaceLevelString())) {
               psServer.OnPhysTick(event.getDelta(), event.getWorld());
           }
       }
    }

    private static void onTickEnd(TickEndEvent event){
    }

    private static void onShipLoadEvent(ShipLoadEvent shipLoadEvent) {
        if (PSServer.get() != null) {
            PSServer.get().getHostSpaceManager().shipAddedToSpace(shipLoadEvent.getShip());
        }
    }
}