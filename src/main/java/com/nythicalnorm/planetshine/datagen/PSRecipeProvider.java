package com.nythicalnorm.planetshine.datagen;

import com.nythicalnorm.planetshine.Item.PSItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PSRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public PSRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PSItems.HANDHELD_THRUSTER.get())
                .pattern("XRX")
                .pattern("R#R")
                .pattern("XRX")
                .define('#', Items.IRON_BLOCK)
                .define('X', Items.REDSTONE)
                .define('R', Items.IRON_BARS)
                .unlockedBy(getHasName(Items.IRON_BLOCK), has(Items.IRON_BLOCK))
                .save(pWriter);
    }
}
