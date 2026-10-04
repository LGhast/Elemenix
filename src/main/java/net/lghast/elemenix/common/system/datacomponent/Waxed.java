package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public record Waxed(boolean waxed, Optional<UUID> ownerUuid, Optional<String> ownerName) {
    public static final Codec<Waxed> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("waxed").forGetter(Waxed::waxed),
                    Codec.STRING.xmap(UUID::fromString, UUID::toString)
                            .optionalFieldOf("owner_uuid").forGetter(Waxed::ownerUuid),
                    Codec.STRING.optionalFieldOf("owner_name").forGetter(Waxed::ownerName)
            ).apply(instance, Waxed::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Waxed> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    Waxed::waxed,
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)),
                    Waxed::ownerUuid,
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                    Waxed::ownerName,
                    Waxed::new
            );

    public static Waxed unwaxed() {
        return new Waxed(false, Optional.empty(), Optional.empty());
    }

    public static Waxed waxedBy(Player player) {
        return new Waxed(true, Optional.of(player.getUUID()), Optional.of(player.getGameProfile().getName()));
    }

    public boolean hasOwner() {
        return ownerUuid.isPresent();
    }

    public boolean isOwnedBy(Player player) {
        return ownerUuid.isPresent() && ownerUuid.get().equals(player.getUUID());
    }
}
