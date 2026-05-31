package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RecipeHelper {
    private static final Map<ResourceLocation, Item> RECIPE_OUTPUT_CACHE = new HashMap<>();
    private static final List<ResourceLocation> RECIPE_IGNORED_CACHE = new ArrayList<>();
    private static final Map<Item, List<RecipeInfo>> RECIPE_MAP = new ConcurrentHashMap<>();

    private static final List<String> RECIPE_TYPES_NORMAL = new ArrayList<>();
    private static final List<String> RECIPE_TYPES_WITH_CONTAINER = new ArrayList<>();
    private static final List<String> RECIPE_TYPES_BREWING_LIKE = new ArrayList<>();
    private static final List<String> RECIPE_TYPES_GLODIUM = new ArrayList<>();
    private static final List<String> RECIPE_TYPES_GLODIUMS = new ArrayList<>();

    private static final boolean FARMERS_DELIGHT_LOADED;
    private static final boolean ANVILCRAFT_LOADED;
    private static final boolean CATACLYSM_LOADED;
    private static final boolean AE2_LOADED;
    private static final boolean AETHER_LOADED;
    private static final boolean CONFLUENCE_LOADED;

    private static boolean initialized = false;

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        FARMERS_DELIGHT_LOADED = ModUtils.hasServerMod("farmersdelight");
        ANVILCRAFT_LOADED = ModUtils.hasServerMod("anvilcraft");
        CATACLYSM_LOADED = ModUtils.hasServerMod("cataclysm");
        AE2_LOADED = ModUtils.hasServerMod("ae2");
        AETHER_LOADED = ModUtils.hasServerMod("aether");
        CONFLUENCE_LOADED = ModUtils.hasServerMod("confluence");

        if(FARMERS_DELIGHT_LOADED){
            RECIPE_TYPES_WITH_CONTAINER.add("farmersdelight:cooking");

            if(ModUtils.hasServerMod("dungeonsdelight")){
                RECIPE_TYPES_WITH_CONTAINER.add("dungeonsdelight:monster_cooking");
            }

            if(ModUtils.hasServerMod("minersdelight")){
                RECIPE_TYPES_WITH_CONTAINER.add("minersdelight:cooking");
            }

            if(ModUtils.hasServerMod("youkaishomecoming")){
                RECIPE_TYPES_WITH_CONTAINER.add("youkaishomecoming:moka_pot");
                RECIPE_TYPES_WITH_CONTAINER.add("youkaishomecoming:kettle");
                RECIPE_TYPES_NORMAL.add("youkaishomecoming:steaming");
            }
        }
        if(ANVILCRAFT_LOADED){
            RECIPE_TYPES_NORMAL.add("anvilcraft:jewel_crafting");
        }
        if(AE2_LOADED){
            RECIPE_TYPES_NORMAL.add("ae2:inscriber");
            RECIPE_TYPES_NORMAL.add("ae2:charger");

            if(ModUtils.hasServerMod("extendedae")){
                RECIPE_TYPES_GLODIUM.add("extendedae:circuit_cutter");
                RECIPE_TYPES_GLODIUMS.add("extendedae:crystal_assembler");

            }
            if(ModUtils.hasServerMod("advanced_ae")){
                RECIPE_TYPES_GLODIUMS.add("advanced_ae:reaction");
            }
        }
        if(AETHER_LOADED){
            RECIPE_TYPES_NORMAL.add("aether:enchanting");
            RECIPE_TYPES_NORMAL.add("aether:freezing");

            if(ModUtils.hasServerMod("deep_aether")){
                RECIPE_TYPES_NORMAL.add("deep_aether:combining");
            }
        }
        if(CONFLUENCE_LOADED){
            RECIPE_TYPES_NORMAL.add("confluence:heavy_work_bench");
            RECIPE_TYPES_NORMAL.add("confluence:fletching_table");
            RECIPE_TYPES_NORMAL.add("confluence:altar");
            RECIPE_TYPES_NORMAL.add("confluence:hellforge");
            RECIPE_TYPES_NORMAL.add("confluence:loom");
            RECIPE_TYPES_NORMAL.add("confluence:sky_mill");
            RECIPE_TYPES_NORMAL.add("confluence:solidifier");
            RECIPE_TYPES_NORMAL.add("confluence:crystal_ball");
            RECIPE_TYPES_NORMAL.add("confluence:hardmode_anvil");
            RECIPE_TYPES_NORMAL.add("confluence:hardmode_forge");
            RECIPE_TYPES_NORMAL.add("terra_furniture:glass_kiln");
            RECIPE_TYPES_WITH_CONTAINER.add("confluence:alchemy_table");
            RECIPE_TYPES_WITH_CONTAINER.add("confluence:cooking_pot");
        }

        if(ModUtils.hasServerMod("twilightforest")){
            RECIPE_TYPES_NORMAL.add("twilightforest:drying");
        }
        if(ModUtils.hasServerMod("create")){
            RECIPE_TYPES_NORMAL.add("create:pressing");
            RECIPE_TYPES_NORMAL.add("create:mechanical_crafting");
            RECIPE_TYPES_NORMAL.add("create:sandpaper_polishing");
            RECIPE_TYPES_NORMAL.add("create:mixing");
            RECIPE_TYPES_NORMAL.add("create:compacting");
        }
        if(ModUtils.hasServerMod("ars_nouveau")){
            RECIPE_TYPES_NORMAL.add("ars_nouveau:enchanting_apparatus");
            RECIPE_TYPES_NORMAL.add("ars_nouveau:glyph");
        }
        if(ModUtils.hasServerMod("cobblemon")){
            RECIPE_TYPES_NORMAL.add("cobblemon:cooking_pot");
            RECIPE_TYPES_BREWING_LIKE.add("cobblemon:brewing_stand");
        }
        if(ModUtils.hasServerMod("terra_curio")){
            RECIPE_TYPES_NORMAL.add("terra_curio:workshop");
        }
    }

    public static void clearCache() {
        RECIPE_OUTPUT_CACHE.clear();
        RECIPE_IGNORED_CACHE.clear();
        RECIPE_MAP.clear();
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        initialized = false;
    }

    public static void precomputeRecipes(@Nullable Level level) {
        if (level == null) {
            LOGGER.warn("Cannot precompute recipes: level is null");
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        RECIPE_MAP.clear();
        RECIPE_OUTPUT_CACHE.clear();
        RECIPE_IGNORED_CACHE.clear();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            ResourceLocation recipeId = holder.id();
            Recipe<?> recipe = holder.value();
            ItemStack result = getRecipeResultItem(recipe, level.registryAccess());

            if (result == null || result.isEmpty()) {
                continue;
            }
            Item resultItem = result.getItem();

            if (isValidRecipeType(recipe)) {
                RecipeInfo info = getInfo(holder, recipe, level, result);
                RECIPE_MAP.computeIfAbsent(resultItem, k -> new ArrayList<>()).add(info);
                RECIPE_OUTPUT_CACHE.put(recipeId, resultItem);
                continue;
            }

            RECIPE_IGNORED_CACHE.add(recipeId);
        }

        initialized = true;
        LOGGER.info("RecipeHelper precomputed {} recipes", RECIPE_MAP.size());
    }

    private static ItemStack replacedStack(RecipeType<?> type, ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            return new ItemStack(ModItems.NULLVOID.asItem(), stack.getCount());
        }
        if (item == Items.TIPPED_ARROW) {
            return new ItemStack(Items.ARROW, stack.getCount());
        }

        if (item == Items.ENCHANTED_BOOK) {
            return new ItemStack(Items.BOOK, stack.getCount());
        }

        if(!AE2_LOADED){
            return stack;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        if(type.toString().equals("ae2:inscriber") && (itemId.equals("ae2:calculation_processor_press")
                || itemId.equals("ae2:engineering_processor_press") || itemId.equals("ae2:logic_processor_press")
                || itemId.equals("ae2:silicon_press"))){
            return new ItemStack(ModItems.NULLVOID.asItem(), stack.getCount());
        }

        return stack;
    }

    private static List<Ingredient> getRecipeIngredients(Recipe<?> recipe) {
        try {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            //noinspection ConstantConditions
            if (ingredients != null && !ingredients.isEmpty()) {
                return ingredients;
            }
        } catch (Exception ignored) {}

        Optional<List<?>> fromMethod = tryInvokeMethods(recipe,
                obj -> obj instanceof List<?> list && !list.isEmpty() ? Optional.of(list) : Optional.empty(),
                "getInputs");
        if (fromMethod.isPresent()) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (Object obj : fromMethod.get()) {
                if (obj instanceof Ingredient ing) {
                    ingredients.add(ing);
                }
            }
            if (!ingredients.isEmpty()) {
                return ingredients;
            }
        }

        Optional<List<?>> fromField = tryGetFields(recipe,
                obj -> obj instanceof List<?> list && !list.isEmpty() ? Optional.of(list) : Optional.empty(),
                "ingredients");
        if (fromField.isPresent()) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (Object obj : fromField.get()) {
                if (obj instanceof Ingredient ing) {
                    ingredients.add(ing);
                }
            }
            if (!ingredients.isEmpty()) {
                return ingredients;
            }
        }

        return Collections.emptyList();
    }

    private static ItemStack getRecipeResultItem(Recipe<?> recipe, @Nullable HolderLookup.Provider registryAccess) {
        if (registryAccess == null) return ItemStack.EMPTY;

        try {
            ItemStack result = recipe.getResultItem(registryAccess);
            //noinspection ConstantConditions
            if (result != null && !result.isEmpty()) return result;
        } catch (Exception ignored) {}

        Optional<ItemStack> fromMethod = tryInvokeMethods(recipe,
                RecipeHelper::tryExtractItemStack,
                "getResultItem", "getOutput", "getResult");
        if (fromMethod.isPresent()) return fromMethod.get();

        Optional<ItemStack> fromField = tryGetFields(recipe,
                RecipeHelper::tryExtractItemStack,
                "output", "result");
        return fromField.orElse(ItemStack.EMPTY);
    }

    private static ItemStack getRecipeContainer(Recipe<?> recipe) {
        Optional<ItemStack> fromMethod = tryInvokeMethods(recipe,
                RecipeHelper::tryExtractItemStack,
                "getOutputContainer", "getContainerOverride", "getBase");
        if (fromMethod.isPresent()) return fromMethod.get();

        Optional<ItemStack> fromField = tryGetFields(recipe,
                RecipeHelper::tryExtractItemStack,
                "container", "outputContainer", "containerItem", "base");
        return fromField.orElse(ItemStack.EMPTY);
    }

    private static List<RecipeInfo> getRecipesForItem(Item targetItem, @Nullable Level level) {
        if (initialized && RECIPE_MAP.containsKey(targetItem)) {
            return new ArrayList<>(RECIPE_MAP.get(targetItem));
        }

        if(level == null){
            return Collections.emptyList();
        }

        List<RecipeInfo> recipes = new ArrayList<>();
        RecipeManager recipeManager = level.getRecipeManager();

        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            ResourceLocation recipeId = recipeHolder.id();

            Item cachedOutput = RECIPE_OUTPUT_CACHE.get(recipeId);
            if (cachedOutput != null && cachedOutput != targetItem) {
                continue;
            }

            Recipe<?> recipe = recipeHolder.value();
            ItemStack result = getRecipeResultItem(recipe, level.registryAccess());

            //noinspection ConstantConditions
            if(result == null) continue;
            if(result.isEmpty()) continue;

            Item resultItem = result.getItem();
            RECIPE_OUTPUT_CACHE.put(recipeId, resultItem);

            if (resultItem != targetItem) {
                continue;
            }

            if(RECIPE_IGNORED_CACHE.contains(recipeId)){
                continue;
            }

            if (!isValidRecipeType(recipe)) {
                RECIPE_IGNORED_CACHE.add(recipeId);
                continue;
            }

            recipes.add(getInfo(recipeHolder, recipe, level, result));
        }

        return recipes;
    }

    private static RecipeInfo getBestRecipeForItem(Item targetItem, @Nullable Level level) {
        if(ElemenixInfo.isUnanalysableStrictly(targetItem)){
            return null;
        }

        List<RecipeInfo> recipes = getRecipesForItem(targetItem, level);

        if (recipes.isEmpty()) {
            LOGGER.info("Fail to calculate: recipes of " + targetItem.getDescription().getString() + " not found");
            return null;
        }

        RecipeInfo bestRecipe = findBestRecipeInfo(recipes);

        if(bestRecipe.sum == 0 || bestRecipe.sum == Long.MAX_VALUE){
            return null;
        }
        return bestRecipe;
    }

    private static RecipeInfo findBestRecipeInfo(List<RecipeInfo> recipes) {
        RecipeInfo bestRecipe = recipes.getFirst();

        for (int i = 1; i < recipes.size(); i++) {
            RecipeInfo current = recipes.get(i);
            if(current.hasUnanalysable) continue;
            if(current.isComplex) continue;
            if (current.getSum() < bestRecipe.getSum()) {
                bestRecipe = current;
            } else if (current.getSum() == bestRecipe.getSum()) {
                if (current.amount < bestRecipe.amount) {
                    bestRecipe = current;
                }
            }
        }
        return bestRecipe;
    }

    public static Constituents getRecipeConstituents(Item item, @Nullable Level level) {
        RecipeInfo recipeInfo = getBestRecipeForItem(item, level);
        if(recipeInfo == null) return null;
        return recipeInfo.getConstituents();
    }

    private static boolean isValidRecipeType(Recipe<?> recipe) {
        RecipeType<?> type = recipe.getType();

        return type == RecipeType.CRAFTING ||
                type == RecipeType.SMELTING ||
                type == RecipeType.SMOKING ||
                type == RecipeType.STONECUTTING ||
                recipe instanceof SmithingTransformRecipe ||
                isNormalModRecipe(recipe) ||
                isRecipeWithContainer(recipe) ||
                isCuttingBoardRecipe(recipe) ||
                isMultipleToOneSmithingRecipe(recipe) ||
                isBrewingStandRecipe(recipe) ||
                isWeaponFusionRecipe(recipe) ||
                isGlodiumRecipe(recipe) ||
                isGlodiumsRecipe(recipe);
    }

    private static RecipeInfo getInfo(RecipeHolder<?> holder, Recipe<?> recipe, @Nullable Level level, ItemStack result) {
        RecipeInfo info = new RecipeInfo();
        if(level == null) return info;

        info.recipeId = holder.id();
        info.type = recipe.getType();
        info.ingredients = new ArrayList<>();
        info.result = result;
        info.resultAmount = info.result.getCount();
        info.amount = 0;

        if (isRecipeWithContainer(recipe)) {
            handleRecipeWithContainer(recipe, info);
        } else if (isCuttingBoardRecipe(recipe)) {
            handleCuttingBoardRecipe(recipe, info);
        } else if (recipe.getType() == RecipeType.SMITHING) {
            handleSmithingRecipe(recipe, info);
        } else if (isMultipleToOneSmithingRecipe(recipe)) {
            handleMultipleToOneSmithingRecipe(recipe, info);
        } else if (isBrewingStandRecipe(recipe)){
            handleBrewingStandRecipe(recipe, info);
        } else if (isWeaponFusionRecipe(recipe)) {
            handleWeaponFusionRecipe(recipe, info);
        } else if(isGlodiumRecipe(recipe)){
            handleGlodiumRecipe(recipe, info);
        } else if(isGlodiumsRecipe(recipe)){
            handleGlodiumsRecipe(recipe, info);
        }else {
            List<Ingredient> ingredients = getRecipeIngredients(recipe);
            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(recipe.getType(), matchingItems);
                        if(bestItemStack == null || bestItemStack.isEmpty()){
                            info.hasUnanalysable = true;
                        }else {
                            info.ingredients.add(bestItemStack);
                            info.amount += bestItemStack.getCount();
                        }
                    }
                }
            }
        }

        info.sum = info.getConstituents().getSum();
        return info;
    }

    private static ItemStack findBestItemStack(RecipeType<?> type, ItemStack[] matchingItems) {
        if (matchingItems.length == 1) {
            return replacedStack(type, matchingItems[0]);
        }

        ItemStack bestItemStack = ItemStack.EMPTY;
        long minSum = Long.MAX_VALUE;

        for (ItemStack matchingItem : matchingItems) {
            ItemStack replacedStack = replacedStack(type, matchingItem);

            if (ElemenixInfo.isUnanalysable(replacedStack)) {
                continue;
            }
            long currentSum = getConstituentsSum(replacedStack);
            if (currentSum < minSum) {
                minSum = currentSum;
                bestItemStack = replacedStack;
            }
        }

        return bestItemStack;
    }

    private static long getConstituentsSum(ItemStack stack) {
        Constituents constituents = ElemenixInfo.getConstituents(stack);
        return constituents != null ? constituents.getSum() : Long.MAX_VALUE;
    }

    private static void handleSmithingRecipe(Recipe<?> recipe, RecipeInfo info) {
        RecipeType<?> type = recipe.getType();

        Optional<Ingredient> templateOpt = tryGetFields(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(), "template");
        Optional<Ingredient> baseOpt = tryGetFields(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(), "base");
        Optional<Ingredient> additionOpt = tryGetFields(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(), "addition");

        if (baseOpt.isEmpty() || additionOpt.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }

        List<Ingredient> validIngredients = new ArrayList<>();
        templateOpt.ifPresent(validIngredients::add);
        validIngredients.add(baseOpt.get());
        validIngredients.add(additionOpt.get());

        for (Ingredient ingredient : validIngredients) {
            ItemStack best = extractBestFromIngredient(ingredient, type);
            if (best.isEmpty()) {
                info.hasUnanalysable = true;
                return;
            }
            info.ingredients.add(best);
            info.amount += best.getCount();
        }
    }

    private static boolean isNormalModRecipe(Recipe<?> recipe){
        try {
            RecipeType<?> type = recipe.getType();
            return RECIPE_TYPES_NORMAL.contains(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isRecipeWithContainer(Recipe<?> recipe) {
        try {
            RecipeType<?> type = recipe.getType();
            return RECIPE_TYPES_WITH_CONTAINER.contains(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCuttingBoardRecipe(Recipe<?> recipe) {
        if(!FARMERS_DELIGHT_LOADED){
          return false;
        }
        try {
            RecipeType<?> type = recipe.getType();
            return type.toString().equals("farmersdelight:cutting");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isMultipleToOneSmithingRecipe(Recipe<?> recipe) {
        if(!ANVILCRAFT_LOADED){
            return false;
        }
        try {
            RecipeType<?> type = recipe.getType();
            return type.toString().contains("anvilcraft:multiple_to_one_smithing") || type.toString().contains("anvilcraft:two_to_one_smithing") ||
                    type.toString().contains("anvilcraft:four_to_one_smithing") || type.toString().contains("anvilcraft:eight_to_one_smithing");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBrewingStandRecipe(Recipe<?> recipe) {
        try {
            RecipeType<?> type = recipe.getType();
            return RECIPE_TYPES_BREWING_LIKE.contains(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWeaponFusionRecipe(Recipe<?> recipe) {
        if(!CATACLYSM_LOADED){
            return false;
        }
        try {
            RecipeType<?> type = recipe.getType();
            return type.toString().equals("cataclysm:weapon_fusion");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isFreezingRecipe(RecipeType<?> type) {
        if(!AETHER_LOADED){
            return false;
        }
        try {
            return type.toString().equals("aether:freezing");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isAetherEnchantingRecipe(RecipeType<?> type) {
        if(!AETHER_LOADED){
            return false;
        }
        try {
            return type.toString().equals("aether:enchanting");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isGlodiumRecipe(Recipe<?> recipe) {
        try {
            RecipeType<?> type = recipe.getType();
            return RECIPE_TYPES_GLODIUM.contains(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isGlodiumsRecipe(Recipe<?> recipe) {
        try {
            RecipeType<?> type = recipe.getType();
            return RECIPE_TYPES_GLODIUMS.contains(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleRecipeWithContainer(Recipe<?> recipe, RecipeInfo info) {
        List<Ingredient> ingredients = getRecipeIngredients(recipe);
        if (ingredients.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient != Ingredient.EMPTY) {
                ItemStack[] matchingItems = ingredient.getItems();
                if (matchingItems.length > 0) {
                    ItemStack bestItemStack = findBestItemStack(recipe.getType(), matchingItems);
                    if(bestItemStack == null || bestItemStack.isEmpty()){
                        info.hasUnanalysable = true;
                    }else {
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
        }

        ItemStack container = getRecipeContainer(recipe);
        if (!container.isEmpty()) {
            info.container = container;
        }
    }

    private static void handleCuttingBoardRecipe(Recipe<?> recipe, RecipeInfo info) {
        Optional<List<?>> rollableResultsOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof List<?> list ? Optional.of(list) : Optional.empty(),
                "getRollableResults");

        if (rollableResultsOpt.isEmpty()) {
            info.isComplex = true;
            return;
        }

        List<?> resultsList = rollableResultsOpt.get();
        if (resultsList.size() != 1) {
            info.isComplex = true;
            return;
        }

        Object chanceResult = resultsList.getFirst();
        Optional<ItemStack> stackOpt = tryInvokeMethods(chanceResult,
                obj -> obj instanceof ItemStack stack && !stack.isEmpty() ? Optional.of(stack) : Optional.empty(),
                "stack", "getStack");

        if (stackOpt.isEmpty()) {
            info.isComplex = true;
            return;
        }

        Optional<Float> chanceOpt = tryInvokeMethods(chanceResult,
                obj -> obj instanceof Float f ? Optional.of(f) : Optional.empty(),
                "chance", "getChance");

        if (chanceOpt.isEmpty() || chanceOpt.get() < 1.0f) {
            info.isComplex = true;
            return;
        }

        info.result = stackOpt.get().copy();
        info.resultAmount = info.result.getCount();

        Optional<Ingredient> inputOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                "getInput");
        if (inputOpt.isPresent()) {
            ItemStack best = extractBestFromIngredient(inputOpt.get(), recipe.getType());
            if (!best.isEmpty()) {
                info.ingredients.add(best);
                info.amount += best.getCount();
            } else {
                info.hasUnanalysable = true;
            }
        } else {
            Optional<List<?>> ingredientsList = tryInvokeMethods(recipe,
                    obj -> obj instanceof List<?> list && !list.isEmpty() ? Optional.of(list) : Optional.empty(),
                    "getIngredients");
            if (ingredientsList.isPresent()) {
                for (Object obj : ingredientsList.get()) {
                    if (obj instanceof Ingredient ing && ing != Ingredient.EMPTY) {
                        ItemStack best = extractBestFromIngredient(ing, recipe.getType());
                        if (!best.isEmpty()) {
                            info.ingredients.add(best);
                            info.amount += best.getCount();
                        } else {
                            info.hasUnanalysable = true;
                        }
                    } else {
                        info.hasUnanalysable = true;
                    }
                }
            } else {
                info.hasUnanalysable = true;
            }
        }
    }

    private static void handleMultipleToOneSmithingRecipe(Recipe<?> recipe, RecipeInfo info) {
        RecipeType<?> type = recipe.getType();

        Optional<Object> materialPredicate = tryGetFields(recipe, obj -> obj != null ? Optional.of(obj) : Optional.empty(), "material");
        Optional<List<?>> inputsList = tryGetFields(recipe, obj -> obj instanceof List<?> list ? Optional.of(list) : Optional.empty(), "inputs");

        if (materialPredicate.isEmpty() || inputsList.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }

        java.util.function.Function<Object, List<ItemStack>> extractStacks = predicate -> {
            List<ItemStack> stacks = new ArrayList<>();
            try {
                Optional<Set<?>> itemsSet = tryInvokeMethods(predicate,
                        obj -> obj instanceof Set<?> set && !set.isEmpty() ? Optional.of(set) : Optional.empty(),
                        "items");
                if (itemsSet.isPresent()) {
                    for (Object itemObj : itemsSet.get()) {
                        if (itemObj instanceof Item item) {
                            stacks.add(new ItemStack(item));
                        }
                    }
                } else {
                    Optional<ItemStack[]> matchingStacks = tryInvokeMethods(predicate,
                            obj -> obj instanceof ItemStack[] arr && arr.length > 0 ? Optional.of(arr) : Optional.empty(),
                            "getMatchingStacks", "getItems");
                    matchingStacks.ifPresent(itemStacks -> stacks.addAll(Arrays.asList(itemStacks)));
                }
            } catch (Exception ignored) {}
            return stacks;
        };

        List<ItemStack> materialCandidates = extractStacks.apply(materialPredicate.get());
        if (materialCandidates.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        ItemStack bestMaterial = findBestItemStack(type, materialCandidates.toArray(new ItemStack[0]));
        if (bestMaterial.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        info.ingredients.add(bestMaterial);
        info.amount += bestMaterial.getCount();

        List<?> inputs = inputsList.get();
        for (Object inputPredicate : inputs) {
            List<ItemStack> inputCandidates = extractStacks.apply(inputPredicate);
            if (inputCandidates.isEmpty()) {
                info.hasUnanalysable = true;
                return;
            }
            ItemStack bestInput = findBestItemStack(type, inputCandidates.toArray(new ItemStack[0]));
            if (bestInput.isEmpty()) {
                info.hasUnanalysable = true;
                return;
            }
            info.ingredients.add(bestInput);
            info.amount += bestInput.getCount();
        }
    }

    private static void handleBrewingStandRecipe(Recipe<?> recipe, RecipeInfo info) {
        RecipeType<?> type = recipe.getType();
        info.isBrewingStandRecipe = true;

        Optional<Ingredient> inputOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                "getInput");
        Optional<Ingredient> bottleOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                "getBottle");

        if (inputOpt.isPresent()) {
            ItemStack best = extractBestFromIngredient(inputOpt.get(), type);
            if (!best.isEmpty()) {
                info.ingredients.add(best);
                info.amount += best.getCount();
            } else {
                info.hasUnanalysable = true;
            }
        }
        if (bottleOpt.isPresent()) {
            ItemStack best = extractBestFromIngredient(bottleOpt.get(), type);
            if (!best.isEmpty()) {
                info.ingredients.add(best);
                info.amount += best.getCount();
            } else {
                info.hasUnanalysable = true;
            }
        }
        if (info.ingredients.isEmpty()) info.hasUnanalysable = true;
    }

    private static void handleWeaponFusionRecipe(Recipe<?> recipe, RecipeInfo info) {
        RecipeType<?> type = recipe.getType();

        Optional<Ingredient> baseOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                "getbaseIngredient", "getBaseIngredient");
        Optional<Ingredient> additionOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                "getAdditionIngredient");

        if (baseOpt.isEmpty() || additionOpt.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }

        ItemStack bestBase = extractBestFromIngredient(baseOpt.get(), type);
        ItemStack bestAddition = extractBestFromIngredient(additionOpt.get(), type);
        if (bestBase.isEmpty() || bestAddition.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        info.ingredients.add(bestBase);
        info.amount += bestBase.getCount();
        info.ingredients.add(bestAddition);
        info.amount += bestAddition.getCount();
    }

    private static void handleGlodiumRecipe(Recipe<?> recipe, RecipeInfo info) {
        Optional<Object> ingredientStackOpt = tryInvokeMethods(recipe,
                obj -> obj != null ? Optional.of(obj) : Optional.empty(),
                "getInput");
        if (ingredientStackOpt.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        ItemStack extracted = extractFromGlodiumStack(ingredientStackOpt.get(), recipe.getType());
        if (extracted.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        info.ingredients.add(extracted);
        info.amount += extracted.getCount();
    }

    private static void handleGlodiumsRecipe(Recipe<?> recipe, RecipeInfo info) {
        Optional<List<?>> inputsOpt = tryInvokeMethods(recipe,
                obj -> obj instanceof List<?> list ? Optional.of(list) : Optional.empty(),
                "getInputs");
        if (inputsOpt.isEmpty()) {
            info.hasUnanalysable = true;
            return;
        }
        List<?> inputs = inputsOpt.get();
        for (Object stackObj : inputs) {
            if (stackObj == null) continue;
            try {
                Method isEmptyMethod = getMethod(stackObj.getClass(), "isEmpty");
                if (isEmptyMethod != null) {
                    Boolean isEmpty = (Boolean) isEmptyMethod.invoke(stackObj);
                    if (isEmpty != null && isEmpty) continue;
                }
            } catch (Exception ignored) {}

            ItemStack extracted = extractFromGlodiumStack(stackObj, recipe.getType());
            if (extracted.isEmpty()) {
                info.hasUnanalysable = true;
                return;
            }
            info.ingredients.add(extracted);
            info.amount += extracted.getCount();
        }
    }

    public static class RecipeInfo {
        public ResourceLocation recipeId;
        public List<ItemStack> ingredients;
        public ItemStack result;
        public RecipeType<?> type;
        public int resultAmount = 1;
        public int amount = 0;
        public long sum = 0;
        public boolean hasUnanalysable = false;
        public boolean isComplex = false;
        public boolean isBrewingStandRecipe = false;
        public ItemStack container = ItemStack.EMPTY;

        public long getSum() {
            if(hasUnanalysable || isComplex){
                return Long.MAX_VALUE;
            }
            return sum;
        }

        public Constituents getConstituents(){
            Constituents constituents = Constituents.sumConstituents(ingredients);

            for(ItemStack stack : ingredients){
                ItemStack remaining = stack.getCraftingRemainingItem();
                if(!remaining.isEmpty()){
                    constituents.deduct(ElemenixInfo.getConstituents(remaining));
                }
            }

            if (isBrewingStandRecipe) {
                if (ingredients.size() >= 2) {
                    ItemStack input = ingredients.get(0);
                    ItemStack bottle = ingredients.get(1);

                    Constituents inputConstituents = ElemenixInfo.getConstituents(input).copy();
                    Constituents bottleConstituents = ElemenixInfo.getConstituents(bottle).copy();

                    if (inputConstituents.isUnanalysable() || bottleConstituents.isUnanalysable()) {
                        return new Constituents(true);
                    }

                    Constituents total = bottleConstituents.copy();
                    inputConstituents.multiply(1.0 / 3.0);
                    total.add(inputConstituents);

                    return total;
                } else {
                    return new Constituents(true);
                }
            }

            if(result.is(ModTags.C_FOODS) || result.getFoodProperties(null) != null){
                for(ItemStack stack : ingredients) {
                    if(stack.is(ModTags.EGGS_WITH_TERRIX_SHELL)){
                        constituents.deduct(Elemenix.TERRIX, ElemenixInfo.getConstituents(stack).get(Elemenix.TERRIX));
                    }
                }
            }

            constituents.multiply(1.0 / resultAmount);

            if (!container.isEmpty()) {
                constituents.add(ElemenixInfo.getConstituents(container));
            }

            if(type == RecipeType.SMELTING || type == RecipeType.SMOKING){
                if(result.getFoodProperties(null) == null){
                    constituents.set(Elemenix.FLUMIX, 0);
                }else{
                    constituents.set(Elemenix.FLUMIX, (int)(constituents.get(Elemenix.FLUMIX) * 0.2));
                    constituents.set(Elemenix.ENERGIX, constituents.get(Elemenix.ENERGIX) + (int)Math.min(10, constituents.getSum()));
                }
            }

            if(isFreezingRecipe(type)){
                constituents.set(Elemenix.FLUMIX, constituents.get(Elemenix.FLUMIX) + 10);
            }
            if(isAetherEnchantingRecipe(type)){
                constituents.set(Elemenix.ENERGIX, constituents.get(Elemenix.ENERGIX) + 10);
            }
            return constituents;
        }
    }

    private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    @Nullable
    private static Method getMethod(Class<?> clazz, String methodName) {
        if (clazz == null || methodName == null) return null;

        return METHOD_CACHE.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(methodName, name -> {
                    try {
                        Method method = clazz.getMethod(name);
                        method.setAccessible(true);
                        return method;
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                });
    }

    @Nullable
    private static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) return null;

        Map<String, Field> classCache = FIELD_CACHE.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        Field field = classCache.get(fieldName);
        if (field != null) {
            return field;
        }

        field = findFieldRecursiveAndCache(clazz, fieldName);
        if (field != null) {
            classCache.put(fieldName, field);
        }
        return field;
    }

    @Nullable
    private static Field findFieldRecursiveAndCache(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) return null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return findFieldRecursiveAndCache(superclass, fieldName);
            }
            return null;
        }
    }

    private static <T> Optional<T> tryInvokeMethods(Object target, Function<Object, Optional<T>> converter, String... methodNames) {
        if (target == null || methodNames == null) return Optional.empty();
        Class<?> clazz = target.getClass();
        for (String name : methodNames) {
            if (name == null) continue;
            Method method = getMethod(clazz, name);
            if (method == null) continue;
            try {
                Object result = method.invoke(target);
                Optional<T> converted = converter.apply(result);
                if (converted.isPresent()) {
                    return converted;
                }
            } catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    private static <T> Optional<T> tryGetFields(Object target, Function<Object, Optional<T>> converter, String... fieldNames) {
        if (target == null || fieldNames == null) return Optional.empty();
        Class<?> clazz = target.getClass();
        for (String name : fieldNames) {
            if (name == null) continue;
            Field field = getField(clazz, name);
            if (field == null) continue;
            try {
                Object value = field.get(target);
                Optional<T> converted = converter.apply(value);
                if (converted.isPresent()) {
                    return converted;
                }
            } catch (IllegalAccessException ignored) {}
        }
        return Optional.empty();
    }

    private static ItemStack extractItemStackFromObject(Object obj) {
        if (obj instanceof ItemStack stack && !stack.isEmpty()) {
            return stack;
        }
        if (obj instanceof Ingredient ingredient && ingredient != Ingredient.EMPTY) {
            ItemStack[] items = ingredient.getItems();
            if (items.length > 0 && items[0] != null && !items[0].isEmpty()) {
                return items[0].copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static Optional<ItemStack> tryExtractItemStack(Object obj) {
        ItemStack stack = extractItemStackFromObject(obj);
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    private static ItemStack extractBestFromIngredient(Ingredient ing, RecipeType<?> type) {
        ItemStack[] items = ing.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        return findBestItemStack(type, items);
    }

    private static ItemStack extractFromGlodiumStack(Object stackObj, RecipeType<?> type) {
        if (stackObj == null) return ItemStack.EMPTY;
        try {
            Optional<Ingredient> ingOpt = tryInvokeMethods(stackObj,
                    obj -> obj instanceof Ingredient ing && ing != Ingredient.EMPTY ? Optional.of(ing) : Optional.empty(),
                    "getIngredient");
            Optional<Integer> amountOpt = tryInvokeMethods(stackObj,
                    obj -> obj instanceof Integer i ? Optional.of(i) : Optional.empty(),
                    "getAmount");
            if (ingOpt.isEmpty() || amountOpt.isEmpty()) return ItemStack.EMPTY;

            Ingredient ing = ingOpt.get();
            int amount = amountOpt.get();
            ItemStack[] items = ing.getItems();
            if (items.length == 0) return ItemStack.EMPTY;

            ItemStack best = findBestItemStack(type, items);
            if (best.isEmpty()) return ItemStack.EMPTY;
            best = best.copy();
            best.setCount(amount);
            return best;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
}
