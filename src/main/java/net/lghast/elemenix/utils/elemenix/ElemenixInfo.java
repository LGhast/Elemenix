package net.lghast.elemenix.utils.elemenix;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.recipe.RecipeHelper;
import net.lghast.elemenix.utils.recipe.TRecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup for item constituents;
 * Resolution order: direct cache -> tags -> recipe inference -> T-recipes -> unanalysable
 */
public class ElemenixInfo {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int ESSENCE_VALUE = 486;

    private static Map<String, Constituents> CONFIG_MAPPINGS = new HashMap<>();
    protected static final Map<TagKey<Item>, Constituents> TAG_MAPPINGS = new HashMap<>();
    protected static final Map<Item, Constituents> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Integer> RECURSION_PREVENTED = new ConcurrentHashMap<>();

    private static final Set<Item> UNANALYSABLE_CACHE = ConcurrentHashMap.newKeySet();
    private static final Set<Item> CALCULATING_ITEMS = ConcurrentHashMap.newKeySet();
    private static final Set<Item> STRICTLY_UNANALYSABLE_ITEMS = ConcurrentHashMap.newKeySet();

    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initialized = false;
    private static volatile boolean initializing = false;
    private static volatile Thread initializingThread = null;

    /**
     * Loads config/JSON mappings and T-recipes once; no-op while another thread initializes.
     */
    public static void initialize() {
        if (initialized || initializing) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (initialized || initializing) {
                return;
            }
            initializingThread = Thread.currentThread();
            initializing = true;

            try {
                if (CONFIG_MAPPINGS.isEmpty()) {
                    loadFromConfig();
                }

                loadJsonMappings();
                applyMappings(CONFIG_MAPPINGS);

                TRecipeHelper.initialize();
                initialized = true;

                LOGGER.info("ElemenixInfo Class has been initialized.");
            } finally {
                initializing = false;
                initializingThread = null;
            }
        }
    }

    public static void clearCaches() {
        synchronized (INIT_LOCK) {
            initialized = false;

            ITEM_CACHE.clear();
            CALCULATING_ITEMS.clear();
            UNANALYSABLE_CACHE.clear();
            STRICTLY_UNANALYSABLE_ITEMS.clear();
            RECURSION_PREVENTED.clear();

            RecipeHelper.clearCache();
            TRecipeHelper.clear();

            LOGGER.info("ElemenixInfo Cache has been cleared.");
        }
    }

    private static void applyMappings(Map<String, Constituents> mappings) {
        if (mappings.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Constituents> entry : mappings.entrySet()) {
            String id = entry.getKey();
            Constituents constituents = entry.getValue();

            if (id.startsWith("#")) {
                registerTagMapping(id.substring(1), constituents);
            } else {
                registerItemMapping(id, constituents);
            }
        }
    }

    private static void registerTagMapping(String tagId, Constituents constituents) {
        try {
            ResourceLocation tagLocation = ResourceLocation.parse(tagId);
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagLocation);
            TAG_MAPPINGS.put(tagKey, constituents);
        } catch (Exception e) {
            LOGGER.info("Unknown tag ID: #{}", tagId);
        }
    }

    private static void registerItemMapping(String itemId, Constituents constituents) {
        Item item = ModUtils.getItemFromString(itemId);
        if (item == null) {
            LOGGER.info("Unknown item ID: {}", itemId);
            return;
        }

        if (constituents.isUnanalysable()) {
            STRICTLY_UNANALYSABLE_ITEMS.add(item);
        } else {
            ITEM_CACHE.put(item, constituents.copy());
        }
    }

    public static Constituents getConstituents(Item item) {
        return getConstituents(item, Elemenics.getCurrentLevel());
    }

    /**
     * Core resolution for one item.
     * Direct cache first, then tag mappings, then T-recipe pass cache, then recipe inference.
     *
     * @param item  the item to resolve
     * @param level used for recipe lookup
     * @return constituents, or an unanalysable constituents when unresolvable
     */
    public static Constituents getConstituents(Item item, @Nullable Level level) {
        if (!Elemenics.started) {
            return new Constituents(true);
        }

        if (!initialized) {
            if (initializing && Thread.currentThread() != initializingThread) {
                return new Constituents(true);
            }
            Level checkLevel = level != null ? level : Elemenics.getCurrentLevel();
            if (!RecipeHelper.areRecipesLoaded(checkLevel)) {
                return new Constituents(true);
            }
            initialize();
        }

        if(UNANALYSABLE_CACHE.contains(item) || STRICTLY_UNANALYSABLE_ITEMS.contains(item)){
            return new Constituents(true);
        }

        Constituents directConstituents = ITEM_CACHE.get(item);
        if (directConstituents != null) return directConstituents.copy();

        if (TRecipeHelper.isHandling()) {
            Constituents passCached = TRecipeHelper.getPassConstituents(item);
            if (passCached != null) {
                return passCached.copy();
            }
        }

        List<Map.Entry<TagKey<Item>, Constituents>> matchingTags = new ArrayList<>();
        for (Map.Entry<TagKey<Item>, Constituents> entry : TAG_MAPPINGS.entrySet()) {
            if (item.getDefaultInstance().is(entry.getKey())) {
                matchingTags.add(entry);
            }
        }

        if (!matchingTags.isEmpty()) {
            Constituents tagConstituents = getBestTagConstituents(matchingTags);
            ITEM_CACHE.put(item, tagConstituents);
            return tagConstituents.copy();
        }

        if (CALCULATING_ITEMS.contains(item)) {
            countRecursionPrevented(item);
            return new Constituents(true);
        }

        if(isUnanalysableStrictly(item)){
            return new Constituents(true);
        }

        if (RecipeHelper.isPrecomputing() && Thread.currentThread() != RecipeHelper.getPrecomputingThread()) {
            return new Constituents(true);
        }

        try {
            CALCULATING_ITEMS.add(item);
            Constituents recipeConstituents = RecipeHelper.getRecipeConstituents(item, level);
            if (recipeConstituents != null) {
                if (TRecipeHelper.isHandled()) {
                    ITEM_CACHE.put(item, recipeConstituents);
                } else if (TRecipeHelper.isHandling() && !recipeConstituents.isUnanalysable()) {
                    TRecipeHelper.putPassConstituents(item, recipeConstituents);
                }
                return recipeConstituents.copy();
            }
            if(initialized) {
                LOGGER.info("Fail to calculate constituents for " + item.getDescription().getString());
            }
        } finally {
            CALCULATING_ITEMS.remove(item);
        }

        if(Elemenics.started && TRecipeHelper.isHandled()) {
            UNANALYSABLE_CACHE.add(item);
        }
        return new Constituents(true);
    }

    public static Constituents getConstituents(ItemStack stack) {
        return getConstituents(stack, Elemenics.getCurrentLevel());
    }

    public static Constituents getConstituents(ItemStack stack, @Nullable Level level) {
        if (!stack.isDamaged()) {
            return getConstituents(stack.getItem(), level);
        }
        return getConstituentsForDamagedStack(stack, level);
    }

    /**
     * Damaged stacks are scaled by their remaining-durability ratio (remaining / max).
     */
    private static Constituents getConstituentsForDamagedStack(ItemStack stack, @Nullable Level level) {
        Constituents constituents = getConstituents(stack.getItem(), level).copy();
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        double damageMultiple = (maxDamage - damage) / (double) maxDamage;

        constituents.multiply(damageMultiple);
        return constituents;
    }

    /**
     * When several tags match, the one with the largest total constituents wins.
     */
    private static Constituents getBestTagConstituents(List<Map.Entry<TagKey<Item>, Constituents>> matchingTags) {
        Map.Entry<TagKey<Item>, Constituents> bestMatch = matchingTags.getFirst();

        if (matchingTags.size() > 1) {
            for (int i = 1; i < matchingTags.size(); i++) {
                Map.Entry<TagKey<Item>, Constituents> current = matchingTags.get(i);
                if (current.getValue().getSum() > bestMatch.getValue().getSum()) {
                    bestMatch = current;
                }
            }
        }

        return bestMatch.getValue();
    }

    public static boolean isUnanalysable(Item item) {
        if (UNANALYSABLE_CACHE.contains(item) || STRICTLY_UNANALYSABLE_ITEMS.contains(item)) {
            return true;
        }

        Constituents constituents = getConstituents(item);
        return constituents != null && constituents.isUnanalysable();
    }

    public static boolean isUnanalysable(ItemStack stack){
        return isUnanalysable(stack.getItem());
    }

    public static boolean isUnanalysableStrictly(Item item){
        return STRICTLY_UNANALYSABLE_ITEMS.contains(item);
    }

    public static boolean isUnanalysableStrictly(ItemStack stack){
        return isUnanalysableStrictly(stack.getItem());
    }

    public static boolean isUndeconstructable(ItemStack stack){
        return isUnanalysable(stack.getItem()) || stack.is(ModTags.UNDECONSTRUCTABLE);
    }

    public static boolean isUnreconstructable(ItemStack stack){
        return isUnanalysable(stack.getItem()) || stack.is(ModTags.UNRECONSTRUCTABLE);
    }

    public static Constituents getDiscountAppliedConstituents(ItemStack stack){
        Constituents constituents = getConstituents(stack);
        if(constituents.isUnanalysable()) return constituents;

        return Constituents.getDiscountApplied(constituents);
    }

    public static Constituents getPremiumAppliedConstituents(ItemStack stack){
        Constituents constituents = getConstituents(stack);
        if(constituents.isUnanalysable()) return constituents;

        return Constituents.getPremiumApplied(constituents);
    }

    /**
     * Loads compat mappings from data/elemenix/mod_constituents JSON files.
     */
    private static void loadJsonMappings() {
        Map<String, Constituents> jsonMappings = JsonElemenixLoader.loadAllMappings();
        applyMappings(jsonMappings);
    }

    /**
     * Parses the config mapping/unanalysable lists and rebuilds CONFIG_MAPPINGS;
     * invalid entries are logged and skipped.
     */
    public static void loadFromConfig() {
        Map<String, Constituents> newElemenixMap = new HashMap<>();
        List<? extends String> configElemenixList = CommonConfig.ELEMENIX_MAPPINGS.get();
        List<? extends String> configUnanalysableList = CommonConfig.UNANALYSABLE_LIST.get();

        for (String entry : configElemenixList) {
            if (entry == null) {
                continue;
            }

            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            try {
                String cleanEntry = trimmedEntry.replaceAll("\\s+", "");

                if (!cleanEntry.matches("#?[a-z0-9_\\-.:/]+,([0-9]+,){5}[0-9]+")) {
                    LOGGER.warn("Invalid mapping format: {}", trimmedEntry);
                    continue;
                }

                String[] parts = cleanEntry.split(",");
                if (parts.length != 7) {
                    LOGGER.warn("Incorrect number of mapping parameters, expected 7, got {}: {}", parts.length, trimmedEntry);
                    continue;
                }

                Constituents constituents = new Constituents(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6])
                );

                newElemenixMap.put(parts[0], constituents);
                LOGGER.debug("Successfully loaded mapping: {}", trimmedEntry);

            } catch (NumberFormatException e) {
                LOGGER.warn("Mapping number format error: {}", trimmedEntry, e);
            } catch (Exception e) {
                LOGGER.warn("Mapping parsing failed: {}", trimmedEntry, e);
            }
        }

        for (String entry : configUnanalysableList) {
            if (entry == null) {
                continue;
            }

            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            try {
                String cleanEntry = trimmedEntry.replaceAll("\\s+", "");

                if (!cleanEntry.matches("#?[a-z0-9_\\-.:/]+")) {
                    LOGGER.warn("Invalid ID format: {}", trimmedEntry);
                    continue;
                }

                Constituents constituents = new Constituents(true);

                newElemenixMap.put(cleanEntry, constituents);
                LOGGER.debug("Successfully loaded unanalysable item: {}", trimmedEntry);

            }catch (Exception e) {
                LOGGER.warn("Unanalysable item parsing failed: {}", trimmedEntry, e);
            }
        }

        CONFIG_MAPPINGS = newElemenixMap;
        clearCaches();
    }

    public static boolean hasCache(Item item){
        return ITEM_CACHE.containsKey(item);
    }

    public static void addCache(Item item, Constituents constituents) {
        if (item == null || constituents == null) {
            return;
        }

        ITEM_CACHE.put(item, constituents.copy());
        UNANALYSABLE_CACHE.remove(item);
    }

    private static void countRecursionPrevented(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String namespace = key.getNamespace();
        RECURSION_PREVENTED.merge(namespace, 1, Integer::sum);
    }

    public static void logRecursionSummary() {
        if (RECURSION_PREVENTED.isEmpty()) {
            return;
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(RECURSION_PREVENTED.entrySet());
        sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        int total = 0;
        StringBuilder detail = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            total += entry.getValue();
            if (i > 0) {
                detail.append(", ");
            }
            detail.append(entry.getKey()).append(" x").append(entry.getValue());
        }

        LOGGER.info("Recursion prevented {} times in total during calculation: {}", total, detail.toString());
        RECURSION_PREVENTED.clear();
    }
}
