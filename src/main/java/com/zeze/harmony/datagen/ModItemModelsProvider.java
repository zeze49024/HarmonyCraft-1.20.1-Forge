package com.zeze.harmony.datagen;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HarmonyCraftMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HARMONY_LOGO.get());
        basicItem(ModItems.HARMONY_TRANSIT.get());
    }

}
