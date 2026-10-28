package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TankBlockEntity;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.register.system.ModStats;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.lghast.elemenix.common.system.menu.TankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
public class TankBlock extends BaseEntityBlock {
    public static final MapCodec<TankBlock> CODEC = simpleCodec(TankBlock::new);
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");

    public TankBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0f).requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(DISABLED) ? 6 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(DISABLED, false));
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
        builder.add(DISABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(DISABLED, level.hasNeighborSignal(pos)), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean disabled = level.hasNeighborSignal(pos);
            if (state.getValue(DISABLED) != disabled) {
                level.setBlock(pos, state.setValue(DISABLED, disabled), Block.UPDATE_ALL);
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof TankBlockEntity tank && tank.isEmpty()) {
            tank.setSkipDrop(true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TankBlockEntity tank && !tank.shouldSkipDrop()) {
            long[] stored = tank.getStoredAmounts();
            boolean hasAny = false;
            for (long value : stored) {
                if (value != 0L) {
                    hasAny = true;
                    break;
                }
            }
            ItemStack stack = new ItemStack(this);
            if (hasAny) {
                CompoundTag tag = new CompoundTag();
                tag.putLongArray("ElemenixStorage", stored);
                BlockItem.setBlockEntityData(stack, ModBlockEntities.TANK.get(), tag);
            }
            Block.popResource(level, pos, stack);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
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
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TankBlockEntity blockEntity) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) -> new TankMenu(containerId, blockEntity),
                    Component.translatable("container.elemenix.elemenic_tank")
            ), pos);
            player.awardStat(ModStats.TANK_INTERACTIONS.get());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public static long[] getStoredElemenix(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        long[] stored = customData.copyTag().getLongArray("ElemenixStorage");
        return stored.length == 6 ? stored : new long[6];
    }

    public static void setStoredElemenix(ItemStack stack, long[] elemenix) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("ElemenixStorage", elemenix);
        BlockItem.setBlockEntityData(stack, ModBlockEntities.TANK.get(), tag);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (ClientConfig.SHOW_STORAGE_TOOLTIPS.get()) {
            ElemenicStorage storage = new ElemenicStorage(getStoredElemenix(stack));
            if (!storage.isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.elemenix.analyzer_storage").withStyle(ChatFormatting.GRAY));
                if (Screen.hasAltDown()) {
                    tooltipComponents.add(storage.toComponentFormerWhole());
                    tooltipComponents.add(storage.toComponentLatterWhole());
                } else {
                    tooltipComponents.add(storage.toComponentFormer());
                    tooltipComponents.add(storage.toComponentLatter());
                }
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
