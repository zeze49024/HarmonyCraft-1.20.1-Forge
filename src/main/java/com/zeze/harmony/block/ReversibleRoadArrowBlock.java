package com.zeze.harmony.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** New asymmetric U-turn arrows need a reflected artwork state for structure mirroring. */
public class ReversibleRoadArrowBlock extends RoadArrowBlock {
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

    public ReversibleRoadArrowBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MIRRORED, false));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState reflected = super.mirror(state, mirror);
        return mirror == Mirror.NONE ? reflected : reflected.cycle(MIRRORED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MIRRORED);
    }
}
