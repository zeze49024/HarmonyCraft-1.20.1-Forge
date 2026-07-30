package com.zeze.harmony.datagen;

import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.item.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        //战利品列表
        //掉落自身
        dropSelf(ModBlocks.ASPHALT_BLOCK.get());
        dropSelf(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get());

        //台阶的掉落物
        add(ModBlocks.ASPHALT_SLAB.get(),
            block -> createSlabItemTable(ModBlocks.ASPHALT_SLAB.get()));

        add(ModBlocks.ASPHALT_WHITE_STR_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.ASPHALT_WHITE_STR_SLAB.get()));
    }

    //检测有无战利品列表
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
