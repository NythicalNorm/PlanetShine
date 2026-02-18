package com.nythicalnorm.planetshine.commands;

import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PSArguments {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, PlanetShine.MODID);
    public static final RegistryObject<ArgumentTypeInfo<PlanetArgument, ?>> PLANET_ARGUMENT_TYPE = ARGUMENT_TYPES.register("planets", () -> ArgumentTypeInfos.registerByClass(PlanetArgument.class, new PlanetArgument.Info()));

    public static void register(IEventBus eventBus)
    {
        ARGUMENT_TYPES.register(eventBus);
    }
}
