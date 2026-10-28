package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ComparatorCriterion(int target, int rule, int threshold, int unit) {
    public static final int TARGET_SUM = 6;
    public static final int TARGET_ANY = 7;

    private static final String[] RULE_SYMBOLS = {">", "<", "=", "≥", "≤"};
    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    public static final Codec<ComparatorCriterion> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("target").forGetter(ComparatorCriterion::target),
                    Codec.INT.fieldOf("rule").forGetter(ComparatorCriterion::rule),
                    Codec.INT.fieldOf("threshold").forGetter(ComparatorCriterion::threshold),
                    Codec.INT.fieldOf("unit").forGetter(ComparatorCriterion::unit)
            ).apply(instance, ComparatorCriterion::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ComparatorCriterion> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ComparatorCriterion::target,
                    ByteBufCodecs.VAR_INT, ComparatorCriterion::rule,
                    ByteBufCodecs.VAR_INT, ComparatorCriterion::threshold,
                    ByteBufCodecs.VAR_INT, ComparatorCriterion::unit,
                    ComparatorCriterion::new
            );

    public String buildDisplay(boolean full) {
        String targetPart;
        if (target == TARGET_SUM) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_sum").getString();
        } else if (target == TARGET_ANY) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_any").getString();
        } else {
            targetPart = Elemenix.values()[target].getName();
        }
        long value = (long) threshold * UNIT_MULTIPLIERS[unit];
        String valuePart = full ? String.valueOf(value) : ModUtils.formatNumber(value);
        return targetPart + " " + RULE_SYMBOLS[rule] + " " + valuePart;
    }
}
