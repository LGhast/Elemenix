package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PotionMemories(List<Entry> entries) {
    public static final int MAX = 175;

    public record Entry(ResourceLocation itemId, ResourceLocation potionId) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("item").forGetter(Entry::itemId),
                        ResourceLocation.CODEC.fieldOf("potion").forGetter(Entry::potionId)
                ).apply(instance, Entry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::itemId,
                ResourceLocation.STREAM_CODEC, Entry::potionId,
                Entry::new
        );
    }

    public static final Codec<PotionMemories> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Entry.CODEC.listOf().fieldOf("entries").forGetter(PotionMemories::entries)
            ).apply(instance, PotionMemories::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionMemories> STREAM_CODEC =
            StreamCodec.composite(
                    Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    PotionMemories::entries,
                    PotionMemories::new
            );

    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }

    public boolean isFull() {
        return entries.size() >= MAX;
    }

    public PotionMemories withEntries(List<Entry> newEntries) {
        return new PotionMemories(new ArrayList<>(newEntries));
    }
}
