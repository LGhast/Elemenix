package net.lghast.elemenix.utils.elemenix;

import net.lghast.elemenix.Elemenics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PotionElemenixInfo {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation WATER_POTION_ID = ResourceLocation.parse("minecraft:water");

    private static final Map<ResourceLocation, Constituents> DIRECT_MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Constituents> DERIVED_MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, List<MixData>> MIX_BY_TO = new HashMap<>();
    private static final Set<ResourceLocation> CALCULATING = new HashSet<>();

    private record MixData(ResourceLocation from, ResourceLocation to, ItemStack ingredient) {}

    /**
     * Registers an explicit potion-type mapping from JSON ('@' prefix in
     * mod_constituents files).
     */
    public static void putDirect(String potionId, Constituents constituents) {
        ResourceLocation id = ResourceLocation.tryParse(potionId);
        if (id != null && constituents != null) {
            DIRECT_MAP.put(id, constituents.copy());
        }
    }

    /**
     * Registers a TRecipe-derived potion mapping (lower priority than direct).
     */
    public static void putDerived(String potionId, Constituents constituents) {
        ResourceLocation id = ResourceLocation.tryParse(potionId);
        if (id != null && constituents != null) {
            DERIVED_MAP.put(id, constituents.copy());
        }
    }

    public static boolean hasConstituents(ResourceLocation id) {
        return DIRECT_MAP.containsKey(id) || DERIVED_MAP.containsKey(id);
    }

    public static void clear() {
        DIRECT_MAP.clear();
        DERIVED_MAP.clear();
        MIX_BY_TO.clear();
        CALCULATING.clear();
    }

    /**
     * Builds the brewing graph and derives every potion; called once per world
     * start after JSON mappings and recipes are ready.
     */
    public static void initialize() {
        DERIVED_MAP.clear();
        MIX_BY_TO.clear();
        CALCULATING.clear();
        buildMixGraph();
        deriveAll();
    }

    public static Constituents getConstituents(Holder<Potion> potion) {
        ResourceLocation id = getPotionId(potion);
        if (id == null) return new Constituents(true);
        return getConstituents(id);
    }

    @Nullable
    private static ResourceLocation getPotionId(Holder<Potion> holder) {
        ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) {
            id = BuiltInRegistries.POTION.getKey(holder.value());
        }
        return id;
    }

    public static Constituents getConstituents(ResourceLocation id) {
        Constituents direct = DIRECT_MAP.get(id);
        if (direct != null) return direct.copy();

        Constituents derived = DERIVED_MAP.get(id);
        if (derived != null) return derived.copy();

        return new Constituents(true);
    }

    private static void buildMixGraph() {
        MIX_BY_TO.clear();

        Level level = Elemenics.getCurrentLevel();
        if (level == null) return;

        PotionBrewing brewing = level.potionBrewing();

        List<?> mixes = getPotionMixes(brewing);
        if (mixes == null) return;

        for (Object mix : mixes) {
            Object from = invokeAccessor(mix, "from");
            Object ingredient = invokeAccessor(mix, "ingredient");
            Object to = invokeAccessor(mix, "to");
            if (from == null || ingredient == null || to == null) continue;

            if (!(from instanceof Holder<?> fromHolder) || !(to instanceof Holder<?> toHolder)) continue;
            if (!(ingredient instanceof Ingredient ing)) continue;

            @SuppressWarnings("unchecked")
            ResourceLocation fromId = getPotionId((Holder<Potion>) fromHolder);
            @SuppressWarnings("unchecked")
            ResourceLocation toId = getPotionId((Holder<Potion>) toHolder);

            ItemStack[] items = ing.getItems();
            if (fromId == null || toId == null || items.length == 0) continue;

            MIX_BY_TO.computeIfAbsent(toId, k -> new ArrayList<>())
                    .add(new MixData(fromId, toId, items[0]));
        }
    }

    public static boolean deriveAll() {
        Set<ResourceLocation> targets = new HashSet<>(MIX_BY_TO.keySet());
        targets.addAll(DIRECT_MAP.keySet());
        targets.add(WATER_POTION_ID);

        int before = DERIVED_MAP.size();
        for (ResourceLocation id : targets) {
            derive(id);
        }
        return DERIVED_MAP.size() != before;
    }

    private static Constituents derive(ResourceLocation id) {
        Constituents direct = DIRECT_MAP.get(id);
        if (direct != null) return direct.copy();

        Constituents cached = DERIVED_MAP.get(id);
        if (cached != null) return cached.copy();

        if (WATER_POTION_ID.equals(id)) {
            Constituents water = getWaterConstituents();
            DERIVED_MAP.put(id, water.copy());
            return water.copy();
        }

        List<MixData> mixes = MIX_BY_TO.get(id);
        if (mixes == null || mixes.isEmpty() || CALCULATING.contains(id)) {
            return new Constituents(true);
        }

        CALCULATING.add(id);
        try {
            Constituents best = null;
            long bestSum = Long.MAX_VALUE;

            for (MixData mix : mixes) {
                Constituents base = derive(mix.from());
                if (base.isUnanalysable()) continue;

                Constituents ingredient = ElemenixInfo.getConstituents(mix.ingredient());
                if (ingredient.isUnanalysable()) continue;

                Constituents total = base.copy();
                Constituents ingredientScaled = ingredient.copy();
                ingredientScaled.multiply(1.0 / 3.0);
                total.add(ingredientScaled);

                long sum = total.getSum();
                if (sum < bestSum) {
                    bestSum = sum;
                    best = total;
                }
            }

            if (best == null) {
                return new Constituents(true);
            }

            DERIVED_MAP.put(id, best.copy());
            return best.copy();
        } finally {
            CALCULATING.remove(id);
        }
    }

    private static Constituents getWaterConstituents() {
        Fluid water = BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water"));
        Constituents mapped = FluidElemenixInfo.getMappedConstituents(water);
        if (mapped != null) return mapped.copy();
        return new Constituents();
    }

    @Nullable
    private static List<?> getPotionMixes(PotionBrewing brewing) {
        try {
            Field field = PotionBrewing.class.getDeclaredField("potionMixes");
            field.setAccessible(true);
            return (List<?>) field.get(brewing);
        } catch (Exception e) {
            LOGGER.warn("Failed to reflect PotionBrewing#potionMixes, potion constituents unavailable", e);
            return null;
        }
    }

    @Nullable
    private static Object invokeAccessor(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Exception e) {
            LOGGER.warn("Failed to invoke accessor '{}' on {}", methodName, target.getClass().getName());
            return null;
        }
    }
}
