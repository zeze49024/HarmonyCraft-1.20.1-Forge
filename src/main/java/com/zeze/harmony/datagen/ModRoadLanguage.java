package com.zeze.harmony.datagen;

import com.zeze.harmony.block.ModBlocks;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Map;

final class ModRoadLanguage {
    private static final Map<String, Names> NAMES = Map.ofEntries(
            Map.entry("white_solid_line", new Names("白色实线沥青道路", "White Solid Line Asphalt", "白い実線付きアスファルト")),
            Map.entry("white_dashed_line", new Names("白色虚线沥青道路", "White Dashed Line Asphalt", "白い破線付きアスファルト")),
            Map.entry("white_double_line", new Names("双白实线沥青道路", "Double White Solid Line Asphalt", "白い二重実線付きアスファルト")),
            Map.entry("yellow_solid_line", new Names("黄色实线沥青道路", "Yellow Solid Line Asphalt", "黄色い実線付きアスファルト")),
            Map.entry("yellow_dashed_line", new Names("黄色虚线沥青道路", "Yellow Dashed Line Asphalt", "黄色い破線付きアスファルト")),
            Map.entry("yellow_double_line", new Names("双黄实线沥青道路", "Double Yellow Solid Line Asphalt", "黄色い二重実線付きアスファルト")),
            Map.entry("stop_line", new Names("停止线沥青道路", "Stop Line Asphalt", "停止線付きアスファルト")),
            Map.entry("crosswalk", new Names("斑马线沥青道路", "Crosswalk Asphalt", "横断歩道付きアスファルト")),
            Map.entry("white_diagonal_line", new Names("白色斜线沥青道路", "White Diagonal Line Asphalt", "白い斜線付きアスファルト")),
            Map.entry("yellow_diagonal_line", new Names("黄色斜线沥青道路", "Yellow Diagonal Line Asphalt", "黄色い斜線付きアスファルト")),
            Map.entry("white_double_diagonal_line", new Names("双白斜线沥青道路", "Double White Diagonal Line Asphalt", "白い二重斜線付きアスファルト")),
            Map.entry("yellow_double_diagonal_line", new Names("双黄斜线沥青道路", "Double Yellow Diagonal Line Asphalt", "黄色い二重斜線付きアスファルト")),
            Map.entry("white_right_angle_line", new Names("白色直角标线沥青道路", "White Right Angle Line Asphalt", "白い直角線付きアスファルト")),
            Map.entry("yellow_right_angle_line", new Names("黄色直角标线沥青道路", "Yellow Right Angle Line Asphalt", "黄色い直角線付きアスファルト")),
            Map.entry("white_double_right_angle_line", new Names("双白直角标线沥青道路", "Double White Right Angle Line Asphalt", "白い二重直角線付きアスファルト")),
            Map.entry("yellow_double_right_angle_line", new Names("双黄直角标线沥青道路", "Double Yellow Right Angle Line Asphalt", "黄色い二重直角線付きアスファルト")),
            Map.entry("white_curved_line", new Names("白色圆弧标线沥青道路", "White Curved Line Asphalt", "白い曲線付きアスファルト")),
            Map.entry("yellow_curved_line", new Names("黄色圆弧标线沥青道路", "Yellow Curved Line Asphalt", "黄色い曲線付きアスファルト")),
            Map.entry("white_double_curved_line", new Names("双白圆弧标线沥青道路", "Double White Curved Line Asphalt", "白い二重曲線付きアスファルト")),
            Map.entry("yellow_double_curved_line", new Names("双黄圆弧标线沥青道路", "Double Yellow Curved Line Asphalt", "黄色い二重曲線付きアスファルト")),
            Map.entry("road_arrow_straight", new Names("直行箭头标线", "Straight Ahead Road Arrow", "直進矢印の道路標示")),
            Map.entry("road_arrow_left", new Names("左转箭头标线", "Left Turn Road Arrow", "左折矢印の道路標示")),
            Map.entry("road_arrow_right", new Names("右转箭头标线", "Right Turn Road Arrow", "右折矢印の道路標示")),
            Map.entry("road_arrow_straight_left", new Names("直行或左转箭头标线", "Straight or Left Road Arrow", "直進・左折矢印の道路標示")),
            Map.entry("road_arrow_straight_right", new Names("直行或右转箭头标线", "Straight or Right Road Arrow", "直進・右折矢印の道路標示")),
            Map.entry("road_arrow_left_right", new Names("仅左转或右转箭头标线", "Left or Right Road Arrow", "左折・右折矢印の道路標示")),
            Map.entry("road_arrow_uturn", new Names("掉头箭头标线", "U-turn Road Arrow", "転回矢印の道路標示")),
            Map.entry("road_arrow_straight_uturn", new Names("直行或掉头箭头标线", "Straight or U-turn Road Arrow", "直進・転回矢印の道路標示")),
            Map.entry("road_arrow_left_uturn", new Names("左转或掉头箭头标线", "Left or U-turn Road Arrow", "左折・転回矢印の道路標示")),
            Map.entry("road_arrow_merge_left", new Names("向左合流箭头标线", "Merge Left Road Arrow", "左合流矢印の道路標示")),
            Map.entry("road_arrow_merge_right", new Names("向右合流箭头标线", "Merge Right Road Arrow", "右合流矢印の道路標示")),
            Map.entry("reversible_lane_zigzag", new Names("潮汐车道锯齿线沥青道路", "Reversible Lane Zigzag Asphalt", "可逆車線ジグザグ線付きアスファルト")));

    private ModRoadLanguage() { }

    static void add(LanguageProvider provider, String language) {
        String slabSuffix = switch (language) {
            case "zh_cn" -> "半砖";
            case "en_us" -> " Slab";
            case "ja_jp" -> "のハーフブロック";
            default -> throw new IllegalArgumentException(language);
        };
        ModBlocks.ROAD_MARKINGS.forEach(road -> {
            String name = NAMES.get(road.textureName()).in(language);
            provider.add(road.block().get(), name);
            provider.add(road.slab().get(), name + slabSuffix);
        });
        ModBlocks.ROAD_ARROWS.forEach(arrow -> provider.add(arrow.get(), NAMES.get(arrow.getId().getPath()).in(language)));
        String[] asphaltNames = switch (language) {
            case "zh_cn" -> new String[]{"新铺沥青道路", "褪色沥青道路", "道路井盖", "道路排水口"};
            case "en_us" -> new String[]{"New Asphalt", "Faded Asphalt", "Road Manhole", "Road Drain"};
            case "ja_jp" -> new String[]{"新設アスファルト", "退色アスファルト", "道路マンホール", "道路排水口"};
            default -> throw new IllegalArgumentException(language);
        };
        provider.add(ModBlocks.ASPHALT_NEW_BLOCK.get(), asphaltNames[0]);
        provider.add(ModBlocks.ASPHALT_NEW_SLAB.get(), asphaltNames[0] + slabSuffix);
        provider.add(ModBlocks.ASPHALT_FADED_BLOCK.get(), asphaltNames[1]);
        provider.add(ModBlocks.ASPHALT_FADED_SLAB.get(), asphaltNames[1] + slabSuffix);
        provider.add(ModBlocks.ROAD_MANHOLE.get(), asphaltNames[2]);
        provider.add(ModBlocks.ROAD_DRAIN.get(), asphaltNames[3]);
    }

    private record Names(String chinese, String english, String japanese) {
        String in(String language) {
            return switch (language) {
                case "zh_cn" -> chinese;
                case "en_us" -> english;
                case "ja_jp" -> japanese;
                default -> throw new IllegalArgumentException(language);
            };
        }
    }
}
