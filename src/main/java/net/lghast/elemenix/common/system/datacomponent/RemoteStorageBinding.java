package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record RemoteStorageBinding(Optional<GlobalPos> boundPos) {
    public static final RemoteStorageBinding EMPTY = new RemoteStorageBinding(Optional.empty());

    public static final Codec<RemoteStorageBinding> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    GlobalPos.CODEC.optionalFieldOf("bound_pos").forGetter(RemoteStorageBinding::boundPos)
            ).apply(instance, RemoteStorageBinding::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteStorageBinding> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
                    RemoteStorageBinding::boundPos,
                    RemoteStorageBinding::new
            );

    public boolean isBound() {
        return boundPos.isPresent();
    }

    public static RemoteStorageBinding of(GlobalPos pos) {
        return new RemoteStorageBinding(Optional.of(pos));
    }
}
