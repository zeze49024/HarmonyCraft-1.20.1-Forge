package com.zeze.harmony.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RoadLineSlabBlock extends SlabBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final boolean adjacentEdges;

    public RoadLineSlabBlock(Properties properties) {
        this(properties, false);
    }

    public RoadLineSlabBlock(Properties properties, boolean adjacentEdges) {
        super(properties);
        this.adjacentEdges = adjacentEdges;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = super.getStateForPlacement(context);
        // Merging keeps the first slab's direction and vanilla waterlogging behavior.
        if (placed == null || context.getLevel().getBlockState(context.getClickedPos()).is(this)) {
            return placed;
        }
        return placed.setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, RoadLineBlock.mirroredFacing(state.getValue(FACING), mirror, adjacentEdges));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
