package com.zeze.harmony.datagen;
//注册中文翻译数据
import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, HarmonyCraftMod.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        //物品栏
        add("itemGroup.road_tab", "HarmonyCraft | 道路");
        add("itemGroup.har_item_tab", "HarmonyCraft | 其他物品");
        //物品
        add(ModItems.HARMONY_LOGO.get(), "花梦工艺Logo");
        add(ModItems.HARMONY_TRANSIT.get(), "花梦交通卡");
        //方块
        add(ModBlocks.ASPHALT_BLOCK.get(), "沥青道路");
        add(ModBlocks.ASPHALT_SLAB.get(), "沥青道路半砖");
        ModRoadLanguage.add(this, "zh_cn");
    }
}
