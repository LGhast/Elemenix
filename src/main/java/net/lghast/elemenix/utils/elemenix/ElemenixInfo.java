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

public class ElemenixInfo {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int ESSENCE_VALUE = 486;

    private static Map<String, Constituents> ADDITIONAL_MAP = new HashMap<>();
    protected static final Map<TagKey<Item>, Constituents> TAG_MAP = new HashMap<>();

    protected static final Map<Item, Constituents> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Set<Item> CALCULATING_ITEMS = ConcurrentHashMap.newKeySet();
    private static final Set<Item> UNANALYSABLE_ITEMS = ConcurrentHashMap.newKeySet();
    private static final Set<Item> UNANALYSABLE_ITEMS_STRICT = ConcurrentHashMap.newKeySet();
    private static boolean initialized = false;
    private static volatile boolean initializing = false;

    public static void initialize() {
        if (initialized || initializing) return;
        initializing = true;
        try {
            if(ADDITIONAL_MAP.isEmpty()){
                loadFromConfig();
            }
            loadModMappings();
            handleMap(ADDITIONAL_MAP);
            TRecipeHelper.initialize();
            initialized = true;
            LOGGER.info("ElemenixInfo Class has been initialized.");
        } finally {
            initializing = false;
        }
    }

    public static void clearCaches() {
        initialized = false;
        ITEM_CACHE.clear();
        CALCULATING_ITEMS.clear();
        UNANALYSABLE_ITEMS.clear();
        UNANALYSABLE_ITEMS_STRICT.clear();
        RecipeHelper.clearCache();
        TRecipeHelper.clear();

        LOGGER.info("ElemenixInfo Cache has been cleared.");
    }

    private static void handleMap(Map<String, Constituents> map){
        if(map.isEmpty()) return;
        for (Map.Entry<String, Constituents> entry : map.entrySet()) {
            String key = entry.getKey();
            Constituents value = entry.getValue();

            if (key.startsWith("#")) {
                String tagId = key.substring(1);
                try {
                    ResourceLocation tagLocation = ResourceLocation.parse(tagId);
                    TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagLocation);
                    TAG_MAP.put(tagKey, value);
                } catch (Exception e) {
                    LOGGER.info("Unknown tag ID: {}", key);
                }
            } else {
                Item item = ModUtils.getItemFromString(key);
                if (item != null) {
                    if(value.isUnanalysable()){
                        UNANALYSABLE_ITEMS_STRICT.add(item);
                    }else {
                        ITEM_CACHE.put(item, value);
                    }
                } else {
                    LOGGER.info("Unknown item ID: {}", key);
                }
            }
        }
    }

    public static Constituents getConstituents(ItemStack stack) {
        return getConstituents(stack, Elemenics.getCurrentLevel());
    }

    public static Constituents getConstituents(ItemStack stack, @Nullable Level level) {
        if(stack.isDamaged()){
            Constituents constituents = getConstituents(stack.getItem(), level).copy();
            int damage = stack.getDamageValue();
            int maxDamage = stack.getMaxDamage();
            double damageMultiple = (maxDamage - damage) / (double)maxDamage;
            constituents.multiply(damageMultiple);
            return constituents;
        }
        return getConstituents(stack.getItem(), level);
    }

    public static Constituents getConstituents(Item item) {
        return getConstituents(item, Elemenics.getCurrentLevel());
    }

    public static Constituents getConstituents(Item item, @Nullable Level level) {
        if (!Elemenics.started) {
            return new Constituents(true);
        }

        if (!initialized) {
            initialize();
        }

        if(UNANALYSABLE_ITEMS.contains(item) || UNANALYSABLE_ITEMS_STRICT.contains(item)){
            return new Constituents(true);
        }

        Constituents directConstituents = ITEM_CACHE.get(item);
        if (directConstituents != null) return directConstituents.copy();

        List<Map.Entry<TagKey<Item>, Constituents>> matchingTags = new ArrayList<>();
        for (Map.Entry<TagKey<Item>, Constituents> entry : TAG_MAP.entrySet()) {
            if (item.getDefaultInstance().is(entry.getKey())) {
                matchingTags.add(entry);
            }
        }

        if (!matchingTags.isEmpty()) {
            Constituents tagConstituents = getConstituentsFromBestTag(matchingTags);
            ITEM_CACHE.put(item, tagConstituents);
            return tagConstituents.copy();
        }

        if (CALCULATING_ITEMS.contains(item)) {
            if(initialized) {
                LOGGER.debug("Preventing recursion for item: {}", BuiltInRegistries.ITEM.getKey(item));
            }
            return new Constituents(true);
        }

        if(isUnanalysableStrictly(item)){
            return new Constituents(true);
        }

        try {
            CALCULATING_ITEMS.add(item);
            Constituents recipeConstituents = RecipeHelper.getRecipeConstituents(item, level);
            if (recipeConstituents != null) {
                ITEM_CACHE.put(item, recipeConstituents);
                return recipeConstituents.copy();
            }
            if(initialized) {
                LOGGER.info("Fail to calculate constituents for " + item.getDescription().getString());
            }
        } finally {
            CALCULATING_ITEMS.remove(item);
        }

        if(Elemenics.started) {
            UNANALYSABLE_ITEMS.add(item);
        }
        return new Constituents(true);
    }

    private static Constituents getConstituentsFromBestTag(List<Map.Entry<TagKey<Item>, Constituents>> matchingTags) {
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

    public static boolean isUnanalysable(Item item){
        if(UNANALYSABLE_ITEMS.contains(item) || UNANALYSABLE_ITEMS_STRICT.contains(item)){
            return true;
        }else{
            Constituents constituents = getConstituents(item);
            if(constituents != null) {
                return getConstituents(item).isUnanalysable();
            }
            return false;
        }
    }

    public static boolean isUnanalysable(ItemStack stack){
        return isUnanalysable(stack.getItem());
    }
    public static boolean isUnanalysableStrictly(Item item){
        return UNANALYSABLE_ITEMS_STRICT.contains(item);
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

    private static void loadModMappings() {
        Map<String, Constituents> jsonMappings = JsonElemenixLoader.loadAllMappings();
        handleMap(jsonMappings);
    }

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

        ADDITIONAL_MAP = newElemenixMap;
        clearCaches();
    }

    public static boolean hasCache(Item item){
        return ITEM_CACHE.containsKey(item);
    }

    public static void addCache(Item item, Constituents constituents){
        ITEM_CACHE.put(item, constituents);
        UNANALYSABLE_ITEMS.remove(item);
    }
}
