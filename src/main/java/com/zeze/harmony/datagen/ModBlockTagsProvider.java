package com.zeze.harmony.datagen;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    //构造函数
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        //调用父类构造函数
        super(output, lookupProvider, HarmonyCraftMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        //可用搞头挖掘
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.ASPHALT_BLOCK.get(), ModBlocks.ASPHALT_SLAB.get());
        tag(BlockTags.SLABS).add(ModBlocks.ASPHALT_SLAB.get());
        ModBlocks.ASPHALT_VARIANTS.forEach(variant -> {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(variant.block().get(), variant.slab().get());
            tag(BlockTags.SLABS).add(variant.slab().get());
        });
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.ROAD_MANHOLE.get(), ModBlocks.ROAD_DRAIN.get());
        ModBlocks.ROAD_MARKINGS.forEach(road -> {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(road.block().get(), road.slab().get());
            tag(BlockTags.SLABS).add(road.slab().get());
        });

    }
}
