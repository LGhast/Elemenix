package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.OutletBlockEntity;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.system.ModStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.lghast.elemenix.common.system.menu.OutletMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Extracts elemenix from an adjacent elemenix-output block and pushes it along a
 * {@link ChannelBlock} network to a single input block. FACING constrains the flow
 * direction; DISABLED locks the outlet (redstone or open GUI) and WORKING marks active output.
 */
@ParametersAreNonnullByDefault
public class OutletBlock extends BaseEntityBlock {
    public static final MapCodec<OutletBlock> CODEC = simpleCodec(OutletBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");

    private static final VoxelShape SHAPE_UP = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0),
            Block.box(6.0, 5.0, 6.0, 10.0, 16.0, 10.0)
    );
    private static final VoxelShape SHAPE_DOWN = Shapes.or(
            Block.box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0),
            Block.box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0)
    );
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(2.0, 2.0, 11.0, 14.0, 14.0, 16.0),
            Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 11.0)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 5.0),
            Block.box(6.0, 6.0, 5.0, 10.0, 10.0, 16.0)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0.0, 2.0, 2.0, 5.0, 14.0, 14.0),
            Block.box(5.0, 6.0, 6.0, 16.0, 10.0, 10.0)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(11.0, 2.0, 2.0, 16.0, 14.0, 14.0),
            Block.box(0.0, 6.0, 6.0, 11.0, 10.0, 10.0)
    );

    public OutletBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.5f).requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(WORKING) || state.getValue(DISABLED) ? 6 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false)
                .setValue(DISABLED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING, DISABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OutletBlockEntity(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (ChannelBlock.shouldPlaceOver(state, stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof OutletBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) -> new OutletMenu(containerId, blockEntity),
                    Component.translatable("container.elemenix.elemenic_outlet")
            ), pos);
            player.awardStat(ModStats.OUTLET_INTERACTIONS.get());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.OUTLET.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }
}
