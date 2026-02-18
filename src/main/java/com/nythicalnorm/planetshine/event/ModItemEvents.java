package com.nythicalnorm.planetshine.event;

import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PlanetShine.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModItemEvents {

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent event) {
        if (!ServerPlayer.class.isAssignableFrom(event.getEntity().getClass())) {
            return;
        } else {
            if (event.getEntity().isUsingItem() && event.getEntity().getUseItem().getItem() == PSItems.HANDHELD_THRUSTER.get()) {
                Vec3 force = event.getEntity().getLookAngle().normalize();
                force = force.scale(0.05d);
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().add(force));
                ((ServerPlayer) event.getEntity()).connection.send(new ClientboundSetEntityMotionPacket(event.getEntity()));
            }
        }
    }
}
