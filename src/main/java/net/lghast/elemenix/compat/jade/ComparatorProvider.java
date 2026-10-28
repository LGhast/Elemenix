package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.content.blockentity.ElemenicComparatorBlockEntity;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ComparatorProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    private static final String[] RULE_SYMBOLS = {">", "<", "=", "≥", "≤"};
    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ElemenicComparatorBlockEntity comparator) {
            BlockState state = comparator.getBlockState();
            data.putInt("Target", state.getValue(ElemenicComparatorBlock.TARGET));
            data.putInt("Rule", state.getValue(ElemenicComparatorBlock.RULE));
            data.putInt("Threshold", comparator.getThreshold());
            data.putInt("Unit", comparator.getUnit());
            data.putBoolean("Passing", comparator.isOwnCriterionPassing());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("Target")) return;

        int target = data.getInt("Target");
        int rule = data.getInt("Rule");
        int threshold = data.getInt("Threshold");
        int unit = data.getInt("Unit");
        boolean passing = data.getBoolean("Passing");

        tooltip.add(Component.translatable("gui.elemenix.comparator_criterion",
                buildCriterion(target, rule, threshold, unit)).withStyle(ChatFormatting.GRAY));

        String judgment = passing
                ? Component.translatable("gui.elemenix.comparator_true").getString()
                : Component.translatable("gui.elemenix.comparator_false").getString();
        tooltip.add(Component.translatable("gui.elemenix.comparator_judgment", judgment)
                .withStyle(passing ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static String buildCriterion(int target, int rule, int threshold, int unit) {
        String targetPart;
        if (target == ElemenicComparatorBlockEntity.TARGET_SUM) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_sum").getString();
        } else if (target == ElemenicComparatorBlockEntity.TARGET_ANY) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_any").getString();
        } else {
            targetPart = Elemenix.values()[target].getName();
        }

        long value = (long) threshold * UNIT_MULTIPLIERS[unit];
        String valuePart = Screen.hasAltDown() ? String.valueOf(value) : ModUtils.formatNumber(value);

        return targetPart + " " + RULE_SYMBOLS[rule] + " " + valuePart;
    }

    @Override
    public ResourceLocation getUid() {
        return ElemenixJadePlugin.UID_COMPARATOR;
    }
}
