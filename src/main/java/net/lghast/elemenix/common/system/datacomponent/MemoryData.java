package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public record MemoryData(List<ResourceLocation> resolvedItems) {
    public static final int MAX = 147;

    public static final Codec<MemoryData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("resolvedItems").forGetter(MemoryData::resolvedItems)
            ).apply(instance, MemoryData::new)
    );

    public MemoryData withResolvedItems(List<ResourceLocation> newResolvedItems) {
        return new MemoryData(new ArrayList<>(newResolvedItems));
    }

    public ItemStack firstItem(){
        if(resolvedItems == null || resolvedItems.isEmpty()){
            return ItemStack.EMPTY;
        }

        try {
            Item item = BuiltInRegistries.ITEM.get(resolvedItems.getFirst());
            return new ItemStack(item);
        } catch (Exception e) {
            return new ItemStack(Items.BARRIER);
        }
    }

    public boolean isEmpty(){
        return resolvedItems.isEmpty();
    }

    public boolean isFull(){
        return resolvedItems.size() >= MAX;
    }
}
