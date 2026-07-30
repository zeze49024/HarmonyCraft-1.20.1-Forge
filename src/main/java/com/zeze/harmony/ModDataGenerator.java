package com.zeze.harmony;

import com.zeze.harmony.datagen.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = HarmonyCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        //获取数据生成器
        DataGenerator generator = event.getGenerator();
        //获取数据输出
        PackOutput packOutput = generator.getPackOutput();
        //获取已存在文件帮助器
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        //获取HolderLookup.Provider的 CompletableFuture
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        //调用datagen文件夹下的8个Java类
        generator.addProvider(event.includeServer(), new ModRecipesProvider(packOutput));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)
        )));

        //服务器数据
        BlockTagsProvider blockTagsProvider = generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        //添加物品标签提供器
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput,lookupProvider,blockTagsProvider.contentsGetter(),existingFileHelper));
        //客户端数据
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, existingFileHelper));
        //语言文件
        //添加英文语言提供器
        generator.addProvider(event.includeClient(), new ModEnUsLangProvider(packOutput));
        //添加中文语言提供器
        generator.addProvider(event.includeClient(), new ModZhCnLangProvider(packOutput));
        //添加日文语言提供器
        generator.addProvider(event.includeClient(), new ModJaJpLangProvider(packOutput));
    }
}
