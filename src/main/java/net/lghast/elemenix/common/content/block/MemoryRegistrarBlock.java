package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.MemoryRegistrarBlockEntity;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.system.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MemoryRegistrarBlock extends BaseEntityBlock {
    public static final MapCodec<MemoryRegistrarBlock> CODEC = simpleCodec(MemoryRegistrarBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0);

    public MemoryRegistrarBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.5f)
                .noOcclusion()
                .lightLevel(state -> state.getValue(WORKING) ? 6 : 2));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MemoryRegistrarBlockEntity(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isMemorizer(stack)) {
            if (level.getBlockEntity(pos) instanceof MemoryRegistrarBlockEntity blockEntity && !blockEntity.hasMemorizer()) {
                if (level.isClientSide) {
                    return ItemInteractionResult.sidedSuccess(true);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    MemoryRegistrarBlockEntity.PlacementResult result =
                            MemoryRegistrarBlockEntity.checkPlacement(stack, serverPlayer);
                    if (result == MemoryRegistrarBlockEntity.PlacementResult.EMPTY_OR_INVALID) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.elemenix.registrar.empty_or_invalid"), true);
                        return ItemInteractionResult.sidedSuccess(false);
                    }
                    if (result == MemoryRegistrarBlockEntity.PlacementResult.ALL_REGISTERED) {
                        String key = MemoryRegistrarBlockEntity.isBuiltInMemorySource(stack)
                                ? "message.elemenix.registrar.built_in_all_registered"
                                : "message.elemenix.registrar.all_registered";
                        serverPlayer.displayClientMessage(Component.translatable(key), true);
                        return ItemInteractionResult.sidedSuccess(false);
                    }
                    blockEntity.setMemorizer(stack.copyWithCount(1));
                    blockEntity.setOwner(serverPlayer.getUUID());
                    stack.shrink(1);
                    blockEntity.setChanged();
                    serverPlayer.level().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ModSounds.MEMORIZER_PLACED.get(),
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                    return ItemInteractionResult.sidedSuccess(false);
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MemoryRegistrarBlockEntity blockEntity) {
            if (blockEntity.hasMemorizer()) {
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            8, 0.2, 0.2, 0.2, 0.02);
                    ItemStack memorizer = blockEntity.getMemorizer();
                    int count = blockEntity.getRegisteredCount();
                    blockEntity.setMemorizer(ItemStack.EMPTY);
                    blockEntity.setOwner(null);
                    blockEntity.setChanged();
                    serverLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ModSounds.MEMORIZER_REMOVE.get(),
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (count > 0) {
                        player.displayClientMessage(
                                Component.translatable("message.elemenix.registrar.aborted", count), true);
                    } else {
                        player.displayClientMessage(
                                Component.translatable("message.elemenix.registrar.invalid"), true);
                    }
                    if (!player.getInventory().add(memorizer)) {
                        player.drop(memorizer, false);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ItemStack stack = new ItemStack(state.getBlock().asItem());
                    if (!serverPlayer.getInventory().add(stack)) {
                        serverPlayer.drop(stack, false);
                    }
                }
                level.destroyBlock(pos, false, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MemoryRegistrarBlockEntity registrar && registrar.hasMemorizer()) {
                Block.popResource(level, pos, registrar.getMemorizer());
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
        BlockEntityType<?> type = ModBlockEntities.MEMORY_REGISTRAR.get();
        if (type != blockEntityType) {
            return null;
        }
        BlockEntityTicker<MemoryRegistrarBlockEntity> ticker =
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1);
        return (BlockEntityTicker<T>) ticker;
    }

    private static boolean isMemorizer(ItemStack stack) {
        return stack.getItem() instanceof MemorizerItem
                || stack.getItem() instanceof WitchcraftMemorizerItem
                || MemoryRegistrarBlockEntity.isBuiltInMemorySource(stack);
    }
}
