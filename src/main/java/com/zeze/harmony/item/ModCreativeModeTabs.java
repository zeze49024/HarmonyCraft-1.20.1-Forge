package com.zeze.harmony.item;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HarmonyCraftMod.MOD_ID);
    //物品栏注册
    //道路
    public static final RegistryObject<CreativeModeTab> ROAD_TAB =
            CREATIVE_MODE_TABS.register("road_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ASPHALT_BLOCK.get()))
                    .title(Component.translatable("itemGroup.road_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        //要放入的物品
                        pOutput.accept(ModItems.HARMONY_LOGO.get());
                        pOutput.accept(ModBlocks.ASPHALT_BLOCK.get());
                        pOutput.accept(ModBlocks.ASPHALT_SLAB.get());
                        ModBlocks.ASPHALT_VARIANTS.forEach(variant -> {
                            pOutput.accept(variant.block().get());
                            pOutput.accept(variant.slab().get());
                        });
                        pOutput.accept(ModBlocks.ROAD_MANHOLE.get());
                        pOutput.accept(ModBlocks.ROAD_DRAIN.get());
                        ModBlocks.ROAD_MARKINGS.forEach(road -> {
                            pOutput.accept(road.block().get());
                            pOutput.accept(road.slab().get());
                        });
                        ModBlocks.ROAD_ARROWS.forEach(arrow -> pOutput.accept(arrow.get()));
                    }).build());
    //其他物品
    public static final RegistryObject<CreativeModeTab> HAR_ITEM_TAB =
            CREATIVE_MODE_TABS.register("har_item_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.HARMONY_TRANSIT.get()))
                    .title(Component.translatable("itemGroup.har_item_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        //要放入的物品
                        pOutput.accept(ModItems.HARMONY_TRANSIT.get());
                    }).build());
    //注册事件
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
