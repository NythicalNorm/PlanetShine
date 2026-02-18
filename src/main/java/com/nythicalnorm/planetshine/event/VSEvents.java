package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.PSServer;
import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.events.TickEndEvent;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class VSEvents {
    public static void addListeners() {
        ValkyrienSkies.api().getPhysTickEvent().on(VSEvents::onPhysTick);
        ValkyrienSkies.api().getTickEndEvent().on(VSEvents::onTickEnd);
    }

    public static void onPhysTick(PhysTickEvent event) {
       if (PSServer.get() != null) {
           PSServer solarSys = PSServer.get();
           if (event.getWorld().getDimension().equals(solarSys.getSpaceLevelString())) {
               solarSys.OnPhysTick(event.getDelta());
           }
       }
    }

    public static void onTickEnd(TickEndEvent event){
    }
}
