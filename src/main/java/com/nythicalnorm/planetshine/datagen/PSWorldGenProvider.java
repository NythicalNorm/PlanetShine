package com.nythicalnorm.planetshine.datagen;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.dimensions.SpaceDimension;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PSWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, SpaceDimension::bootstrapType)
            .add(Registries.LEVEL_STEM, SpaceDimension::bootstrapStem);


    public PSWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(PlanetShine.MODID));
    }
}
