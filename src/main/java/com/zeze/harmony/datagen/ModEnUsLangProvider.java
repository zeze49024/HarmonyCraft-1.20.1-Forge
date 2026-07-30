package com.zeze.harmony.datagen;
//注册英文翻译数据
import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {
    public ModEnUsLangProvider(PackOutput output) {
        super(output, HarmonyCraftMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        //物品栏
        add("itemGroup.road_tab", "HarmonyCraft | Road");
        //物品
        add(ModItems.HARMONY_LOGO.get(), "Harmony Logo");
        //方块
        add(ModBlocks.ASPHALT_BLOCK.get(), "Asphalt Block");
        add(ModBlocks.ASPHALT_SLAB.get(), "Asphalt Slab");
        add(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get(), "White Line");
        add(ModBlocks.ASPHALT_WHITE_STR_SLAB.get(), "White Line Slab");
    }
}
