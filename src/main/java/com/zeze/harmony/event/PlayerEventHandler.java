package com.zeze.harmony.event;

import com.zeze.harmony.HarmonyCraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.ChatFormatting;

// 玩家登录事件处理类
@Mod.EventBusSubscriber(modid = HarmonyCraftMod.MOD_ID) // 替换为你的模组ID
public class PlayerEventHandler {
    // 玩家登录事件处理方法
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 获取登录的玩家实体
        Player player = event.getEntity();
        // 发送欢迎消息给玩家
        // 分割线
        Component line = Component.literal("==============================================")
                // 设置消息样式为红色，以警告玩家
                .withStyle(ChatFormatting.WHITE);

        //消息1：欢迎进入HarmonyCraft！本模组仅供HarmonyCraft服务器使用，严禁非法传播
        // 中文
        Component message1zhcn = Component.literal("欢迎进入HarmonyCraft！本模组仅供HarmonyCraft服务器使用，严禁非法传播。")
                // 设置消息样式为红色，以警告玩家
                .withStyle(ChatFormatting.RED);
        // 英文
        Component message1enus = Component.literal("Welcome to HarmonyCraft! This mod is exclusively for use on the HarmonyCraft server. Unauthorized distribution is strictly prohibited.。")
                // 设置消息样式为红色，以警告玩家
                .withStyle(ChatFormatting.RED);

        //消息2：请严格遵守玩家守则，违规者将会受到处罚。
        // 中文
        Component message2zhcn = Component.literal("请严格遵守玩家守则，违规者将会受到处罚。")
                // 设置消息样式为黄色，以提醒玩家
                .withStyle(ChatFormatting.YELLOW);
        // 英文
        Component message2enus = Component.literal("Please strictly follow the Player Rules. Any violations will result in disciplinary action.")
                // 设置消息样式为黄色，以提醒玩家
                .withStyle(ChatFormatting.YELLOW);

        // 发送系统消息给玩家
        player.sendSystemMessage(line);
        player.sendSystemMessage(message1zhcn);
        player.sendSystemMessage(message1enus);
        player.sendSystemMessage(message2zhcn);
        player.sendSystemMessage(message2enus);
        player.sendSystemMessage(line);
    }
}
