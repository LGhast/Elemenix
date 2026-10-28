package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.content.block.ChannelBlock;
import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.content.block.OutletBlock;
import net.lghast.elemenix.common.content.blockentity.ElemenicComparatorBlockEntity;
import net.lghast.elemenix.common.content.blockentity.OutletBlockEntity;
import net.lghast.elemenix.common.system.datacomponent.ComparatorCriterion;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        boolean shift = player.isShiftKeyDown();

        if (state.getBlock() instanceof ChannelBlock) {
            if (!level.isClientSide) {
                Direction newFacing = state.getValue(ChannelBlock.INPUT);
                Direction newInput = state.getValue(ChannelBlock.FACING);
                level.setBlock(pos, state.setValue(ChannelBlock.FACING, newFacing)
                        .setValue(ChannelBlock.INPUT, newInput), Block.UPDATE_ALL);
                level.playSound(null, pos, ModSounds.WRENCH.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getBlock() instanceof OutletBlock) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof OutletBlockEntity outlet) {
                OutletBlockEntity.OutletPath path = outlet.getPath();
                if (path.valid() && path.source() != null && path.target() != null) {
                    Component sourceName = Component.translatable(path.source().getDescriptionId());
                    Component targetName = Component.translatable(path.target().getDescriptionId());
                    player.displayClientMessage(Component.translatable("gui.elemenix.outlet_path", sourceName, targetName), true);
                } else {
                    player.displayClientMessage(Component.translatable("gui.elemenix.outlet_path_invalid"), true);
                }
                level.playSound(null, pos, ModSounds.WRENCH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getBlock() instanceof ElemenicComparatorBlock) {
            if (shift) {
                if (!level.isClientSide) {
                    recordCriterion(stack, state, level, pos);
                    player.displayClientMessage(Component.translatable("message.elemenix.wrench.criterion_recorded"), true);
                    level.playSound(null, pos, ModSounds.WRENCH_RECORD.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            } else {
                ComparatorCriterion criterion = getCriterion(stack);
                if (criterion != null) {
                    if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElemenicComparatorBlockEntity comparator) {
                        comparator.setCriterion(criterion.target(), criterion.rule(), criterion.threshold(), criterion.unit());
                        level.playSound(null, pos, ModSounds.WRENCH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
        }

        if (shift) {
            if (!level.isClientSide && hasCriterion(stack)) {
                clearCriterion(stack);
                player.displayClientMessage(Component.translatable("message.elemenix.wrench.criterion_cleared"), true);
                level.playSound(null, pos, ModSounds.WRENCH_RECORD.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        ComparatorCriterion criterion = getCriterion(stack);
        if (criterion != null) {
            components.add(Component.translatable("tooltip.elemenix.wrench.criterion").withStyle(ChatFormatting.GRAY));
            components.add(Component.literal(criterion.buildDisplay(Screen.hasAltDown())).withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasCriterion(stack) || super.isFoil(stack);
    }

    private static ComparatorCriterion getCriterion(ItemStack stack) {
        return stack.get(ModDataComponents.COMPARATOR_CRITERION.get());
    }

    private static boolean hasCriterion(ItemStack stack) {
        return stack.has(ModDataComponents.COMPARATOR_CRITERION.get());
    }

    private static void clearCriterion(ItemStack stack) {
        stack.remove(ModDataComponents.COMPARATOR_CRITERION.get());
    }

    private static void recordCriterion(ItemStack stack, BlockState state, Level level, BlockPos pos) {
        int target = state.getValue(ElemenicComparatorBlock.TARGET);
        int rule = state.getValue(ElemenicComparatorBlock.RULE);
        int threshold = 0;
        int unit = 0;
        if (level.getBlockEntity(pos) instanceof ElemenicComparatorBlockEntity comparator) {
            threshold = comparator.getThreshold();
            unit = comparator.getUnit();
        }
        stack.set(ModDataComponents.COMPARATOR_CRITERION.get(),
                new ComparatorCriterion(target, rule, threshold, unit));
    }
}
