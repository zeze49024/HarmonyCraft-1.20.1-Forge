package com.zeze.harmony.block;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HarmonyCraftMod.MOD_ID);

    //注册方块
    //.noLootTable() 为没有战利品列表
    //asphalt_block 沥青道路
    public static final RegistryObject<Block> ASPHALT_BLOCK =
            registerBlock("asphalt_block",
                    () -> new Block(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));

    //asphalt_slab 沥青道路台阶
    public static final RegistryObject<SlabBlock> ASPHALT_SLAB =
            registerBlock("asphalt_slab",
                    () -> new SlabBlock(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));

    //asphalt_white_str_block 沥青道路白实线
    public static final RegistryObject<Block> ASPHALT_WHITE_STR_BLOCK =
            registerBlock("asphalt_white_str_block",
                    () -> new Block(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));

    //asphalt_white_str_slab 沥青道路白实线台阶
    public static final RegistryObject<SlabBlock> ASPHALT_WHITE_STR_SLAB =
            registerBlock("asphalt_white_str_slab",
                    () -> new SlabBlock(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));



    //创建注册方法，用来注册方块的blockItem
    private static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    //创建注册方法，用来注册方块的block
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> blcoks = BLOCKS.register(name, block);
        registerBlockItems(name, blcoks);
        return blcoks;
    }

    //注册事件
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
