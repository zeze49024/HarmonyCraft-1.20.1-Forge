package com.zeze.harmony.block;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HarmonyCraftMod.MOD_ID);

    //注册方块
    //.noLootTable() 为没有战利品列表
    //asphalt_block 沥青道路
    public static final RegistryObject<Block> ASPHALT_BLOCK =
            registerBlock("asphalt_block",
                    () -> new Block(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));

    //asphalt_slab 沥青道路台阶
    public static final RegistryObject<SlabBlock> ASPHALT_SLAB =
            registerBlock("asphalt_slab",
                    () -> new SlabBlock(BlockBehaviour.Properties.of().strength(3.0F, 2.0F)));

    public static final RegistryObject<Block> ASPHALT_NEW_BLOCK = asphaltBlock("asphalt_new_block");
    public static final RegistryObject<SlabBlock> ASPHALT_NEW_SLAB = asphaltSlab("asphalt_new_slab");
    public static final RegistryObject<Block> ASPHALT_FADED_BLOCK = asphaltBlock("asphalt_faded_block");
    public static final RegistryObject<SlabBlock> ASPHALT_FADED_SLAB = asphaltSlab("asphalt_faded_slab");

    public static final List<AsphaltVariant> ASPHALT_VARIANTS = List.of(
            new AsphaltVariant("asphalt_new", ASPHALT_NEW_BLOCK, ASPHALT_NEW_SLAB),
            new AsphaltVariant("asphalt_faded", ASPHALT_FADED_BLOCK, ASPHALT_FADED_SLAB));

    //asphalt_white_str_block 沥青道路白实线
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_STR_BLOCK =
            registerBlock("asphalt_white_str_block",
                    () -> new RoadLineBlock(roadProperties()));

    //asphalt_white_str_slab 沥青道路白实线台阶
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_STR_SLAB =
            registerBlock("asphalt_white_str_slab",
                    () -> new RoadLineSlabBlock(roadProperties()));

    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DASHED_LINE_BLOCK = lineBlock("asphalt_white_dashed_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DASHED_LINE_SLAB = lineSlab("asphalt_white_dashed_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DOUBLE_LINE_BLOCK = lineBlock("asphalt_white_double_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DOUBLE_LINE_SLAB = lineSlab("asphalt_white_double_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_SOLID_LINE_BLOCK = lineBlock("asphalt_yellow_solid_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_SOLID_LINE_SLAB = lineSlab("asphalt_yellow_solid_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DASHED_LINE_BLOCK = lineBlock("asphalt_yellow_dashed_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DASHED_LINE_SLAB = lineSlab("asphalt_yellow_dashed_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DOUBLE_LINE_BLOCK = lineBlock("asphalt_yellow_double_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DOUBLE_LINE_SLAB = lineSlab("asphalt_yellow_double_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_STOP_LINE_BLOCK = lineBlock("asphalt_stop_line_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_STOP_LINE_SLAB = lineSlab("asphalt_stop_line_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_CROSSWALK_BLOCK = lineBlock("asphalt_crosswalk_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_CROSSWALK_SLAB = lineSlab("asphalt_crosswalk_slab");
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DIAGONAL_LINE_BLOCK =
            registerBlock("asphalt_white_diagonal_line_block", () -> new RoadLineBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DIAGONAL_LINE_SLAB =
            registerBlock("asphalt_white_diagonal_line_slab", () -> new RoadLineSlabBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DIAGONAL_LINE_BLOCK =
            registerBlock("asphalt_yellow_diagonal_line_block", () -> new RoadLineBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DIAGONAL_LINE_SLAB =
            registerBlock("asphalt_yellow_diagonal_line_slab", () -> new RoadLineSlabBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DOUBLE_DIAGONAL_LINE_BLOCK =
            registerBlock("asphalt_white_double_diagonal_line_block", () -> new RoadLineBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DOUBLE_DIAGONAL_LINE_SLAB =
            registerBlock("asphalt_white_double_diagonal_line_slab", () -> new RoadLineSlabBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DOUBLE_DIAGONAL_LINE_BLOCK =
            registerBlock("asphalt_yellow_double_diagonal_line_block", () -> new RoadLineBlock(roadProperties(), true));
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DOUBLE_DIAGONAL_LINE_SLAB =
            registerBlock("asphalt_yellow_double_diagonal_line_slab", () -> new RoadLineSlabBlock(roadProperties(), true));

    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_RIGHT_ANGLE_LINE_BLOCK = cornerBlock("white_right_angle_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_RIGHT_ANGLE_LINE_SLAB = cornerSlab("white_right_angle_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_RIGHT_ANGLE_LINE_BLOCK = cornerBlock("yellow_right_angle_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_RIGHT_ANGLE_LINE_SLAB = cornerSlab("yellow_right_angle_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DOUBLE_RIGHT_ANGLE_LINE_BLOCK = cornerBlock("white_double_right_angle_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DOUBLE_RIGHT_ANGLE_LINE_SLAB = cornerSlab("white_double_right_angle_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DOUBLE_RIGHT_ANGLE_LINE_BLOCK = cornerBlock("yellow_double_right_angle_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DOUBLE_RIGHT_ANGLE_LINE_SLAB = cornerSlab("yellow_double_right_angle_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_CURVED_LINE_BLOCK = cornerBlock("white_curved_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_CURVED_LINE_SLAB = cornerSlab("white_curved_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_CURVED_LINE_BLOCK = cornerBlock("yellow_curved_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_CURVED_LINE_SLAB = cornerSlab("yellow_curved_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_WHITE_DOUBLE_CURVED_LINE_BLOCK = cornerBlock("white_double_curved_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_WHITE_DOUBLE_CURVED_LINE_SLAB = cornerSlab("white_double_curved_line");
    public static final RegistryObject<RoadLineBlock> ASPHALT_YELLOW_DOUBLE_CURVED_LINE_BLOCK = cornerBlock("yellow_double_curved_line");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_YELLOW_DOUBLE_CURVED_LINE_SLAB = cornerSlab("yellow_double_curved_line");

    public static final RegistryObject<RoadLineBlock> ASPHALT_REVERSIBLE_LANE_ZIGZAG_BLOCK = lineBlock("asphalt_reversible_lane_zigzag_block");
    public static final RegistryObject<RoadLineSlabBlock> ASPHALT_REVERSIBLE_LANE_ZIGZAG_SLAB = lineSlab("asphalt_reversible_lane_zigzag_slab");

    public static final List<RoadVariant> ROAD_MARKINGS = List.of(
            new RoadVariant("white_solid_line", ASPHALT_WHITE_STR_BLOCK, ASPHALT_WHITE_STR_SLAB),
            new RoadVariant("white_dashed_line", ASPHALT_WHITE_DASHED_LINE_BLOCK, ASPHALT_WHITE_DASHED_LINE_SLAB),
            new RoadVariant("white_double_line", ASPHALT_WHITE_DOUBLE_LINE_BLOCK, ASPHALT_WHITE_DOUBLE_LINE_SLAB),
            new RoadVariant("yellow_solid_line", ASPHALT_YELLOW_SOLID_LINE_BLOCK, ASPHALT_YELLOW_SOLID_LINE_SLAB),
            new RoadVariant("yellow_dashed_line", ASPHALT_YELLOW_DASHED_LINE_BLOCK, ASPHALT_YELLOW_DASHED_LINE_SLAB),
            new RoadVariant("yellow_double_line", ASPHALT_YELLOW_DOUBLE_LINE_BLOCK, ASPHALT_YELLOW_DOUBLE_LINE_SLAB),
            new RoadVariant("stop_line", ASPHALT_STOP_LINE_BLOCK, ASPHALT_STOP_LINE_SLAB),
            new RoadVariant("crosswalk", ASPHALT_CROSSWALK_BLOCK, ASPHALT_CROSSWALK_SLAB),
            new RoadVariant("white_diagonal_line", ASPHALT_WHITE_DIAGONAL_LINE_BLOCK, ASPHALT_WHITE_DIAGONAL_LINE_SLAB),
            new RoadVariant("yellow_diagonal_line", ASPHALT_YELLOW_DIAGONAL_LINE_BLOCK, ASPHALT_YELLOW_DIAGONAL_LINE_SLAB),
            new RoadVariant("white_double_diagonal_line", ASPHALT_WHITE_DOUBLE_DIAGONAL_LINE_BLOCK, ASPHALT_WHITE_DOUBLE_DIAGONAL_LINE_SLAB),
            new RoadVariant("yellow_double_diagonal_line", ASPHALT_YELLOW_DOUBLE_DIAGONAL_LINE_BLOCK, ASPHALT_YELLOW_DOUBLE_DIAGONAL_LINE_SLAB),
            new RoadVariant("white_right_angle_line", ASPHALT_WHITE_RIGHT_ANGLE_LINE_BLOCK, ASPHALT_WHITE_RIGHT_ANGLE_LINE_SLAB),
            new RoadVariant("yellow_right_angle_line", ASPHALT_YELLOW_RIGHT_ANGLE_LINE_BLOCK, ASPHALT_YELLOW_RIGHT_ANGLE_LINE_SLAB),
            new RoadVariant("white_double_right_angle_line", ASPHALT_WHITE_DOUBLE_RIGHT_ANGLE_LINE_BLOCK, ASPHALT_WHITE_DOUBLE_RIGHT_ANGLE_LINE_SLAB),
            new RoadVariant("yellow_double_right_angle_line", ASPHALT_YELLOW_DOUBLE_RIGHT_ANGLE_LINE_BLOCK, ASPHALT_YELLOW_DOUBLE_RIGHT_ANGLE_LINE_SLAB),
            new RoadVariant("white_curved_line", ASPHALT_WHITE_CURVED_LINE_BLOCK, ASPHALT_WHITE_CURVED_LINE_SLAB),
            new RoadVariant("yellow_curved_line", ASPHALT_YELLOW_CURVED_LINE_BLOCK, ASPHALT_YELLOW_CURVED_LINE_SLAB),
            new RoadVariant("white_double_curved_line", ASPHALT_WHITE_DOUBLE_CURVED_LINE_BLOCK, ASPHALT_WHITE_DOUBLE_CURVED_LINE_SLAB),
            new RoadVariant("yellow_double_curved_line", ASPHALT_YELLOW_DOUBLE_CURVED_LINE_BLOCK, ASPHALT_YELLOW_DOUBLE_CURVED_LINE_SLAB),
            new RoadVariant("reversible_lane_zigzag", ASPHALT_REVERSIBLE_LANE_ZIGZAG_BLOCK, ASPHALT_REVERSIBLE_LANE_ZIGZAG_SLAB));

    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_STRAIGHT = arrow("road_arrow_straight");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_LEFT = arrow("road_arrow_left");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_RIGHT = arrow("road_arrow_right");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_STRAIGHT_LEFT = arrow("road_arrow_straight_left");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_STRAIGHT_RIGHT = arrow("road_arrow_straight_right");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_LEFT_RIGHT = arrow("road_arrow_left_right");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_UTURN = reversibleArrow("road_arrow_uturn");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_STRAIGHT_UTURN = reversibleArrow("road_arrow_straight_uturn");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_LEFT_UTURN = reversibleArrow("road_arrow_left_uturn");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_MERGE_LEFT = arrow("road_arrow_merge_left");
    public static final RegistryObject<RoadArrowBlock> ROAD_ARROW_MERGE_RIGHT = arrow("road_arrow_merge_right");
    public static final List<RegistryObject<RoadArrowBlock>> ROAD_ARROWS =
            List.of(ROAD_ARROW_STRAIGHT, ROAD_ARROW_LEFT, ROAD_ARROW_RIGHT,
                    ROAD_ARROW_STRAIGHT_LEFT, ROAD_ARROW_STRAIGHT_RIGHT, ROAD_ARROW_LEFT_RIGHT,
                    ROAD_ARROW_UTURN, ROAD_ARROW_STRAIGHT_UTURN, ROAD_ARROW_LEFT_UTURN,
                    ROAD_ARROW_MERGE_LEFT, ROAD_ARROW_MERGE_RIGHT);

    public static final RegistryObject<RoadSurfaceFeatureBlock> ROAD_MANHOLE =
            registerBlock("road_manhole", () -> new RoadSurfaceFeatureBlock(roadProperties()));
    public static final RegistryObject<RoadSurfaceFeatureBlock> ROAD_DRAIN =
            registerBlock("road_drain", () -> new RoadSurfaceFeatureBlock(roadProperties()));

    public record RoadVariant(String textureName, RegistryObject<RoadLineBlock> block,
                              RegistryObject<RoadLineSlabBlock> slab) {
        public boolean isDiagonal() {
            return textureName.endsWith("diagonal_line");
        }

        public boolean isDoubleLine() {
            return textureName.contains("_double_");
        }

        public boolean isCorner() {
            return textureName.endsWith("right_angle_line") || textureName.endsWith("curved_line");
        }

        public boolean connectsAdjacentEdges() {
            return isDiagonal() || isCorner();
        }

        /** Edges actually reached by paint in the north-facing top texture. */
        public List<Direction> paintedEdges() {
            if (connectsAdjacentEdges()) {
                return List.of(Direction.NORTH, Direction.EAST);
            }
            if (textureName.endsWith("solid_line") || textureName.endsWith("double_line")) {
                return List.of(Direction.NORTH, Direction.SOUTH);
            }
            return switch (textureName) {
                case "crosswalk" -> List.of(Direction.EAST, Direction.WEST);
                case "stop_line" -> List.of(Direction.NORTH, Direction.EAST, Direction.WEST);
                case "reversible_lane_zigzag" -> List.of(Direction.NORTH, Direction.SOUTH);
                default -> List.of(); // A centered dash does not touch any block edge.
            };
        }
    }

    public record AsphaltVariant(String textureName, RegistryObject<Block> block,
                                 RegistryObject<SlabBlock> slab) { }

    private static BlockBehaviour.Properties roadProperties() {
        return BlockBehaviour.Properties.of().strength(3.0F, 2.0F);
    }

    private static RegistryObject<RoadLineBlock> lineBlock(String name) {
        return registerBlock(name, () -> new RoadLineBlock(roadProperties()));
    }

    private static RegistryObject<RoadLineSlabBlock> lineSlab(String name) {
        return registerBlock(name, () -> new RoadLineSlabBlock(roadProperties()));
    }

    private static RegistryObject<RoadLineBlock> cornerBlock(String pattern) {
        return registerBlock("asphalt_" + pattern + "_block", () -> new RoadLineBlock(roadProperties(), true));
    }

    private static RegistryObject<RoadLineSlabBlock> cornerSlab(String pattern) {
        return registerBlock("asphalt_" + pattern + "_slab", () -> new RoadLineSlabBlock(roadProperties(), true));
    }

    private static RegistryObject<RoadLineBlock> adjacentBlock(String name) {
        return registerBlock(name, () -> new RoadLineBlock(roadProperties(), true));
    }

    private static RegistryObject<RoadLineSlabBlock> adjacentSlab(String name) {
        return registerBlock(name, () -> new RoadLineSlabBlock(roadProperties(), true));
    }

    private static RegistryObject<Block> asphaltBlock(String name) {
        return registerBlock(name, () -> new Block(roadProperties()));
    }

    private static RegistryObject<SlabBlock> asphaltSlab(String name) {
        return registerBlock(name, () -> new SlabBlock(roadProperties()));
    }

    private static RegistryObject<RoadArrowBlock> arrow(String name) {
        return registerBlock(name, () -> new RoadArrowBlock(arrowProperties()));
    }

    private static RegistryObject<RoadArrowBlock> reversibleArrow(String name) {
        return registerBlock(name, () -> new ReversibleRoadArrowBlock(arrowProperties()));
    }

    private static BlockBehaviour.Properties arrowProperties() {
        return BlockBehaviour.Properties.of().strength(0.1F).noCollission().noOcclusion().noLootTable().forceSolidOff();
    }

    //创建注册方法，用来注册方块的blockItem
    private static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    //创建注册方法，用来注册方块的block
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        registerBlockItems(name, registered);
        return registered;
    }

    //注册事件
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
