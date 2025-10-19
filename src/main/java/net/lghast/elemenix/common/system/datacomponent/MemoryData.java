package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record MemoryData(List<ResourceLocation> resolvedItems) {
    public static final Codec<MemoryData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("resolvedItems").forGetter(MemoryData::resolvedItems)
            ).apply(instance, MemoryData::new)
    );

    public MemoryData withResolvedItems(List<ResourceLocation> newResolvedItems) {
        return new MemoryData(new ArrayList<>(newResolvedItems));
    }
}
