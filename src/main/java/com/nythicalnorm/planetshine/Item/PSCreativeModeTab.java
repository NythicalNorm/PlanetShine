package com.nythicalnorm.planetshine.Item;

import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class PSCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PlanetShine.MODID);

//    public static final RegistryObject<CreativeModeTab> Main_Mod_Tab = CREATIVE_MODE_TABS.register("ps_tab",
//            () -> CreativeModeTab.builder().icon(() -> new ItemStack(PSItems.HANDHELD_THRUSTER.get()))
//                    .title(Component.translatable("creativetab.main_ps"))
//                    .displayItems((itemDisplayParameters, output) -> {
//                        output.accept(PSItems.HANDHELD_THRUSTER.get());
//                        output.accept(PSItems.SPACESUIT_HELMET.get());
//
//                        output.accept(PSBlocks.LUNAR_REGOLITH.get());
//                    })
//                    .build());

    public static void register(IEventBus eventBus)
    {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
