package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Waxed(boolean waxed) {
    public static final Codec<Waxed> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("waxed").forGetter(Waxed::waxed)
            ).apply(instance, Waxed::new)
    );

    public Waxed withWaxed(boolean newWaxed) {
        return new Waxed(newWaxed);
    }
}
