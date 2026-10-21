package com.zeze.harmony.datagen;

import com.zeze.harmony.block.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
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

        //台阶的掉落物
        add(ModBlocks.ASPHALT_SLAB.get(),
            block -> createSlabItemTable(ModBlocks.ASPHALT_SLAB.get()));

        ModBlocks.ASPHALT_VARIANTS.forEach(variant -> {
            dropSelf(variant.block().get());
            add(variant.slab().get(), this::createSlabItemTable);
        });
        dropSelf(ModBlocks.ROAD_MANHOLE.get());
        dropSelf(ModBlocks.ROAD_DRAIN.get());

        ModBlocks.ROAD_MARKINGS.forEach(road -> {
            dropSelf(road.block().get());
            add(road.slab().get(), this::createSlabItemTable);
        });
        // Arrow blocks declare noLootTable and never drop an item.
    }

    //检测有无战利品列表
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
