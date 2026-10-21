package com.zeze.harmony.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Facing is the direction of travel, not the direction of a transverse stripe. */
public class RoadLineBlock extends HorizontalDirectionalBlock {
    private final boolean adjacentEdges;

    public RoadLineBlock(Properties properties) {
        this(properties, false);
    }

    public RoadLineBlock(Properties properties, boolean adjacentEdges) {
        super(properties);
        this.adjacentEdges = adjacentEdges;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirroredFacing(state.getValue(FACING), mirror, adjacentEdges));
    }

    static Direction mirroredFacing(Direction facing, Mirror mirror, boolean adjacentEdges) {
        Direction reflected = mirror.getRotation(facing).rotate(facing);
        // Diagonals and corners join facing + clockwise; reflection reverses that ordering.
        return adjacentEdges && mirror != Mirror.NONE ? reflected.getCounterClockWise() : reflected;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
