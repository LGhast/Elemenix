package net.lghast.elemenix.utils;

import com.ibm.icu.impl.coll.Collation;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecipeHelper {
    private static List<RecipeInfo> getRecipesForItem(Item targetItem) {
        List<RecipeInfo> recipes = new ArrayList<>();
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return Collections.emptyList();
        RecipeManager recipeManager = level.getRecipeManager();

        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            Recipe<?> recipe = recipeHolder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());

            if (!result.isEmpty() && result.getItem() == targetItem && isValidRecipeType(recipe)) {
                recipes.add(getInfo(recipeHolder, recipe));
            }
        }

        return recipes;
    }

    private static RecipeInfo getBestRecipeForItem(Item targetItem) {
        List<RecipeInfo> recipes = getRecipesForItem(targetItem);

        if (recipes.isEmpty()) {
            return null;
        }

        RecipeInfo bestRecipe = recipes.getFirst();

        for (int i = 1; i < recipes.size(); i++) {
            RecipeInfo current = recipes.get(i);
            if(current.hasUnanalysable) continue;
            if(current.isComplexCutting) continue;
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

    public static Constituents getRecipeConstituents(Item item){
        RecipeInfo recipeInfo = getBestRecipeForItem(item);
        if(recipeInfo == null) return null;
        return recipeInfo.getConstituents();
    }

    private static boolean isValidRecipeType(Recipe<?> recipe) {
        RecipeType<?> type = recipe.getType();

        return type == RecipeType.CRAFTING ||
                type == RecipeType.SMELTING ||
                type == RecipeType.STONECUTTING ||
                recipe instanceof SmithingTransformRecipe ||
                isCookingPotRecipe(recipe) ||
                isCuttingBoardRecipe(recipe);
    }

    private static RecipeInfo getInfo(RecipeHolder<?> holder, Recipe<?> recipe) {
        RecipeInfo info = new RecipeInfo();
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return info;
        info.recipeId = holder.id();
        info.type = recipe.getType();
        info.ingredients = new ArrayList<>();
        info.result = recipe.getResultItem(level.registryAccess());
        info.resultAmount = info.result.getCount();
        info.amount = 0;

        if (isCookingPotRecipe(recipe)) {
            handleCookingPotRecipe(recipe, info);
        } else if (isCuttingBoardRecipe(recipe)) {
            handleCuttingBoardRecipe(recipe, info);
        } else if (recipe.getType() == RecipeType.SMITHING) {
            handleSmithingRecipe(recipe, info);
        } else {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(matchingItems);
                        if(bestItemStack.isEmpty()){
                            info.hasUnanalysable = true;
                        }
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
        }

        info.sum = info.getConstituents().getSum();
        return info;
    }

    private static ItemStack findBestItemStack(ItemStack[] matchingItems) {
        if (matchingItems.length == 1) {
            return matchingItems[0];
        }

        ItemStack bestItemStack = ItemStack.EMPTY;
        long minSum = Long.MAX_VALUE;

        for (ItemStack matchingItem : matchingItems) {
            if (ElemenixInfo.getConstituents(matchingItem).isUnanalysable()) {
                continue;
            }
            long currentSum = getConstituentsSum(matchingItem);
            if (currentSum < minSum) {
                minSum = currentSum;
                bestItemStack = matchingItem;
            }
        }

        return bestItemStack;
    }

    private static long getConstituentsSum(ItemStack stack) {
        Constituents constituents = ElemenixInfo.getConstituents(stack);
        return constituents != null ? constituents.getSum() : Long.MAX_VALUE;
    }

    private static void handleSmithingRecipe(Recipe<?> recipe, RecipeInfo info) {
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

            ItemStack bestTemplate = findBestItemStack(templateItems.toArray(new ItemStack[0]));
            ItemStack bestBase = findBestItemStack(baseItems.toArray(new ItemStack[0]));
            ItemStack bestAddition = findBestItemStack(additionItems.toArray(new ItemStack[0]));

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
                        ItemStack bestItemStack = findBestItemStack(matchingItems);
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
            info.hasUnanalysable = true;
        }
    }

    private static boolean isCookingPotRecipe(Recipe<?> recipe) {
        try {
            return recipe.getClass().getName().contains("CookingPotRecipe") ||
                    recipe.getType().toString().contains("farmersdelight:cooking");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCuttingBoardRecipe(Recipe<?> recipe) {
        try {
            return recipe.getClass().getName().contains("CuttingBoardRecipe") ||
                    recipe.getType().toString().contains("farmersdelight:cutting");
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleCookingPotRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            java.lang.reflect.Method getIngredientsMethod = recipe.getClass().getMethod("getIngredients");
            NonNullList<Ingredient> ingredients = (NonNullList<Ingredient>) getIngredientsMethod.invoke(recipe);

            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(matchingItems);
                        if(bestItemStack.isEmpty()){
                            info.hasUnanalysable = true;
                        }
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
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
                        ItemStack bestItemStack = findBestItemStack(matchingItems);
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
        }
    }

    private static void handleCuttingBoardRecipe(Recipe<?> recipe, RecipeInfo info) {
        try {
            java.lang.reflect.Method getRollableResultsMethod = recipe.getClass().getMethod("getRollableResults");
            Object rollableResults = getRollableResultsMethod.invoke(recipe);

            if (rollableResults instanceof List<?> results) {
                if (results.size() != 1) {
                    info.isComplexCutting = true;
                }

                Object firstResult = results.getFirst();
                java.lang.reflect.Method getChanceMethod = firstResult.getClass().getMethod("chance");
                float chance = (Float) getChanceMethod.invoke(firstResult);

                if (chance != 1.0f) {
                    info.isComplexCutting = true;
                }

                java.lang.reflect.Method getStackMethod = firstResult.getClass().getMethod("stack");
                ItemStack resultStack = (ItemStack) getStackMethod.invoke(firstResult);
                info.result = resultStack;
                info.resultAmount = resultStack.getCount();
            }

            java.lang.reflect.Method getInputMethod = recipe.getClass().getMethod("getInput");
            Ingredient input = (Ingredient) getInputMethod.invoke(recipe);

            if (input != Ingredient.EMPTY) {
                ItemStack[] matchingItems = input.getItems();
                if (matchingItems.length > 0) {
                    ItemStack bestItemStack = findBestItemStack(matchingItems);
                    if(bestItemStack.isEmpty()){
                        info.hasUnanalysable = true;
                    }
                    info.ingredients.add(bestItemStack);
                    info.amount += bestItemStack.getCount();
                }
            }
        } catch (Exception e) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (Ingredient ingredient : ingredients) {
                if (ingredient != Ingredient.EMPTY) {
                    ItemStack[] matchingItems = ingredient.getItems();
                    if (matchingItems.length > 0) {
                        ItemStack bestItemStack = findBestItemStack(matchingItems);
                        info.ingredients.add(bestItemStack);
                        info.amount += bestItemStack.getCount();
                    }
                }
            }
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
        public boolean isComplexCutting = false;
        public ItemStack container = ItemStack.EMPTY;

        public long getSum() {
            if(hasUnanalysable || isComplexCutting){
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

            if(result.getFoodProperties(null) != null){
                for(ItemStack stack : ingredients) {
                    if(stack.is(ModTags.EGGS_WITH_TERRIX_SHELL)){
                        constituents.consume(Elemenix.TERRIX, ElemenixInfo.getConstituents(stack).get(Elemenix.TERRIX));
                    }
                }
            }

            if (!container.isEmpty()) {
                constituents.add(ElemenixInfo.getConstituents(container));
            }

            constituents.multiply(1.0 / resultAmount);

            if(type == RecipeType.SMELTING){
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
