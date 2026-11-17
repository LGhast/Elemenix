package net.lghast.elemenix.common.system.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-player collection of registered items. Regular items are keyed by item id;
 * potion-like items are keyed by the (item, potion) pair so each potion counts as a distinct entry.
 * Immutable: mutators return a new instance so attachment replacement is always explicit.
 */
public record RegistrarCollection(Set<ResourceLocation> items, Set<PotionMemoryData.Entry> potions) {
    public static final Codec<RegistrarCollection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf()
                            .xmap(RegistrarCollection::toItemSet, RegistrarCollection::toItemList)
                            .fieldOf("items").forGetter(RegistrarCollection::items),
                    PotionMemoryData.Entry.CODEC.listOf()
                            .xmap(RegistrarCollection::toPotionlikeSet, RegistrarCollection::toPotionlikeList)
                            .fieldOf("potions").forGetter(RegistrarCollection::potions)
            ).apply(instance, RegistrarCollection::new)
    );

    public RegistrarCollection {
        items = Set.copyOf(items);
        potions = Set.copyOf(potions);
    }

    public static RegistrarCollection empty() {
        return new RegistrarCollection(Set.of(), Set.of());
    }

    public boolean containsItem(ResourceLocation id) {
        return items.contains(id);
    }

    public boolean containsPotion(PotionMemoryData.Entry entry) {
        return potions.contains(entry);
    }

    public RegistrarCollection withItem(ResourceLocation id) {
        Set<ResourceLocation> newItems = new HashSet<>(items);
        newItems.add(id);
        return new RegistrarCollection(newItems, potions);
    }

    public RegistrarCollection withPotion(PotionMemoryData.Entry entry) {
        Set<PotionMemoryData.Entry> newPotions = new HashSet<>(potions);
        newPotions.add(entry);
        return new RegistrarCollection(items, newPotions);
    }

    public int size() {
        return items.size() + potions.size();
    }

    private static Set<ResourceLocation> toItemSet(List<ResourceLocation> list) {
        return new HashSet<>(list);
    }

    private static List<ResourceLocation> toItemList(Set<ResourceLocation> set) {
        return new ArrayList<>(set);
    }

    private static Set<PotionMemoryData.Entry> toPotionlikeSet(List<PotionMemoryData.Entry> list) {
        return new HashSet<>(list);
    }

    private static List<PotionMemoryData.Entry> toPotionlikeList(Set<PotionMemoryData.Entry> set) {
        return new ArrayList<>(set);
    }
}
