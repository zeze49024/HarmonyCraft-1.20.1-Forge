package com.zeze.harmony.item;

import com.zeze.harmony.HarmonyCraftMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HarmonyCraftMod.MOD_ID);
    //注册物品
    //logo
    public static final RegistryObject<Item> HARMONY_LOGO =
        ITEMS.register("harmony_logo",() -> new Item(new Item.Properties().stacksTo(64)));
    //花梦交通卡
    public static final RegistryObject<Item> HARMONY_TRANSIT =
        ITEMS.register("harmony_transit",() -> new Item(new Item.Properties().stacksTo(64)));

//    public static final RegistryObject<Item> ASPHALT_SLAB =
//            ITEMS.register("asphalt_slab",() -> new Item(new Item.Properties().stacksTo(64)));
//
//    public static final RegistryObject<Item> ASPHALT_WHITE_STR_BLOCK =
//            ITEMS.register("asphalt_white_str_block",() -> new Item(new Item.Properties().stacksTo(64)));
//
//    public static final RegistryObject<Item> ASPHALT_WHITE_STR_SLAB =
//            ITEMS.register("asphalt_white_str_slab",() -> new Item(new Item.Properties().stacksTo(64)));
    //注册事件
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
