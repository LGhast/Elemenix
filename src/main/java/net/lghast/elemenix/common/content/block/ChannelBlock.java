package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Directional, point-to-point conduit linking an {@link OutletBlock} to an elemenix input block.
 * FACING is the output direction (where the flow exits); INPUT is the input direction (where the flow enters).
 * A straight run has INPUT == FACING.getOpposite(); an elbow has INPUT perpendicular to FACING.
 * A channel has exactly one input and one output, so it cannot branch.
 */
@ParametersAreNonnullByDefault
public class ChannelBlock extends BaseEntityBlock {
    public static final MapCodec<ChannelBlock> CODEC = simpleCodec(ChannelBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty INPUT = DirectionProperty.create("input");

    private static final VoxelShape CENTER = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape DOWN_SEGMENT = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    private static final VoxelShape UP_SEGMENT = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape NORTH_SEGMENT = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape SOUTH_SEGMENT = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape EAST_SEGMENT = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape WEST_SEGMENT = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);

    public ChannelBlock(Properties properties) {
        super(properties.sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(INPUT, Direction.SOUTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INPUT);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean shouldPlaceOver(BlockState target, ItemStack held) {
        if (held.getItem() instanceof BlockItem blockItem) {
            Block heldBlock = blockItem.getBlock();
            if (heldBlock instanceof ChannelBlock) {
                return target.getBlock() instanceof OutletBlock || target.is(ModTags.ELEMENIX_INPUTS);
            }
            if (heldBlock instanceof OutletBlock) {
                return target.is(ModTags.ELEMENIX_OUTPUTS);
            }
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        Direction input = facing.getOpposite();
        BlockPos pos = context.getClickedPos();

        if (!canConnectTo(context.getLevel().getBlockState(pos.relative(facing.getOpposite())))) {
            for (Direction d : Direction.values()) {
                if (d == facing || d == facing.getOpposite()) continue;
                if (canConnectTo(context.getLevel().getBlockState(pos.relative(d)))) {
                    input = d;
                    break;
                }
            }
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(INPUT, input);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                              LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        Direction input = state.getValue(INPUT);

        if (canConnectTo(neighborState)) {
            if (input == facing.getOpposite() && direction != facing && direction != facing.getOpposite()) {
                boolean inputConnected = canConnectTo(level.getBlockState(pos.relative(input)));
                boolean outputConnected = canConnectTo(level.getBlockState(pos.relative(facing)));
                if (!(inputConnected && outputConnected)) {
                    facing = direction;
                }
            }
        } else {
            if (direction == facing && input != facing.getOpposite()) {
                facing = input.getOpposite();
            }
        }

        if (facing != state.getValue(FACING)) {
            return state.setValue(FACING, facing);
        }
        return state;
    }

    private boolean canConnectTo(BlockState neighbor) {
        Block block = neighbor.getBlock();
        return block instanceof ChannelBlock
                || block instanceof OutletBlock
                || neighbor.is(ModTags.ELEMENIX_INPUTS);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(CENTER,
                Shapes.or(segmentFor(state.getValue(FACING)), segmentFor(state.getValue(INPUT))));
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private static VoxelShape segmentFor(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN_SEGMENT;
            case UP -> UP_SEGMENT;
            case NORTH -> NORTH_SEGMENT;
            case SOUTH -> SOUTH_SEGMENT;
            case EAST -> EAST_SEGMENT;
            case WEST -> WEST_SEGMENT;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
