package net.lghast.elemenix.core.resolver;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.core.data.JsonElemenixLoader;
import net.lghast.elemenix.core.data.JsonPotionlikeLoader;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.common.util.ModUtils;
import net.lghast.elemenix.core.recipe.RecipeHelper;
import net.lghast.elemenix.core.recipe.TRecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central lookup for the elemenic constituents of item kinds.
 * <p>Owns the loaded mappings, caches, lifecycle and recipe-based inference shared by the rest of the mod.
 * <p>Resolution order: Direct Cache -> Tags -> T-Recipe Pass Cache -> Recipe Inference -> Unanalysable.
 */
public class BaseResolver {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Elemenix value of one essence item; used by the enricher cost and transformer requirements.
     */
    public static final int ESSENCE_VALUE = 486;

    private static Map<String, Constituents> CONFIG_MAPPINGS = new HashMap<>();
    private static final Map<TagKey<Item>, Constituents> TAG_MAPPINGS = new HashMap<>();
    private static final Map<Item, Constituents> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Integer> RECURSION_PREVENTED = new ConcurrentHashMap<>();

    private static final Set<Item> UNANALYSABLE_CACHE = ConcurrentHashMap.newKeySet();
    private static final Set<Item> STRICTLY_UNANALYSABLE_ITEMS = ConcurrentHashMap.newKeySet();
    private static volatile List<String> commandIgnoredResolved = null;

    private static final ThreadLocal<Set<Item>> CALCULATING_ITEMS = ThreadLocal.withInitial(ConcurrentHashMap::newKeySet);
    private static final ThreadLocal<Set<Item>> FAILED_ITEMS = ThreadLocal.withInitial(ConcurrentHashMap::newKeySet);
    private static final ThreadLocal<Integer> QUERY_DEPTH = ThreadLocal.withInitial(() -> 0);

    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initialized = false;
    private static volatile boolean initializing = false;
    private static volatile Thread initializingThread = null;

    /**
     * Loads config/JSON mappings and T-recipes once; no-op while another thread initializes.
     */
    public static void initialize() {
        if (initialized || initializing) return;

        synchronized (INIT_LOCK) {
            if (initialized || initializing) return;

            initializingThread = Thread.currentThread();
            initializing = true;

            try {
                if (CONFIG_MAPPINGS.isEmpty()) {
                    loadFromConfig();
                }

                loadJsonMappings();
                applyMappings(CONFIG_MAPPINGS);

                Level level = Elemenics.getCurrentLevel();
                if (RecipeHelper.areRecipesLoaded(level)) {
                    RecipeHelper.precomputeRecipes(level);
                }

                JsonPotionlikeLoader.loadAll();
                PotionResolver.initialize();
                TRecipeHelper.initialize();

                boolean changed = true;
                int guard = 16;
                while (changed && guard-- > 0) {
                    TRecipeHelper.clear();
                    changed = TRecipeHelper.handleMap();
                    changed |= PotionResolver.deriveAll();
                }

                initialized = true;

                if (RecipeHelper.areRecipesLoaded(level)) {
                    RecipeHelper.warmUpConstituents(level);
                }
                commandIgnoredResolved = computeCommandIgnoredResolved();

                LOGGER.info("BaseResolver Class has been initialized.");
            } finally {
                initializing = false;
                initializingThread = null;
            }
        }
    }

    /**
     * Drops every cache so the next query rebuilds it; called on world load/stop and after a config reload.
     */
    public static void clearCaches() {
        synchronized (INIT_LOCK) {
            initialized = false;
            ITEM_CACHE.clear();
            CALCULATING_ITEMS.remove();
            UNANALYSABLE_CACHE.clear();
            STRICTLY_UNANALYSABLE_ITEMS.clear();
            RECURSION_PREVENTED.clear();
            FAILED_ITEMS.remove();
            QUERY_DEPTH.remove();
            ItemStackResolver.clear();
            RecipeHelper.clearCache();
            TRecipeHelper.clear();
            JsonPotionlikeLoader.clear();
            PotionResolver.clear();
            commandIgnoredResolved = null;
            LOGGER.info("BaseResolver Cache has been cleared.");
        }
    }

    /**
     * True when every cache is warm and queries return final values.
     */
    public static boolean isReady() {
        return initialized && !RecipeHelper.isPrecomputing() && TRecipeHelper.isHandled();
    }

    /**
     * True while the game runs but constituents are still being computed.
     */
    public static boolean isCalculating() {
        return Elemenics.started && !isReady();
    }

    /**
     * True while this item kind is on the current thread's resolution stack; used to detect cycles.
     */
    public static boolean isCalculating(Item item) {
        return CALCULATING_ITEMS.get().contains(item);
    }

    /**
     * Registers mapping entries: ids starting with '#' are item tags, all others are item ids.
     */
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

    /**
     * Constituents of an item kind, using the current level for recipe lookup.
     */
    public static Constituents getConstituents(Item item) {
        return getConstituents(item, Elemenics.getCurrentLevel());
    }

    /**
     * Core resolution for one item.
     * <p>Direct cache first, then tag mappings, then T-recipe pass cache, then recipe inference.
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

        if (CALCULATING_ITEMS.get().contains(item)) {
            countRecursionPrevented(item);
            return new Constituents(true);
        }

        if(isUnanalysableStrictly(item)){
            return new Constituents(true);
        }

        if (FAILED_ITEMS.get().contains(item)) {
            return new Constituents(true);
        }

        if (RecipeHelper.isPrecomputing() && Thread.currentThread() != RecipeHelper.getPrecomputingThread()) {
            return new Constituents(true);
        }

        Integer queryDepth = QUERY_DEPTH.get();
        QUERY_DEPTH.set(queryDepth + 1);
        if (queryDepth == 0) {
            FAILED_ITEMS.get().clear();
        }

        try {
            CALCULATING_ITEMS.get().add(item);
            Constituents recipeConstituents = RecipeHelper.getRecipeConstituents(item, level);
            if (recipeConstituents != null) {
                if (TRecipeHelper.isHandled()) {
                    ITEM_CACHE.put(item, recipeConstituents);
                } else if (TRecipeHelper.isHandling() && !recipeConstituents.isUnanalysable()) {
                    TRecipeHelper.putPassConstituents(item, recipeConstituents);
                }
                return recipeConstituents.copy();
            }
            FAILED_ITEMS.get().add(item);
            if(initialized) {
                LOGGER.debug("Fail to calculate constituents for " + item.getDescription().getString());
            }
        } finally {
            CALCULATING_ITEMS.get().remove(item);
            QUERY_DEPTH.set(queryDepth);
        }

        if(Elemenics.started && TRecipeHelper.isHandled() && !RecipeHelper.isTransientFailure()) {
            UNANALYSABLE_CACHE.add(item);
        }
        return new Constituents(true);
    }

    /**
     * Constituents of a concrete stack, including stack-specific modifiers and stored container contents.
     */
    public static Constituents getConstituents(ItemStack stack) {
        return ItemStackResolver.getResult(stack).constituents();
    }

    /**
     * Same as {@link #getConstituents(ItemStack)}, using the given level for recipe lookup.
     */
    public static Constituents getConstituents(ItemStack stack, @Nullable Level level) {
        return ItemStackResolver.getResult(stack, level).constituents();
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

    /**
     * True when the item kind has no resolvable constituents.
     */
    public static boolean isUnanalysable(Item item) {
        if (UNANALYSABLE_CACHE.contains(item) || STRICTLY_UNANALYSABLE_ITEMS.contains(item)) {
            return true;
        }

        Constituents constituents = getConstituents(item);
        return constituents != null && constituents.isUnanalysable();
    }

    /**
     * True only for kinds declared unanalysable by data or config, not merely unresolved.
     */
    public static boolean isUnanalysableStrictly(Item item){
        return STRICTLY_UNANALYSABLE_ITEMS.contains(item);
    }

    /**
     * Stack overload of {@link #isUnanalysableStrictly(Item)}.
     */
    public static boolean isUnanalysableStrictly(ItemStack stack){
        return isUnanalysableStrictly(stack.getItem());
    }

    /**
     * True when the stack must not be deconstructed:
     * <p>unanalysable kind, tagged undeconstructable, or a container whose stored items cannot be resolved.
     */
    public static boolean isUndeconstructable(ItemStack stack){
        return isUnanalysable(stack.getItem())
                || stack.is(ModTags.UNDECONSTRUCTABLE)
                || ItemStackResolver.isContentRestricted(stack);
    }

    /**
     * True when the stack must not be reconstructed:
     * <p>unanalysable kind or tagged unreconstructable.
     */
    public static boolean isUnreconstructable(ItemStack stack){
        return isUnanalysable(stack.getItem()) || stack.is(ModTags.UNRECONSTRUCTABLE);
    }

    /**
     * Stack constituents scaled by the global deconstruction discount, rounded up.
     */
    public static Constituents getDiscountAppliedConstituents(ItemStack stack){
        Constituents constituents = getConstituents(stack);
        if(constituents.isUnanalysable()) return constituents;

        return Constituents.getDiscountApplied(constituents);
    }

    /**
     * Stack constituents scaled by the global reconstruction premium, rounded down.
     */
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
                        Long.parseLong(parts[1]),
                        Long.parseLong(parts[2]),
                        Long.parseLong(parts[3]),
                        Long.parseLong(parts[4]),
                        Long.parseLong(parts[5]),
                        Long.parseLong(parts[6])
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

    /**
     * True when a direct value for the item kind is already cached.
     */
    public static boolean hasCache(Item item){
        return ITEM_CACHE.containsKey(item);
    }

    /**
     * Stores a computed value for the item kind and clears its unanalysable mark.
     */
    public static void addCache(Item item, Constituents constituents) {
        if (item == null || constituents == null) {
            return;
        }

        ITEM_CACHE.put(item, constituents.copy());
        UNANALYSABLE_CACHE.remove(item);
    }

    /**
     * Returns command_ignored-tagged items that have since become analysable.
     * <p>Computed once at the end of initialization; empty until then.
     */
    public static List<String> getCommandIgnoredResolved() {
        if (commandIgnoredResolved == null) {
            if (!initialized) {
                initialize();
            }
            if (commandIgnoredResolved == null) {
                commandIgnoredResolved = computeCommandIgnoredResolved();
            }
        }
        return commandIgnoredResolved;
    }

    private static List<String> computeCommandIgnoredResolved() {
        List<String> resolved = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (!new ItemStack(item).is(ModTags.COMMAND_IGNORED)) continue;
            if (isUnanalysable(item)) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            resolved.add(id.toString());
        }
        resolved.sort(Comparator.naturalOrder());
        return resolved;
    }

    /**
     * Counts one prevented cycle for the namespace of the item.
     */
    private static void countRecursionPrevented(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String namespace = key.getNamespace();
        RECURSION_PREVENTED.merge(namespace, 1, Integer::sum);
    }

    /**
     * Logs and clears the per-namespace count of prevented cycles; called once after recipe indexing.
     */
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
