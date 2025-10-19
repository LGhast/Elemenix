package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record ElemenicStorage(long[] elemenix) {
    public static final Codec<ElemenicStorage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf()
                            .validate(list ->
                                    list.size() == 6
                                            ? DataResult.success(list)
                                            : DataResult.error(() -> "Elemenix array must have exactly 6 elements")
                            )
                            .xmap(
                                    list -> {
                                        long[] array = new long[6];
                                        for (int i = 0; i < 6; i++) {
                                            array[i] = list.get(i);
                                        }
                                        return array;
                                    },
                                    array -> java.util.Arrays.stream(array).boxed().collect(java.util.stream.Collectors.toList())
                            )
                            .fieldOf("elemenix")
                            .forGetter(ElemenicStorage::elemenix)
            ).apply(instance, ElemenicStorage::new)
    );

    public long organix() { return elemenix[0]; }
    public long terrix() { return elemenix[1]; }
    public long flumix() { return elemenix[2]; }
    public long metallix() { return elemenix[3]; }
    public long energix() { return elemenix[4]; }
    public long arcanix() { return elemenix[5]; }

    public ElemenicStorage withElemenix(long[] newElemenix) {
        return new ElemenicStorage(newElemenix.clone());
    }

    public Component toComponentFormer() {
        return Component.literal(ModUtils.formatNumber(organix(), "O:%s  ")).withColor(Elemenix.ORGANIX.getColor())
                        .append(Component.literal(ModUtils.formatNumber(terrix(), "T:%s  ")).withColor(Elemenix.TERRIX.getColor()))
                        .append(Component.literal(ModUtils.formatNumber(flumix(), "F:%s")).withColor(Elemenix.FLUMIX.getColor()));
    }

    public Component toComponentLatter() {
        return Component.literal(ModUtils.formatNumber(metallix(), "M:%s  ")).withColor(Elemenix.METALLIX.getColor())
                .append(Component.literal(ModUtils.formatNumber(energix(), "E:%s  ")).withColor(Elemenix.ENERGIX.getColor()))
                .append(Component.literal(ModUtils.formatNumber(arcanix(), "A:%s")).withColor(Elemenix.ARCANIX.getColor()));
    }
}

