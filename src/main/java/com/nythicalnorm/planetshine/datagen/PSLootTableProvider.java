package com.nythicalnorm.planetshine.datagen;

import com.nythicalnorm.planetshine.datagen.loot.PSBlockLootTables;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class PSLootTableProvider {
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(PSBlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }
}
