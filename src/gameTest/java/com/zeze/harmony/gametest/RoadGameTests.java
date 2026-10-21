package com.zeze.harmony.gametest;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.block.RoadArrowBlock;
import com.zeze.harmony.block.RoadLineBlock;
import com.zeze.harmony.block.RoadLineSlabBlock;
import com.zeze.harmony.block.ReversibleRoadArrowBlock;
import com.zeze.harmony.block.RoadSurfaceFeatureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

@GameTestHolder(HarmonyCraftMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RoadGameTests {
    private static final BlockPos SUPPORT = new BlockPos(3, 1, 3);

    @GameTest(template = "empty")
    public static void roadPlacementInEveryDirection(GameTestHelper helper) {
        for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                helper.setBlock(SUPPORT, Blocks.STONE);
                helper.setBlock(SUPPORT.above(), Blocks.AIR);
                helper.assertTrue(place(helper, road.block().get(), SUPPORT, Direction.UP, facing, 1).consumesAction(),
                        "Full road placement failed: " + road.textureName());
                helper.assertBlockProperty(SUPPORT.above(), RoadLineBlock.FACING, facing);
                helper.setBlock(SUPPORT.above(), Blocks.AIR);
                helper.assertTrue(place(helper, road.slab().get(), SUPPORT, Direction.UP, facing, 1).consumesAction(),
                        "Bottom slab placement failed: " + road.textureName());
                helper.assertBlockProperty(SUPPORT.above(), RoadLineSlabBlock.FACING, facing);
                helper.assertBlockProperty(SUPPORT.above(), SlabBlock.TYPE, SlabType.BOTTOM);

                BlockPos ceiling = SUPPORT.above(2);
                helper.setBlock(SUPPORT.above(), Blocks.AIR);
                helper.setBlock(ceiling, Blocks.STONE);
                helper.assertTrue(place(helper, road.slab().get(), ceiling, Direction.DOWN, facing, 0).consumesAction(),
                        "Top slab placement failed: " + road.textureName());
                helper.assertBlockProperty(SUPPORT.above(), RoadLineSlabBlock.FACING, facing);
                helper.assertBlockProperty(SUPPORT.above(), SlabBlock.TYPE, SlabType.TOP);
                helper.setBlock(ceiling, Blocks.AIR);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mergingSlabsKeepsDirectionAndClearsWater(GameTestHelper helper) {
        for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
            for (SlabType type : new SlabType[]{SlabType.BOTTOM, SlabType.TOP}) {
                helper.setBlock(SUPPORT, road.slab().get().defaultBlockState()
                        .setValue(RoadLineSlabBlock.FACING, Direction.WEST)
                        .setValue(SlabBlock.TYPE, type).setValue(SlabBlock.WATERLOGGED, true));
                Direction face = type == SlabType.BOTTOM ? Direction.UP : Direction.DOWN;
                helper.assertTrue(place(helper, road.slab().get(), SUPPORT, face, Direction.EAST, 0.5).consumesAction(),
                        "Slab merge failed: " + road.textureName());
                helper.assertBlockProperty(SUPPORT, SlabBlock.TYPE, SlabType.DOUBLE);
                helper.assertBlockProperty(SUPPORT, RoadLineSlabBlock.FACING, Direction.WEST);
                helper.assertBlockProperty(SUPPORT, SlabBlock.WATERLOGGED, false);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slabsRetainVanillaWaterlogging(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
            helper.setBlock(SUPPORT.above(), Blocks.WATER);
            helper.assertTrue(place(helper, road.slab().get(), SUPPORT, Direction.UP, Direction.SOUTH, 1).consumesAction(),
                    "Waterlogged slab placement failed");
            helper.assertBlockProperty(SUPPORT.above(), SlabBlock.WATERLOGGED, true);
            helper.assertBlockProperty(SUPPORT.above(), RoadLineSlabBlock.FACING, Direction.SOUTH);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arrowsSupportFullBlocksAndAllSlabHeights(GameTestHelper helper) {
        BlockState[] supports = {
                Blocks.STONE.defaultBlockState(), Blocks.GLASS.defaultBlockState(),
                Blocks.OAK_SLAB.defaultBlockState(),
                Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP),
                Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
                ModBlocks.ASPHALT_SLAB.get().defaultBlockState(),
                ModBlocks.ASPHALT_WHITE_STR_SLAB.get().defaultBlockState()
        };
        for (var arrow : ModBlocks.ROAD_ARROWS) {
            for (BlockState support : supports) {
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    helper.setBlock(SUPPORT.above(), Blocks.AIR);
                    helper.setBlock(SUPPORT, support);
                    double top = RoadArrowBlock.isLowerSlab(support) ? 0.5 : 1;
                    helper.assertTrue(place(helper, arrow.get(), SUPPORT, Direction.UP, facing, top).consumesAction(),
                            "Arrow placement failed on " + support);
                    helper.assertBlockPresent(arrow.get(), SUPPORT.above());
                    helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.FACING, facing);
                    helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.LOWERED, top == 0.5);
                    BlockState state = helper.getBlockState(SUPPORT.above());
                    helper.assertTrue(state.getCollisionShape(helper.getLevel(), helper.absolutePos(SUPPORT.above())).isEmpty(),
                            "An arrow must not have collision");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void surfaceFeaturesRetainShapeAndFacing(GameTestHelper helper) {
        for (RoadSurfaceFeatureBlock block : new RoadSurfaceFeatureBlock[]{ModBlocks.ROAD_MANHOLE.get(), ModBlocks.ROAD_DRAIN.get()}) {
            for (RoadSurfaceFeatureBlock.FeatureShape shape : RoadSurfaceFeatureBlock.FeatureShape.values()) {
                BlockState state = block.defaultBlockState().setValue(RoadSurfaceFeatureBlock.FACING, Direction.WEST)
                        .setValue(RoadSurfaceFeatureBlock.SHAPE, shape);
                BlockState restored = NbtUtils.readBlockState(helper.getLevel().holderLookup(Registries.BLOCK),
                        NbtUtils.writeBlockState(state));
                helper.assertTrue(restored.equals(state), "Surface feature state failed NBT round trip");
                BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
                helper.assertTrue(rotated.getValue(RoadSurfaceFeatureBlock.FACING) == Direction.NORTH
                                && rotated.getValue(RoadSurfaceFeatureBlock.SHAPE) == shape,
                        "Surface feature rotation changed shape");
            }
            helper.setBlock(SUPPORT, Blocks.STONE);
            helper.setBlock(SUPPORT.above(), Blocks.AIR);
            helper.assertTrue(place(helper, block, SUPPORT, Direction.UP, Direction.SOUTH, 1, false).consumesAction(),
                    "Regular surface feature placement failed");
            helper.assertBlockProperty(SUPPORT.above(), RoadSurfaceFeatureBlock.SHAPE,
                    RoadSurfaceFeatureBlock.FeatureShape.ROUND);
            helper.setBlock(SUPPORT.above(), Blocks.AIR);
            helper.assertTrue(place(helper, block, SUPPORT, Direction.UP, Direction.SOUTH, 1, true).consumesAction(),
                    "Crouching surface feature placement failed");
            helper.assertBlockProperty(SUPPORT.above(), RoadSurfaceFeatureBlock.SHAPE,
                    RoadSurfaceFeatureBlock.FeatureShape.SQUARE);

            Player player = helper.makeMockPlayer();
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            BlockPos absolute = helper.absolutePos(SUPPORT.above());
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
            BlockState square = helper.getBlockState(SUPPORT.above());
            helper.assertTrue(block.use(square, helper.getLevel(), absolute, player, InteractionHand.MAIN_HAND, hit)
                    .consumesAction(), "Empty-hand crouch interaction must toggle feature shape");
            helper.assertBlockProperty(SUPPORT.above(), RoadSurfaceFeatureBlock.SHAPE,
                    RoadSurfaceFeatureBlock.FeatureShape.ROUND);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arrowsFollowSupportAndDisappearWithoutIt(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.OAK_SLAB);
        helper.assertTrue(place(helper, ModBlocks.ROAD_ARROW_LEFT.get(), SUPPORT, Direction.UP, Direction.WEST, 0.5)
                .consumesAction(), "Arrow on a bottom slab failed");
        helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.LOWERED, true);
        helper.setBlock(SUPPORT, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
        helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.LOWERED, false);
        helper.setBlock(SUPPORT, Blocks.OAK_SLAB);
        helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.LOWERED, true);
        helper.setBlock(SUPPORT, Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, SUPPORT.above());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arrowsRejectWaterUnsupportedAndOccupiedCells(GameTestHelper helper) {
        for (var arrow : ModBlocks.ROAD_ARROWS) {
            helper.setBlock(SUPPORT, Blocks.STONE);
            helper.setBlock(SUPPORT.above(), Blocks.WATER);
            helper.assertTrue(!place(helper, arrow.get(), SUPPORT, Direction.UP, Direction.NORTH, 1).consumesAction(),
                    "An arrow must not be placed underwater");
            helper.setBlock(SUPPORT.above(), Blocks.AIR);
            helper.setBlock(SUPPORT, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true));
            helper.assertTrue(!place(helper, arrow.get(), SUPPORT, Direction.UP, Direction.NORTH, 0.5).consumesAction(),
                    "An arrow must not be placed on a wet slab");
            helper.setBlock(SUPPORT, Blocks.STONE_STAIRS);
            helper.assertTrue(!place(helper, arrow.get(), SUPPORT, Direction.UP, Direction.NORTH, 1).consumesAction(),
                    "A partial support surface must be rejected");
            helper.setBlock(SUPPORT, Blocks.STONE);
            helper.setBlock(SUPPORT.above(), Blocks.STONE);
            helper.assertTrue(!place(helper, arrow.get(), SUPPORT, Direction.UP, Direction.NORTH, 1).consumesAction(),
                    "An occupied anchor must not be overwritten");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arrowAnchorIsSelectableAndBreakable(GameTestHelper helper) {
        for (boolean lowered : new boolean[]{false, true}) {
            helper.setBlock(SUPPORT, lowered ? Blocks.OAK_SLAB : Blocks.STONE);
            helper.setBlock(SUPPORT.above(), Blocks.AIR);
            place(helper, ModBlocks.ROAD_ARROW_STRAIGHT.get(), SUPPORT, Direction.UP, Direction.NORTH, lowered ? 0.5 : 1);
            BlockPos anchor = helper.absolutePos(SUPPORT.above());
            Vec3 start = Vec3.atCenterOf(anchor).add(0, 2, 0);
            Vec3 end = Vec3.atCenterOf(anchor).add(0, -2, 0);
            BlockHitResult hit = helper.getLevel().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, helper.makeMockPlayer()));
            helper.assertTrue(hit.getBlockPos().equals(anchor), "Arrow's center anchor could not be selected");
            helper.destroyBlock(SUPPORT.above());
            helper.assertBlockPresent(Blocks.AIR, SUPPORT.above());
            helper.assertTrue(helper.getBlockState(SUPPORT).is(lowered ? Blocks.OAK_SLAB : Blocks.STONE),
                    "Removing an arrow must not remove its support");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyBlockStatesAndItemsStillResolve(GameTestHelper helper) {
        CompoundTag block = new CompoundTag();
        block.putString("Name", "harmonycraft_mod:asphalt_white_str_block");
        BlockState restored = NbtUtils.readBlockState(helper.getLevel().holderLookup(Registries.BLOCK), block);
        helper.assertTrue(restored.is(ModBlocks.ASPHALT_WHITE_STR_BLOCK.get()), "Legacy full block ID was lost");
        helper.assertTrue(restored.getValue(RoadLineBlock.FACING) == Direction.NORTH, "Missing facing must default north");
        for (SlabType type : SlabType.values()) {
            CompoundTag slab = new CompoundTag();
            slab.putString("Name", "harmonycraft_mod:asphalt_white_str_slab");
            CompoundTag properties = new CompoundTag();
            properties.putString("type", type.getSerializedName());
            properties.putString("waterlogged", "true");
            slab.put("Properties", properties);
            BlockState restoredSlab = NbtUtils.readBlockState(helper.getLevel().holderLookup(Registries.BLOCK), slab);
            helper.assertTrue(restoredSlab.is(ModBlocks.ASPHALT_WHITE_STR_SLAB.get()), "Legacy slab ID was lost");
            helper.assertTrue(restoredSlab.getValue(SlabBlock.TYPE) == type, "Legacy slab type was lost");
            helper.assertTrue(restoredSlab.getValue(SlabBlock.WATERLOGGED), "Legacy waterlogging was lost");
            helper.assertTrue(restoredSlab.getValue(RoadLineSlabBlock.FACING) == Direction.NORTH,
                    "Legacy slab missing facing must default north");
        }
        CompoundTag item = new CompoundTag();
        item.putString("id", "harmonycraft_mod:asphalt_white_str_slab");
        item.putByte("Count", (byte) 12);
        ItemStack restoredItem = ItemStack.of(item);
        helper.assertTrue(restoredItem.is(ModBlocks.ASPHALT_WHITE_STR_SLAB.get().asItem()) && restoredItem.getCount() == 12,
                "Legacy slab item stack must retain its ID and count");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rotationsPreserveSlabProperties(GameTestHelper helper) {
        for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
            BlockState block = road.block().get().defaultBlockState().setValue(RoadLineBlock.FACING, Direction.NORTH);
            helper.assertTrue(block.rotate(Rotation.CLOCKWISE_90).getValue(RoadLineBlock.FACING) == Direction.EAST,
                    "Full road rotation failed");
            helper.assertTrue(block.mirror(Mirror.LEFT_RIGHT).getValue(RoadLineBlock.FACING)
                            == (road.connectsAdjacentEdges() ? Direction.EAST : Direction.SOUTH),
                    "Full road mirror failed");
            BlockState slab = road.slab().get().defaultBlockState().setValue(RoadLineSlabBlock.FACING, Direction.NORTH)
                    .setValue(SlabBlock.TYPE, SlabType.TOP).setValue(SlabBlock.WATERLOGGED, true);
            BlockState rotated = slab.rotate(Rotation.CLOCKWISE_90);
            helper.assertTrue(rotated.getValue(RoadLineSlabBlock.FACING) == Direction.EAST
                    && rotated.getValue(SlabBlock.TYPE) == SlabType.TOP && rotated.getValue(SlabBlock.WATERLOGGED),
                    "Rotation must preserve slab type and waterlogging");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void adjacentLineMirrorsKeepConnectedEdges(GameTestHelper helper) {
        for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
            if (!road.connectsAdjacentEdges()) {
                continue;
            }
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (Mirror mirror : Mirror.values()) {
                    Set<Direction> expected = Set.of(reflect(facing, mirror), reflect(facing.getClockWise(), mirror));
                    BlockState block = road.block().get().defaultBlockState().setValue(RoadLineBlock.FACING, facing);
                    Direction mirrored = block.mirror(mirror).getValue(RoadLineBlock.FACING);
                    helper.assertTrue(Set.of(mirrored, mirrored.getClockWise()).equals(expected),
                            "Mirrored diagonal endpoints are wrong: " + facing + "/" + mirror);
                    helper.assertTrue(block.mirror(mirror).mirror(mirror).equals(block),
                            "Two diagonal mirrors must restore the full block");
                    for (SlabType type : SlabType.values()) {
                        BlockState slab = road.slab().get().defaultBlockState().setValue(RoadLineSlabBlock.FACING, facing)
                                .setValue(SlabBlock.TYPE, type).setValue(SlabBlock.WATERLOGGED, true);
                        BlockState reflected = slab.mirror(mirror);
                        Direction slabFacing = reflected.getValue(RoadLineSlabBlock.FACING);
                        helper.assertTrue(Set.of(slabFacing, slabFacing.getClockWise()).equals(expected)
                                        && reflected.getValue(SlabBlock.TYPE) == type && reflected.getValue(SlabBlock.WATERLOGGED),
                                "Mirrored diagonal slab must preserve endpoints, height and waterlogging");
                        helper.assertTrue(reflected.mirror(mirror).equals(slab),
                                "Two diagonal mirrors must restore the slab");
                    }
                }
            }
        }
        helper.succeed();
    }

    private static Direction reflect(Direction direction, Mirror mirror) {
        boolean reflected = mirror == Mirror.LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z
                || mirror == Mirror.FRONT_BACK && direction.getAxis() == Direction.Axis.X;
        return reflected ? direction.getOpposite() : direction;
    }

    @GameTest(template = "empty")
    public static void mirroredTurnArrowsSwapHandedness(GameTestHelper helper) {
        BlockState left = ModBlocks.ROAD_ARROW_LEFT.get().defaultBlockState()
                .setValue(RoadArrowBlock.FACING, Direction.NORTH).setValue(RoadArrowBlock.LOWERED, true);
        BlockState right = left.mirror(Mirror.LEFT_RIGHT);
        helper.assertTrue(right.is(ModBlocks.ROAD_ARROW_RIGHT.get())
                && right.getValue(RoadArrowBlock.FACING) == Direction.SOUTH
                && right.getValue(RoadArrowBlock.LOWERED), "Mirroring must swap handedness and preserve height");
        helper.assertTrue(right.mirror(Mirror.LEFT_RIGHT).equals(left), "Two mirrors must restore the original arrow");
        helper.assertTrue(left.mirror(Mirror.NONE).equals(left), "No mirror must not swap the arrow");
        helper.assertTrue(left.rotate(Rotation.CLOCKWISE_90).is(ModBlocks.ROAD_ARROW_LEFT.get()),
                "Rotation must not change a left turn into a right turn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void a01All367StatesAnd29IdsStillResolve(GameTestHelper helper) {
        String[] roads = {"asphalt_white_str", "asphalt_white_dashed_line", "asphalt_white_double_line",
                "asphalt_yellow_solid_line", "asphalt_yellow_dashed_line", "asphalt_yellow_double_line",
                "asphalt_stop_line", "asphalt_crosswalk", "asphalt_white_diagonal_line", "asphalt_yellow_diagonal_line",
                "asphalt_white_double_diagonal_line", "asphalt_yellow_double_diagonal_line"};
        int states = roundTripLegacyStates(helper, "asphalt_block") + roundTripLegacyStates(helper, "asphalt_slab");
        for (String road : roads) {
            states += roundTripLegacyStates(helper, road + "_block") + roundTripLegacyStates(helper, road + "_slab");
        }
        for (String arrow : new String[]{"road_arrow_straight", "road_arrow_left", "road_arrow_right"}) {
            states += roundTripLegacyStates(helper, arrow);
        }
        helper.assertTrue(states == 367, "a01's IDs/properties/state count must remain unchanged");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void a02All679StatesAnd53IdsStillResolve(GameTestHelper helper) {
        String[] roads = {"asphalt_white_str", "asphalt_white_dashed_line", "asphalt_white_double_line",
                "asphalt_yellow_solid_line", "asphalt_yellow_dashed_line", "asphalt_yellow_double_line",
                "asphalt_stop_line", "asphalt_crosswalk", "asphalt_white_diagonal_line", "asphalt_yellow_diagonal_line",
                "asphalt_white_double_diagonal_line", "asphalt_yellow_double_diagonal_line",
                "asphalt_white_right_angle_line", "asphalt_yellow_right_angle_line",
                "asphalt_white_double_right_angle_line", "asphalt_yellow_double_right_angle_line",
                "asphalt_white_curved_line", "asphalt_yellow_curved_line",
                "asphalt_white_double_curved_line", "asphalt_yellow_double_curved_line"};
        String[] arrows = {"road_arrow_straight", "road_arrow_left", "road_arrow_right", "road_arrow_straight_left",
                "road_arrow_straight_right", "road_arrow_left_right", "road_arrow_uturn", "road_arrow_straight_uturn",
                "road_arrow_left_uturn", "road_arrow_merge_left", "road_arrow_merge_right"};
        int ids = 2;
        int states = roundTripLegacyStates(helper, "asphalt_block") + roundTripLegacyStates(helper, "asphalt_slab");
        for (String road : roads) {
            states += roundTripLegacyStates(helper, road + "_block") + roundTripLegacyStates(helper, road + "_slab");
            ids += 2;
        }
        for (String arrow : arrows) {
            states += roundTripLegacyStates(helper, arrow);
            ids++;
        }
        helper.assertTrue(states == 679 && ids == 53, "a02's 53 IDs and 679 states must remain unchanged");
        helper.succeed();
    }

    private static int roundTripLegacyStates(GameTestHelper helper, String id) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(HarmonyCraftMod.MOD_ID, id));
        helper.assertTrue(block != null && block != Blocks.AIR, "Lost legacy ID: " + id);
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            BlockState restored = NbtUtils.readBlockState(helper.getLevel().holderLookup(Registries.BLOCK),
                    NbtUtils.writeBlockState(state));
            helper.assertTrue(restored.equals(state), "Lost legacy state properties: " + state);
            CompoundTag item = new CompoundTag();
            item.putString("id", HarmonyCraftMod.MOD_ID + ":" + id);
            item.putByte("Count", (byte) 16);
            ItemStack stack = ItemStack.of(item);
            helper.assertTrue(stack.is(block.asItem()) && stack.getCount() == 16, "Lost legacy item: " + id);
        }
        return block.getStateDefinition().getPossibleStates().size();
    }

    @GameTest(template = "empty")
    public static void allArrowMirrorsPreserveGeometryAndProperties(GameTestHelper helper) {
        for (var arrow : ModBlocks.ROAD_ARROWS) {
            for (BlockState state : arrow.get().getStateDefinition().getPossibleStates()) {
                for (Mirror mirror : Mirror.values()) {
                    BlockState reflected = state.mirror(mirror);
                    helper.assertTrue(reflected.mirror(mirror).equals(state), "Two mirrors must restore " + state);
                    helper.assertTrue(reflected.getValue(RoadArrowBlock.LOWERED) == state.getValue(RoadArrowBlock.LOWERED),
                            "An arrow mirror changed slab height: " + state);
                    if (state.hasProperty(ReversibleRoadArrowBlock.MIRRORED)) {
                        helper.assertTrue(reflected.getValue(ReversibleRoadArrowBlock.MIRRORED)
                                        == (state.getValue(ReversibleRoadArrowBlock.MIRRORED) ^ (mirror != Mirror.NONE)),
                                "Asymmetric U-turn artwork must be reflected, not just rotated");
                    }
                    BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
                    helper.assertTrue(rotated.getBlock() == state.getBlock(), "Rotation must not swap arrow identity");
                }
            }
        }
        var leftPairs = new RoadArrowBlock[]{ModBlocks.ROAD_ARROW_LEFT.get(), ModBlocks.ROAD_ARROW_STRAIGHT_LEFT.get(),
                ModBlocks.ROAD_ARROW_MERGE_LEFT.get()};
        var rightPairs = new RoadArrowBlock[]{ModBlocks.ROAD_ARROW_RIGHT.get(), ModBlocks.ROAD_ARROW_STRAIGHT_RIGHT.get(),
                ModBlocks.ROAD_ARROW_MERGE_RIGHT.get()};
        for (int i = 0; i < leftPairs.length; i++) {
            helper.assertTrue(leftPairs[i].defaultBlockState().mirror(Mirror.FRONT_BACK).is(rightPairs[i]),
                    "Mirror must select the matching right-handed arrow");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mirroredUTurnsKeepReflectionWhenSupportChanges(GameTestHelper helper) {
        for (var arrow : ModBlocks.ROAD_ARROWS) {
            if (!(arrow.get() instanceof ReversibleRoadArrowBlock)) { continue; }
            helper.setBlock(SUPPORT, Blocks.OAK_SLAB);
            helper.setBlock(SUPPORT.above(), arrow.get().defaultBlockState()
                    .setValue(RoadArrowBlock.LOWERED, true).setValue(ReversibleRoadArrowBlock.MIRRORED, true));
            helper.setBlock(SUPPORT, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
            helper.assertBlockProperty(SUPPORT.above(), RoadArrowBlock.LOWERED, false);
            helper.assertBlockProperty(SUPPORT.above(), ReversibleRoadArrowBlock.MIRRORED, true);
            helper.setBlock(SUPPORT, Blocks.AIR);
            helper.assertBlockPresent(Blocks.AIR, SUPPORT.above());
        }
        helper.succeed();
    }

    private static InteractionResult place(GameTestHelper helper, Block block, BlockPos clicked,
                                           Direction face, Direction facing, double surfaceHeight) {
        return place(helper, block, clicked, face, facing, surfaceHeight, false);
    }

    private static InteractionResult place(GameTestHelper helper, Block block, BlockPos clicked,
                                           Direction face, Direction facing, double surfaceHeight, boolean secondaryUse) {
        Player player = helper.makeMockPlayer();
        player.setYRot(facing.toYRot());
        player.setShiftKeyDown(secondaryUse);
        player.getAbilities().instabuild = true;
        ItemStack stack = new ItemStack(block);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(clicked);
        BlockHitResult hit = new BlockHitResult(new Vec3(absolute.getX() + 0.5,
                absolute.getY() + surfaceHeight, absolute.getZ() + 0.5), face, absolute, false);
        return ((BlockItem) stack.getItem()).place(new BlockPlaceContext(helper.getLevel(), player,
                InteractionHand.MAIN_HAND, stack, hit));
    }
}
