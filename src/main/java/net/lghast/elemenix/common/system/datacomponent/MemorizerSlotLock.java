package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MemorizerSlotLock(List<Entry> entries) {
    public static final MemorizerSlotLock EMPTY = new MemorizerSlotLock(List.of());
    public static final int NO_STYLE = -1;

    public record Entry(int slot, ResourceLocation kind, int style) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("slot").forGetter(Entry::slot),
                        ResourceLocation.CODEC.fieldOf("kind").forGetter(Entry::kind),
                        Codec.INT.fieldOf("style").forGetter(Entry::style)
                ).apply(instance, Entry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::slot,
                ResourceLocation.STREAM_CODEC, Entry::kind,
                ByteBufCodecs.VAR_INT, Entry::style,
                Entry::new
        );
    }

    public static final Codec<MemorizerSlotLock> CODEC = Entry.CODEC.listOf()
            .xmap(MemorizerSlotLock::new, MemorizerSlotLock::entries);

    public static final StreamCodec<RegistryFriendlyByteBuf, MemorizerSlotLock> STREAM_CODEC =
            StreamCodec.composite(
                    Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    MemorizerSlotLock::entries,
                    MemorizerSlotLock::new
            );

    public Entry getEntry(int slot) {
        for (Entry entry : entries) {
            if (entry.slot() == slot) {
                return entry;
            }
        }
        return null;
    }
}
