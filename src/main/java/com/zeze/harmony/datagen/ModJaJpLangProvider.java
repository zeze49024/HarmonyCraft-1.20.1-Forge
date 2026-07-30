package com.zeze.harmony.datagen;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModJaJpLangProvider extends LanguageProvider {
    public ModJaJpLangProvider(PackOutput output) {
        super(output, HarmonyCraftMod.MOD_ID, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        //物品栏
        add("itemGroup.road_tab", "ハーモニークラフト | 道路");
        //物品
        add(ModItems.HARMONY_LOGO.get(), "ハーモニーロゴ");
        //方块
        add(ModBlocks.ASPHALT_BLOCK.get(), "アスファルトブロック");
        add(ModBlocks.ASPHALT_SLAB.get(), "アスファルトスラブ");
        add(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get(), "白線アスファルト");
        add(ModBlocks.ASPHALT_WHITE_STR_SLAB.get(), "白線アスファルトスラブ");
    }
}
