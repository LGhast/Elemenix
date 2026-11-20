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

public record ItemMemories(List<ResourceLocation> memories) {
    public static final int MAX = 175;

    public static final Codec<ItemMemories> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("resolvedItems").forGetter(ItemMemories::memories)
            ).apply(instance, ItemMemories::new)
    );

    public ItemMemories withMemories(List<ResourceLocation> newMemories) {
        return new ItemMemories(new ArrayList<>(newMemories));
    }

    public ItemStack firstItem(){
        if(memories == null || memories.isEmpty()){
            return ItemStack.EMPTY;
        }

        try {
            Item item = BuiltInRegistries.ITEM.get(memories.getFirst());
            return new ItemStack(item);
        } catch (Exception e) {
            return new ItemStack(Items.BARRIER);
        }
    }

    public boolean isEmpty(){
        return memories.isEmpty();
    }

    public boolean isFull(){
        return memories.size() >= MAX;
    }
}
