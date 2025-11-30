package com.nythicalnorm.nythicalSpaceProgram.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NythicalSpaceProgram.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityGravityHandler {
    private static final UUID gravityUUID = UUID.fromString("a13033dd-12dc-456f-901f-54c63734ac71");

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent event) {
        float fallDistance = event.getDistance();
        Optional<Double> planetAcceleration = PlanetDimensions.getAccelerationDueToGravityAt(event.getEntity().level().dimension());
        if (planetAcceleration.isPresent()) {
            if (planetAcceleration.get() <= 0){
                event.setCanceled(true);
            }
            double multfactor = ForgeMod.ENTITY_GRAVITY.get().getDefaultValue() / planetAcceleration.get();
            event.setDistance(fallDistance/(float) multfactor);
        }
    }

    @SubscribeEvent // on the mod event bus
    public static void createDefaultAttributes(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            AttributeMap entityAttributes = ((LivingEntity)entity).getAttributes();
            Optional<Double> levelGravity = PlanetDimensions.getAccelerationDueToGravityAt(entity.level().dimension());
            double tempGravity = 0;
            if (levelGravity.isPresent()) {
                tempGravity = levelGravity.get();
            }

            AttributeModifier gravityModifier = new AttributeModifier(gravityUUID, "NythicalSpaceProgram.PlanetGravity",
                    tempGravity - ForgeMod.ENTITY_GRAVITY.get().getDefaultValue(), AttributeModifier.Operation.ADDITION); // Add -0;

            if (entityAttributes.hasAttribute(ForgeMod.ENTITY_GRAVITY.get())) {
                if (entityAttributes.hasModifier(ForgeMod.ENTITY_GRAVITY.get(), gravityUUID)) {
                    Multimap<Attribute, AttributeModifier> ogModifier = ArrayListMultimap.create();
                    ogModifier.put(ForgeMod.ENTITY_GRAVITY.get(), gravityModifier);
                    entityAttributes.removeAttributeModifiers(ogModifier);
                }
                if (levelGravity.isPresent()) {
                    entityAttributes.getInstance(ForgeMod.ENTITY_GRAVITY.get()).addTransientModifier(gravityModifier);
                }
            }
        }
    }
}
