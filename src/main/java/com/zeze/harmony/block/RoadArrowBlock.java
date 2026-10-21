package com.zeze.harmony.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One logical anchor; the decorative plane intentionally extends beyond that anchor. */
public class RoadArrowBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty LOWERED = BooleanProperty.create("lowered");
    private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 0.125, 16);
    // Extend into the logical cell so vanilla ray tracing can select a lowered arrow.
    private static final VoxelShape LOWERED_OUTLINE = Block.box(0, -8, 0, 16, 0.125, 16);

    public RoadArrowBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LOWERED, false));
    }

    public static boolean isLowerSlab(BlockState support) {
        return support.getBlock() instanceof SlabBlock && support.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    public static boolean hasSupport(BlockGetter level, BlockPos supportPos) {
        BlockState support = level.getBlockState(supportPos);
        return support.getFluidState().isEmpty() && (support.getBlock() instanceof SlabBlock
                || support.isFaceSturdy(level, supportPos, Direction.UP, SupportType.FULL));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (!context.getLevel().getFluidState(pos).isEmpty() || !hasSupport(context.getLevel(), pos.below())) {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection())
                .setValue(LOWERED, isLowerSlab(context.getLevel().getBlockState(pos.below())));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSupport(level, pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return hasSupport(level, pos.below()) ? state.setValue(LOWERED, isLowerSlab(neighbor))
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LOWERED) ? LOWERED_OUTLINE : OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = rotate(state, mirror.getRotation(state.getValue(FACING)));
        if (mirror != Mirror.NONE) {
            // A reflected left-turn symbol is a right-turn symbol, and vice versa.
            if (state.is(ModBlocks.ROAD_ARROW_LEFT.get())) {
                return ModBlocks.ROAD_ARROW_RIGHT.get().defaultBlockState()
                        .setValue(FACING, mirrored.getValue(FACING)).setValue(LOWERED, state.getValue(LOWERED));
            }
            if (state.is(ModBlocks.ROAD_ARROW_RIGHT.get())) {
                return ModBlocks.ROAD_ARROW_LEFT.get().defaultBlockState()
                        .setValue(FACING, mirrored.getValue(FACING)).setValue(LOWERED, state.getValue(LOWERED));
            }
            RoadArrowBlock paired = state.is(ModBlocks.ROAD_ARROW_STRAIGHT_LEFT.get()) ? ModBlocks.ROAD_ARROW_STRAIGHT_RIGHT.get()
                    : state.is(ModBlocks.ROAD_ARROW_STRAIGHT_RIGHT.get()) ? ModBlocks.ROAD_ARROW_STRAIGHT_LEFT.get()
                    : state.is(ModBlocks.ROAD_ARROW_MERGE_LEFT.get()) ? ModBlocks.ROAD_ARROW_MERGE_RIGHT.get()
                    : state.is(ModBlocks.ROAD_ARROW_MERGE_RIGHT.get()) ? ModBlocks.ROAD_ARROW_MERGE_LEFT.get() : null;
            if (paired != null) {
                return paired.defaultBlockState().setValue(FACING, mirrored.getValue(FACING))
                        .setValue(LOWERED, state.getValue(LOWERED));
            }
        }
        return mirrored;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOWERED);
    }
}
