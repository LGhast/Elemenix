package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record UuidData(UUID uuid) {
    public static final Codec<UuidData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING
                            .xmap(
                                    UUID::fromString,
                                    UUID::toString
                            )
                            .fieldOf("uuid")
                            .forGetter(UuidData::uuid)
            ).apply(instance, UuidData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UuidData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8
                            .map(
                                    UUID::fromString,
                                    UUID::toString
                            ),
                    UuidData::uuid,
                    UuidData::new
            );

    public static UuidData createRandom() {
        return new UuidData(UUID.randomUUID());
    }
}
