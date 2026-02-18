package com.nythicalnorm.planetshine.datagen;
import com.nythicalnorm.planetshine.Item.PSItems;
import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class PSItemModelProvider extends ItemModelProvider {
    public PSItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PlanetShine.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        item3dOnlyinHand(PSItems.HANDHELD_THRUSTER);

        simpleItem(PSItems.SPACESUIT_HELMET);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        assert item.getId() != null;
        return  withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID, "item/" + item.getId().getPath()));
    }

    private void item3dOnlyinHand(RegistryObject<Item> item) {
        withExistingParent(item.getId().getPath() + "_2d",
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID, "item/" + item.getId().getPath()));
    }
}
