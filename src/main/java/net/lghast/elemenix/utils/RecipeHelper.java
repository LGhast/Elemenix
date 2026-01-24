package net.lghast.elemenix.utils;

import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
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
import java.util.*;

public class RecipeHelper {
    private static final Map<ResourceLocation, Item> RECIPE_OUTPUT_CACHE = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> RECIPE_ANALYZED_CACHE = new HashMap<>();
    private static final List<ResourceLocation> RECIPE_IGNORED_CACHE = new ArrayList<>();

    private static final List<String> RECIPE_TYPES_NORMAL = new ArrayList<>();
    private static final List<String> RECIPE_TYPES_WITH_CONTAINER = new ArrayList<>();
    private static final List<String> RECIPE_TYPE_BREWING_LIKE = new ArrayList<>();

    private static final boolean FARMERS_DELIGHT_LOADED;
    private static final boolean ANVILCRAFT_LOADED;
    private static final boolean CATACLYSM_LOADED;
    private static final boolean AE2_LOADED;

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        FARMERS_DELIGHT_LOADED = ModUtils.hasServerMod("farmersdelight");
        ANVILCRAFT_LOADED = ModUtils.hasServerMod("anvilcraft");
        CATACLYSM_LOADED = ModUtils.hasServerMod("cataclysm");
        AE2_LOADED = ModUtils.hasServerMod("ae2");

        if(FARMERS_DELIGHT_LOADED){
            RECIPE_TYPES_WITH_CONTAINER.add("farmersdelight:cooking");
        }
        if(ANVILCRAFT_LOADED){
            RECIPE_TYPES_NORMAL.add("anvilcraft:jewel_crafting");
        }
        if(AE2_LOADED){
            RECIPE_TYPES_NORMAL.add("ae2:inscriber");
        }

        if(ModUtils.hasServerMod("twilightforest")){
            RECIPE_TYPES_NORMAL.add("twilightforest:drying");
        }
        if(ModUtils.hasServerMod("dungeonsdelight")){
            RECIPE_TYPES_WITH_CONTAINER.add("dungeonsdelight:monster_cooking");
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
        }
        if(ModUtils.hasServerMod("cobblemon")){
            RECIPE_TYPES_NORMAL.add("cobblemon:cooking_pot");
            RECIPE_TYPE_BREWING_LIKE.add("cobblemon:brewing_stand");
        }
        if(ModUtils.hasServerMod("youkaishomecoming")){
            RECIPE_TYPES_WITH_CONTAINER.add("youkaishomecoming:moka_pot");
            RECIPE_TYPES_WITH_CONTAINER.add("youkaishomecoming:kettle");
            RECIPE_TYPES_NORMAL.add("youkaishomecoming:steaming");
        }
    }

    public static void clearCache() {
        RECIPE_OUTPUT_CACHE.clear();
        RECIPE_ANALYZED_CACHE.clear();
        RECIPE_IGNORED_CACHE.clear();
    }

    private static ItemStack replacedStack(RecipeType<?> type, ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            return new ItemStack(ModItems.NULLVOID.asItem(), stack.getCount());
        }
        if (item == Items.TIPPED_ARROW) {
            return new ItemStack(Items.ARROW, stack.getCount());
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

    private static List<RecipeInfo> getRecipesForItem(Item targetItem, @Nullable Level level) {
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
            ItemStack result = recipe.getResultItem(level.registryAccess());

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

            if (RECIPE_ANALYZED_CACHE.getOrDefault(recipeId, false)) {
                recipes.add(getInfo(recipeHolder, recipe, level));
            } else {
                recipes.add(getInfo(recipeHolder, recipe, level));
                RECIPE_ANALYZED_CACHE.put(recipeId, true);
            }
        }

        return recipes;
    }

    private static RecipeInfo getBestRecipeForItem(Item targetItem, @Nullable Level level) {
        List<RecipeInfo> recipes = getRecipesForItem(targetItem, level);

        if (recipes.isEmpty()) {
            LOGGER.info("Fail to calculate: recipes not found");
            return null;
        }

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

        if(bestRecipe.sum == 0 || bestRecipe.sum == Long.MAX_VALUE){
            return null;
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
                isCookingWithContainerRecipe(recipe) ||
                isCuttingBoardRecipe(recipe) ||
                isMultipleToOneSmithingRecipe(recipe) ||
                isBrewingStandRecipe(recipe) ||
                isWeaponFusionRecipe(recipe);
    }

    private static RecipeInfo getInfo(RecipeHolder<?> holder, Recipe<?> recipe, @Nullable Level level) {
        RecipeInfo info = new RecipeInfo();
        if(level == null) return info;

        info.recipeId = holder.id();
        info.type = recipe.getType();
        info.ingredients = new ArrayList<>();
        info.result = recipe.getResultItem(level.registryAccess());
        info.resultAmount = info.result.getCount();
        info.amount = 0;

        if (isCookingWithContainerRecipe(recipe)) {
            handleCookingWithContainerRecipe(recipe, info);
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
        }else {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
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
        try {
            java.lang.reflect.Method isTemplateIngredientMethod = recipe.getClass().getMethod("isTemplateIngredient", ItemStack.class);
            java.lang.reflect.Method isBaseIngredientMethod = recipe.getClass().getMethod("isBaseIngredient", ItemStack.class);
            java.lang.reflect.Method isAdditionIngredientMethod = recipe.getClass().getMethod("isAdditionIngredient", ItemStack.class);

            Collection<Item> allItems = BuiltInRegistries.ITEM.stream().toList();

            List<ItemStack> templateItems = new ArrayList<>();
            List<ItemStack> baseItems = new ArrayList<>();
            List<ItemStack> additionItems = new ArrayList<>();

            for (Item item : allItems) {
                ItemStack stack = new ItemStack(item);
                if ((Boolean) isTemplateIngredientMethod.invoke(recipe, stack)) {
                    templateItems.add(stack);
                }
                if ((Boolean) isBaseIngredientMethod.invoke(recipe, stack)) {
                    baseItems.add(stack);
                }
                if ((Boolean) isAdditionIngredientMethod.invoke(recipe, stack)) {
                    additionItems.add(stack);
                }
            }


            ItemStack bestTemplate = findBestItemStack(type, templateItems.toArray(new ItemStack[0]));
            ItemStack bestBase = findBestItemStack(type, baseItems.toArray(new ItemStack[0]));
            ItemStack bestAddition = findBestItemStack(type, additionItems.toArray(new ItemStack[0]));

            if(bestTemplate == null || bestBase == null || bestAddition == null){
                info.hasUnanalysable = true;
                return;
            }

            if (!bestTemplate.isEmpty()) {
                info.ingredients.add(bestTemplate);
                info.amount += bestTemplate.getCount();
            }
            if (!bestBase.isEmpty()) {
                info.ingredients.add(bestBase);
                info.amount += bestBase.getCount();
            }
            if (!bestAddition.isEmpty()) {
                info.ingredients.add(bestAddition);
                info.amount += bestAddition.getCount();
            }

            if (bestTemplate.isEmpty() || bestBase.isEmpty() || bestAddition.isEmpty()) {
                info.hasUnanalysable = true;
            }

        } catch (Exception e) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(type, matchingItems);
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
            info.hasUnanalysable = true;
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

    private static boolean isCookingWithContainerRecipe(Recipe<?> recipe) {
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
            return RECIPE_TYPE_BREWING_LIKE.contains(type.toString());
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

    @SuppressWarnings("unchecked")
    private static void handleCookingWithContainerRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            java.lang.reflect.Method getIngredientsMethod = recipe.getClass().getMethod("getIngredients");
            NonNullList<Ingredient> ingredients = (NonNullList<Ingredient>) getIngredientsMethod.invoke(recipe);

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

            try {
                java.lang.reflect.Method getOutputContainerMethod = recipe.getClass().getMethod("getOutputContainer");
                ItemStack container = (ItemStack) getOutputContainerMethod.invoke(recipe);

                if (!container.isEmpty()) {
                    info.container = container;
                }
            } catch (Exception e) {
                try {
                    java.lang.reflect.Method getContainerOverrideMethod = recipe.getClass().getMethod("getContainerOverride");
                    ItemStack container = (ItemStack) getContainerOverrideMethod.invoke(recipe);

                    if (!container.isEmpty()) {
                        info.container = container;
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(recipe.getType(), matchingItems);
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleCuttingBoardRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            java.lang.reflect.Method getRollableResultsMethod = recipe.getClass().getMethod("getRollableResults");
            Object rollableResults = getRollableResultsMethod.invoke(recipe);

            if (rollableResults instanceof List<?> results) {
                if (results.size() != 1) {
                    info.isComplex = true;
                }

                Object firstResult = results.getFirst();

                java.lang.reflect.Method getChanceMethod =
                        firstResult.getClass().getMethod("chance");
                float chance = (Float) getChanceMethod.invoke(firstResult);

                if (Math.abs(chance - 1.0f) > 0.0001f) {
                    info.isComplex = true;
                }

                java.lang.reflect.Method getStackMethod =
                        firstResult.getClass().getMethod("stack");
                ItemStack resultStack = (ItemStack) getStackMethod.invoke(firstResult);
                info.result = resultStack;
                info.resultAmount = resultStack.getCount();
            } else {
                info.isComplex = true;
            }

            java.lang.reflect.Method getInputMethod =
                    recipe.getClass().getMethod("getInput");
            Ingredient input = (Ingredient) getInputMethod.invoke(recipe);

            if (input != Ingredient.EMPTY) {
                ItemStack[] matchingItems = input.getItems();
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
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Method getResultsMethod =
                        recipe.getClass().getMethod("getResults");
                List<ItemStack> results = (List<ItemStack>) getResultsMethod.invoke(recipe);

                if (results.size() != 1) {
                    info.isComplex = true;
                } else {
                    info.result = results.getFirst();
                    info.resultAmount = info.result.getCount();
                }

                java.lang.reflect.Method getIngredientsMethod =
                        recipe.getClass().getMethod("getIngredients");
                NonNullList<Ingredient> ingredients =
                        (NonNullList<Ingredient>) getIngredientsMethod.invoke(recipe);

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
            } catch (Exception ex) {
                info.isComplex = true;
            }
        } catch (Exception e) {
            info.isComplex = true;
        }
    }

    private static void handleMultipleToOneSmithingRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            Class<?> recipeClass = recipe.getClass();

            java.lang.reflect.Method isMaterialIngredientMethod = recipeClass.getMethod("isMaterialIngredient", ItemStack.class);
            java.lang.reflect.Method inputSizeMethod = recipeClass.getMethod("inputSize");

            int inputSize = (Integer) inputSizeMethod.invoke(recipe);
            Collection<Item> allItems = BuiltInRegistries.ITEM.stream().toList();

            List<ItemStack> materialItems = new ArrayList<>();
            for (Item item : allItems) {
                ItemStack stack = new ItemStack(item);
                if ((Boolean) isMaterialIngredientMethod.invoke(recipe, stack)) {
                    materialItems.add(stack);
                }
            }

            ItemStack bestMaterial = findBestItemStack(recipe.getType(), materialItems.toArray(new ItemStack[0]));
            if (bestMaterial == null || bestMaterial.isEmpty()) {
                info.hasUnanalysable = true;
                return;
            }

            info.ingredients.add(bestMaterial);
            info.amount += bestMaterial.getCount();

            for (int i = 0; i < inputSize; i++) {
                List<ItemStack> inputItems = new ArrayList<>();
                for (Item item : allItems) {
                    ItemStack stack = new ItemStack(item);
                    try {
                        java.lang.reflect.Method isInputIngredientMethod =
                                recipeClass.getMethod("isInputIngredient", int.class, ItemStack.class);
                        if ((Boolean) isInputIngredientMethod.invoke(recipe, i, stack)) {
                            inputItems.add(stack);
                        }
                    } catch (NoSuchMethodException e) {
                        break;
                    }
                }

                ItemStack bestInput = findBestItemStack(recipe.getType(), inputItems.toArray(new ItemStack[0]));
                if (bestInput == null || bestInput.isEmpty()) {
                    info.hasUnanalysable = true;
                    return;
                }

                info.ingredients.add(bestInput);
                info.amount += bestInput.getCount();
            }

        } catch (Exception e) {
            info.hasUnanalysable = true;
        }
    }

    private static void handleBrewingStandRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            RecipeType<?> type = recipe.getType();
            java.lang.reflect.Method getInputMethod = recipe.getClass().getMethod("getInput");
            Ingredient inputIngredient = (Ingredient) getInputMethod.invoke(recipe);

            java.lang.reflect.Method getBottleMethod = recipe.getClass().getMethod("getBottle");
            Ingredient bottleIngredient = (Ingredient) getBottleMethod.invoke(recipe);

            if (inputIngredient != Ingredient.EMPTY) {
                ItemStack[] inputItems = inputIngredient.getItems();
                if (inputItems.length > 0) {
                    ItemStack bestInput = findBestItemStack(type, inputItems);
                    if(bestInput == null || bestInput.isEmpty()){
                        info.hasUnanalysable = true;
                    } else {
                        info.ingredients.add(bestInput);
                        info.amount += bestInput.getCount();
                    }
                }
            }

            if (bottleIngredient != Ingredient.EMPTY) {
                ItemStack[] bottleItems = bottleIngredient.getItems();
                if (bottleItems.length > 0) {
                    ItemStack bestBottle = findBestItemStack(type, bottleItems);
                    if(bestBottle == null || bestBottle.isEmpty()){
                        info.hasUnanalysable = true;
                    } else {
                        info.ingredients.add(bestBottle);
                        info.amount += bestBottle.getCount();
                    }
                }
            }
            info.isBrewingStandRecipe = true;
        } catch (Exception e) {
            info.hasUnanalysable = true;
        }
    }

    private static void handleWeaponFusionRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            RecipeType<?> type = recipe.getType();
            java.lang.reflect.Method getBaseIngredientMethod =
                    recipe.getClass().getMethod("getbaseIngredient");
            java.lang.reflect.Method getAdditionIngredientMethod =
                    recipe.getClass().getMethod("getAdditionIngredient");

            Ingredient baseIngredient = (Ingredient) getBaseIngredientMethod.invoke(recipe);
            Ingredient additionIngredient = (Ingredient) getAdditionIngredientMethod.invoke(recipe);

            if (baseIngredient != Ingredient.EMPTY) {
                ItemStack[] baseItems = baseIngredient.getItems();
                if (baseItems.length > 0) {
                    ItemStack bestBase = findBestItemStack(type, baseItems);
                    if(bestBase == null || bestBase.isEmpty()){
                        info.hasUnanalysable = true;
                    } else {
                        info.ingredients.add(bestBase);
                        info.amount += bestBase.getCount();
                    }
                }
            }

            if (additionIngredient != Ingredient.EMPTY) {
                ItemStack[] additionItems = additionIngredient.getItems();
                if (additionItems.length > 0) {
                    ItemStack bestAddition = findBestItemStack(type, additionItems);
                    if(bestAddition == null || bestAddition.isEmpty()){
                        info.hasUnanalysable = true;
                    } else {
                        info.ingredients.add(bestAddition);
                        info.amount += bestAddition.getCount();
                    }
                }
            }
        } catch (Exception e) {
            info.hasUnanalysable = true;
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
                    constituents.minus(ElemenixInfo.getConstituents(remaining));
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

            if(result.getFoodProperties(null) != null){
                for(ItemStack stack : ingredients) {
                    if(stack.is(ModTags.EGGS_WITH_TERRIX_SHELL)){
                        constituents.consume(Elemenix.TERRIX, ElemenixInfo.getConstituents(stack).get(Elemenix.TERRIX));
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
            return constituents;
        }
    }
}
