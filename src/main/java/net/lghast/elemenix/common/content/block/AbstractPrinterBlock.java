package net.lghast.elemenix.common.content.block;

import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AbstractPrinterBlock extends BaseEntityBlock {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");
    public static final BooleanProperty HAS_MEMORIZER = BooleanProperty.create("has_memorizer");

    private static final VoxelShape FULL_BLOCK = Shapes.block();

    protected AbstractPrinterBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f).requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> state.getValue(WORKING) || state.getValue(DISABLED) ? 6 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WORKING, false)
                .setValue(DISABLED, false)
                .setValue(HAS_MEMORIZER, false));
    }

    protected abstract BlockEntityType<? extends AbstractPrinterBlockEntity> getBlockEntityType();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING, DISABLED, HAS_MEMORIZER);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
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
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AbstractPrinterBlockEntity blockEntity) {
            player.openMenu(blockEntity, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractPrinterBlockEntity printer) {
                Containers.dropContents(level, pos, printer);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        BlockEntityType<?> type = getBlockEntityType();
        if (type != blockEntityType) {
            return null;
        }
        BlockEntityTicker<AbstractPrinterBlockEntity> ticker =
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1);
        return (BlockEntityTicker<T>) ticker;
    }
}
