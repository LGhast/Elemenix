package net.lghast.elemenix.client.cache;

import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientRegistrarCache {
    private static Set<ResourceLocation> items = new HashSet<>();
    private static Set<PotionMemoryData.Entry> potions = new HashSet<>();

    public static void update(List<ResourceLocation> newItems, List<PotionMemoryData.Entry> newPotions) {
        items = new HashSet<>(newItems);
        potions = new HashSet<>(newPotions);
    }

    public static Set<ResourceLocation> getItems() {
        return items;
    }

    public static Set<PotionMemoryData.Entry> getPotions() {
        return potions;
    }

    private static boolean showUnregistered = true;
    private static boolean putUnregisteredLast = true;
    private static boolean sortedByMod = true;
    private static boolean showPotionlike = true;

    public static boolean isShowUnregistered() {
        return showUnregistered;
    }

    public static void setShowUnregistered(boolean value) {
        showUnregistered = value;
    }

    public static boolean isPutUnregisteredLast() {
        return putUnregisteredLast;
    }

    public static void setPutUnregisteredLast(boolean value) {
        putUnregisteredLast = value;
    }

    public static boolean isSortedByMod() {
        return sortedByMod;
    }

    public static void setSortedByMod(boolean value) {
        sortedByMod = value;
    }

    public static boolean isPotionlikeShown() {
        return showPotionlike;
    }

    public static void setPotionlikeShown(boolean value) {
        showPotionlike = value;
    }
}
