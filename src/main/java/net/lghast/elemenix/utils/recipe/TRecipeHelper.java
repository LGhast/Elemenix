package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TRecipeHelper {
    private static final List<TechnicalRecipe> recipes = new ArrayList<>();
    private static boolean handled = false;

    public static void initialize() {
        clear();
        recipes.clear();
        recipes.addAll(JsonTRecipeLoader.loadAllRecipes());
        handleMap();
    }

    public static void clear() {
        handled = false;
    }

    public static void handleMap() {
        if (handled || recipes.isEmpty()) return;

        for (TechnicalRecipe recipe : recipes) {
            ItemStack output = recipe.getOutput();
            if (output.isEmpty()) continue;

            if (ElemenixInfo.isUnanalysableStrictly(output) || ElemenixInfo.hasCache(output.getItem())) {
                continue;
            }

            if (recipe.getCopperState() != null) {
                handleCopperRecipe(recipe, output);
            } else {
                handleNormalRecipe(recipe, output);
            }
        }
        handled = true;
    }

    private static void handleCopperRecipe(TechnicalRecipe recipe, ItemStack output) {
        Constituents totalInput = calculateInputConstituents(recipe.getInputs(), output);
        if (totalInput.isUnanalysable()) return;

        double rate = recipe.getCopperState().getOxidizingRate();
        int metallix = totalInput.get(Elemenix.METALLIX);
        int oxidized = (int) (metallix * rate);
        totalInput.add(Elemenix.TERRIX, oxidized);
        totalInput.deduct(Elemenix.METALLIX, oxidized);

        if (!totalInput.isUnanalysable()) {
            ElemenixInfo.addCache(output.getItem(), totalInput);
        }
    }

    private static void handleNormalRecipe(TechnicalRecipe recipe, ItemStack output) {
        Constituents result = calculateInputConstituents(recipe.getInputs(), output);
        if (result.isUnanalysable()) return;

        for (ItemStack offcut : recipe.getOffcuts()) {
            Constituents offcutCons = getConstituentsSafe(offcut);
            if (offcutCons == null) continue;
            offcutCons.multiply(offcut.getCount());
            result.deduct(offcutCons);
        }

        result.multiply(recipe.getMultiplier());
        result.multiply(1.0 / output.getCount());
        result.add(recipe.getAdditions());
        result.deduct(recipe.getDeductions());

        Elemenix removed = recipe.getRemovedElemenix();
        if (removed != null) result.set(removed, 0);

        Item container = recipe.getContainer();
        if (container != null) {
            Constituents containerCons = getConstituentsSafe(new ItemStack(container));
            if (containerCons != null) result.add(containerCons);
        }

        if (!result.isUnanalysable()) {
            ElemenixInfo.addCache(output.getItem(), result);
        }
    }

    private static Constituents calculateInputConstituents(List<ItemStack> inputs, ItemStack output) {
        Constituents total = new Constituents();
        for (ItemStack input : inputs) {
            if (input.isEmpty()) continue;
            Constituents inputCons = getConstituentsSafe(input);
            if (inputCons == null) continue;

            if (input.is(ModTags.EGGS_WITH_TERRIX_SHELL) && output.is(ModTags.C_FOODS)) {
                inputCons = inputCons.copy();
                inputCons.set(Elemenix.TERRIX, 0);
            }

            inputCons.multiply(input.getCount());
            total.add(inputCons);
        }
        return total;
    }

    private static Constituents getConstituentsSafe(ItemStack stack) {
        Constituents constituents = ElemenixInfo.getConstituents(stack);
        return (constituents != null && !constituents.isUnanalysable()) ? constituents : null;
    }
}
