package com.zeze.harmony.datagen;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

//方块状态文件
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, HarmonyCraftMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //方块
        simpleBlockWithItem(ModBlocks.ASPHALT_BLOCK.get(), cubeAll(ModBlocks.ASPHALT_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get(), cubeAll(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get()));

        //台阶
        slabBlock(ModBlocks.ASPHALT_SLAB.get(), blockTexture(ModBlocks.ASPHALT_BLOCK.get()), blockTexture(ModBlocks.ASPHALT_BLOCK.get()));
        slabBlock(ModBlocks.ASPHALT_WHITE_STR_SLAB.get(), blockTexture(ModBlocks.ASPHALT_WHITE_STR_SLAB.get()), blockTexture(ModBlocks.ASPHALT_WHITE_STR_SLAB.get()));

        //方块物品
        blockItem(ModBlocks.ASPHALT_SLAB);
        //blockItem(ModBlocks.ASPHALT_WHITE_STR_BLOCK);
        blockItem(ModBlocks.ASPHALT_WHITE_STR_SLAB);
    }

    private <T extends Block> void blockItem(RegistryObject<T> block) {
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(HarmonyCraftMod.MOD_ID + "block/" + block.getId().getPath()));
    }

    private <T extends Block> void blockItem(RegistryObject<T> block, String append) {
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(HarmonyCraftMod.MOD_ID + "block/" + block.getId().getPath() + append));
    }
}
