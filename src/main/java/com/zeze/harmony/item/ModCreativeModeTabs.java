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
    public static final RegistryObject<CreativeModeTab> ROAD_TAB =
            //道路
            CREATIVE_MODE_TABS.register("road_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ASPHALT_BLOCK.get()))
                    .title(Component.translatable("itemGroup.road_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        //要放入的物品
                        pOutput.accept(ModItems.HARMONY_LOGO.get());
                        pOutput.accept(ModBlocks.ASPHALT_BLOCK.get());
                        pOutput.accept(ModBlocks.ASPHALT_SLAB.get());
                        pOutput.accept(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get());
                        pOutput.accept(ModBlocks.ASPHALT_WHITE_STR_SLAB.get());
                    }).build());
    //注册事件
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
