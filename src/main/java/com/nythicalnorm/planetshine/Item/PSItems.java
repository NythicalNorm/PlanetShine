package com.nythicalnorm.planetshine.Item;

import com.nythicalnorm.planetshine.Item.armor.PSArmorMaterial;
import com.nythicalnorm.planetshine.Item.custom.HandheldThrusterItem;
import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PSItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PlanetShine.MODID);

    public static final RegistryObject<Item> HANDHELD_THRUSTER = ITEMS.register("handheld_thruster",
            () -> new HandheldThrusterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPACESUIT_HELMET =  ITEMS.register("spacesuit_helmet", () ->
            new ArmorItem(PSArmorMaterial.SPACE_HUD, ArmorItem.Type.HELMET, new Item.Properties()));

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
