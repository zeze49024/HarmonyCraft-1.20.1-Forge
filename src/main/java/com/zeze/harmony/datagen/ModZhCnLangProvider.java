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
        //物品
        add(ModItems.HARMONY_LOGO.get(), "花梦工艺Logo");
        //方块
        add(ModBlocks.ASPHALT_BLOCK.get(), "沥青道路");
        add(ModBlocks.ASPHALT_SLAB.get(), "沥青道路半砖");
        add(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get(), "");
        add(ModBlocks.ASPHALT_WHITE_STR_SLAB.get(), "沥青道路白线半砖");
    }
}
