package com.zeze.harmony.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Asphalt surface feature with one stable block ID and selectable round/square artwork. */
public class RoadSurfaceFeatureBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<FeatureShape> SHAPE = EnumProperty.create("shape", FeatureShape.class);

    public RoadSurfaceFeatureBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(SHAPE, FeatureShape.ROUND));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection())
                .setValue(SHAPE, context.isSecondaryUseActive() ? FeatureShape.SQUARE : FeatureShape.ROUND);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            FeatureShape next = state.getValue(SHAPE) == FeatureShape.ROUND ? FeatureShape.SQUARE : FeatureShape.ROUND;
            level.setBlock(pos, state.setValue(SHAPE, next), Block.UPDATE_ALL);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    public enum FeatureShape implements StringRepresentable {
        ROUND("round"),
        SQUARE("square");

        private final String serializedName;

        FeatureShape(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
