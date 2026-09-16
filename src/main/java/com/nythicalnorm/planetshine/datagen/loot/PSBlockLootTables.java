package com.nythicalnorm.planetshine.datagen.loot;

import com.nythicalnorm.planetshine.block.PSBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class PSBlockLootTables extends BlockLootSubProvider {
    public PSBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        //this.dropSelf(PSBlocks.LUNAR_REGOLITH.get());
    }

    @Override
    protected  Iterable<Block> getKnownBlocks() {
        return PSBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
