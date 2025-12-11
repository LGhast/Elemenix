package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record AnalyzerUuid(UUID uuid) {
    public static final Codec<AnalyzerUuid> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING
                            .xmap(
                                    UUID::fromString,
                                    UUID::toString
                            )
                            .fieldOf("uuid")
                            .forGetter(AnalyzerUuid::uuid)
            ).apply(instance, AnalyzerUuid::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AnalyzerUuid> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8
                            .map(
                                    UUID::fromString,
                                    UUID::toString
                            ),
                    AnalyzerUuid::uuid,
                    AnalyzerUuid::new
            );

    public static AnalyzerUuid createRandom() {
        return new AnalyzerUuid(UUID.randomUUID());
    }
}
