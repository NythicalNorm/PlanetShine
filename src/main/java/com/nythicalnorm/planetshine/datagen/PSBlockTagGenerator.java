package com.nythicalnorm.planetshine.datagen;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.block.PSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PSBlockTagGenerator extends BlockTagsProvider {
    public PSBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PlanetShine.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(BlockTags.NEEDS_IRON_TOOL).add(PSBlocks.LUNAR_REGOLITH.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PSBlocks.LUNAR_REGOLITH.get());
        this.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(PSBlocks.LUNAR_REGOLITH.get());
    }
}
