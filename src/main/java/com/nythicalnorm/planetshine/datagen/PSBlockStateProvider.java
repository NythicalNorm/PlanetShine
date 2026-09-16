package com.nythicalnorm.planetshine.datagen;

import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class PSBlockStateProvider extends BlockStateProvider {
    public PSBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, PlanetShine.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlockWithItem(PSBlocks.LUNAR_REGOLITH.get(), cubeAll(PSBlocks.LUNAR_REGOLITH.get()));
    }

    protected ModelFile getColumnCubeModel(@NotNull RegistryObject<Block> block, ResourceLocation side, ResourceLocation end) {
        assert block.getId() != null;
        return models().withExistingParent(block.getId().getPath(), ModelProvider.BLOCK_FOLDER + "/cube_column_horizontal")
                .texture("side", side)
                .texture("end", end);
    }
}
