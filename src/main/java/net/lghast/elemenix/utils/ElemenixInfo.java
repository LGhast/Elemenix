package net.lghast.elemenix.utils;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.conifig.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ElemenixInfo {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int ESSENCE_VALUE = 486;

    private static Map<String, Constituents> ADDITIONAL_MAP;
    private static final Map<String, Constituents> ELEMENIX_MAP = ElemenixMapping.ELEMENIX_MAP;

    protected static final Map<Item, Constituents> ITEM_CACHE = new HashMap<>();
    protected static final Map<TagKey<Item>, Constituents> TAG_MAP = new HashMap<>();
    private static final Set<Item> CALCULATING_ITEMS = new HashSet<>();
    private static final Set<Item> UNANALYSABLE_ITEMS = new HashSet<>();
    private static boolean isInitialized = false;

    private static void initialize() {
        if (isInitialized) return;

        if(ADDITIONAL_MAP.isEmpty()){
            reloadFromConfig();
        }
        handleMap(ADDITIONAL_MAP);
        loadModMappings();
        handleMap(ELEMENIX_MAP);

        isInitialized = true;
    }

    public static void clearCaches() {
        isInitialized = false;
        ITEM_CACHE.clear();
        CALCULATING_ITEMS.clear();
        UNANALYSABLE_ITEMS.clear();
        RecipeHelper.clearCache();
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
                    ITEM_CACHE.put(item, value);
                } else {
                    LOGGER.info("Unknown item: {}", key);
                }
            }
        }
    }

    public static Constituents getConstituents(ItemStack stack) {
        if(stack.isDamaged()){
            Constituents constituents = getConstituents(stack.getItem()).copy();
            int damage = stack.getDamageValue();
            int maxDamage = stack.getMaxDamage();
            double damageMultiple = (maxDamage - damage) / (double)maxDamage;
            constituents.multiply(damageMultiple);
            return constituents;
        }
        return getConstituents(stack.getItem());
    }

    public static Constituents getConstituents(Item item) {
        if (!Elemenics.started) {
            return new Constituents(true);
        }

        if (!isInitialized) {
            initialize();
        }

        if(UNANALYSABLE_ITEMS.contains(item)){
            return new Constituents(true);
        }

        Constituents directConstituents = ITEM_CACHE.get(item);
        if (directConstituents != null) return directConstituents;

        List<Map.Entry<TagKey<Item>, Constituents>> matchingTags = new ArrayList<>();
        for (Map.Entry<TagKey<Item>, Constituents> entry : TAG_MAP.entrySet()) {
            if (item.getDefaultInstance().is(entry.getKey())) {
                matchingTags.add(entry);
            }
        }
        
        if (!matchingTags.isEmpty()) {
            Constituents tagConstituents = getConstituentsFromBestTag(matchingTags);
            ITEM_CACHE.put(item, tagConstituents);
            return tagConstituents;
        }

        if (CALCULATING_ITEMS.contains(item)) {
            LOGGER.debug("Preventing recursion for item: {}", BuiltInRegistries.ITEM.getKey(item));
            return new Constituents(true);
        }

        try {
            CALCULATING_ITEMS.add(item);
            Constituents recipeConstituents = RecipeHelper.getRecipeConstituents(item);
            if (recipeConstituents != null) {
                ITEM_CACHE.put(item, recipeConstituents);
                return recipeConstituents;
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
        if(UNANALYSABLE_ITEMS.contains(item)){
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
        for (String modId : ModElemenixMapping.getAvailableModMappings()) {
            if (ModList.get().isLoaded(modId)) {
                try {
                    Map<String, Constituents> modMap = ModElemenixMapping.getModMappings(modId);
                    handleMap(modMap);
                } catch (Exception e) {
                    LOGGER.info("Failed to load compatibility mappings for mod: {}", modId, e);
                }
            }
        }
    }

    public static void reloadFromConfig() {
        Map<String, Constituents> newMap = new HashMap<>();
        List<? extends String> configList = ServerConfig.ELEMENIX_MAPPINGS.get();

        for (String list : configList) {
            try {
                String trimList = list.replace(" ", "");
                String[] parts = trimList.split(",");
                if (parts.length != 7) {
                    LOGGER.warn("Incomplete Mapping: {}", list);
                    continue;
                }

                newMap.put(parts[0], new Constituents(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6])
                ));
            } catch (Exception e) {
                LOGGER.warn("Invalid Mapping: {}", list);
            }
        }
        ADDITIONAL_MAP = newMap;
    }
}
