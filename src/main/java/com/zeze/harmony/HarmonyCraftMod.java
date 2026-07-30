package com.zeze.harmony;

import com.mojang.logging.LogUtils;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.item.ModCreativeModeTabs;
import com.zeze.harmony.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// 这里的值需要与 META-INF/mods.toml 文件中的某个条目保持一致。
@Mod(HarmonyCraftMod.MOD_ID)
public class HarmonyCraftMod
{
    // 在一个公共的地方定义 Mod ID，以便所有地方都能统一引用。
    public static final String MOD_ID = "harmonycraft_mod";
    // 直接引用一个 slf4j 日志记录器（logger）。
    private static final Logger LOGGER = LogUtils.getLogger();

    public HarmonyCraftMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册 commonSetup 方法，以便在 Mod 加载时调用。
        // 这里可以执行一些通用的初始化代码，如注册物品、块、事件等。
        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        //调用Mod物品栏
        ModCreativeModeTabs.register(modEventBus);
        //调用Block方块
        ModBlocks.register(modEventBus);

        // 注册 Mod 事件总线，以便在 Mod 加载时调用事件处理方法。
        MinecraftForge.EVENT_BUS.register(this);

        // 注册 addCreative 方法，以便在 Mod 加载时调用。
        // 这里可以添加物品到创意模式标签页。
        modEventBus.addListener(this::addCreative);

        // 注册 Mod 配置文件，以便在 Mod 加载时加载。
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 打印日志，确认 Mod 加载成功。
        LOGGER.info("Fuck you Mojang,But the mod is running!");//  打印配置文件中的信息。

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // 注册 addCreative 方法，以便在 Mod 加载时调用。
    // 这里可以添加物品到创意模式标签页。
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.HARMONY_LOGO);
        }
    }

    // 注册 onServerStarting 方法，以便在 Mod 加载时调用。
    // 这里可以在服务器启动时执行一些操作，如加载配置文件。
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // 打印日志，确认服务器启动成功。
        LOGGER.info("Fuck you Mojang,But the server is running!");
    }

    // 注册 onClientSetup 方法，以便在 Mod 加载时调用。
    // 这里可以在客户端初始化时执行一些操作，如注册渲染器、事件等。
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // 打印日志，确认客户端初始化成功。
            LOGGER.info("Fuck you Mojang,But the client is running!");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
