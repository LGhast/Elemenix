package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.elemenix.FluidElemenixInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Solves all technical recipes and writes the results into ElemenixInfo's item cache.
 */
public class TRecipeHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<TechnicalRecipe> recipes = new ArrayList<>();
    private static boolean handled = false;

    private static final Map<Item, Constituents> PASS_ITEM_CACHE = new HashMap<>();
    private static boolean handling = false;

    /**
     * Loads JSON T-recipes and solves them; runs only when world recipes are ready.
     */
    public static void initialize() {
        if(!Elemenics.started) return;

        Level level = Elemenics.getCurrentLevel();
        if (!RecipeHelper.areRecipesLoaded(level)) {
            return;
        }

        clear();
        recipes.clear();
        recipes.addAll(JsonTRecipeLoader.loadAllRecipes());
        handleMap();
    }

    public static void clear() {
        handled = false;
    }

    public static boolean isHandled() {
        return handled;
    }

    public static boolean isHandling() {
        return handling;
    }

    public static void ensureHandled() {
        if (!handled) {
            handleMap();
        }
    }

    /**
     * Mid-solving constituents for T-recipe outputs, before promotion to the main cache.
     */
    @Nullable
    public static Constituents getPassConstituents(Item item) {
        return PASS_ITEM_CACHE.get(item);
    }

    public static void putPassConstituents(Item item, Constituents constituents) {
        if (item != null && constituents != null) {
            PASS_ITEM_CACHE.put(item, constituents.copy());
        }
    }


    /**
     * Iterates recipes in strict-then-loose rounds until a fixed point,
     * so dependencies are always resolved before their consumers.
     */
    public static void handleMap() {
        if (handled) return;

        handling = true;
        PASS_ITEM_CACHE.clear();

        try {
            if (!recipes.isEmpty()) {
                long startTime = System.currentTimeMillis();
                Set<Item> tOutputs = collectTRecipeOutputItems();

                int guard = recipes.size() + 1;
                boolean changed = true;
                while (changed && guard-- > 0) {
                    changed = handleFluidRecipesOnce(tOutputs);

                    for (TechnicalRecipe recipe : recipes) {
                        ItemStack output = recipe.getOutput();
                        if (output.isEmpty()) continue;
                        if (ElemenixInfo.isUnanalysableStrictly(output) || ElemenixInfo.hasCache(output.getItem())) {
                            continue;
                        }

                        boolean applied;
                        if (recipe.getCopperState() != null) {
                            applied = handleCopperRecipe(recipe, output, true, tOutputs);
                        } else {
                            applied = handleNormalRecipe(recipe, output, true, tOutputs);
                        }
                        if (applied) changed = true;
                    }
                }

                for (TechnicalRecipe recipe : recipes) {
                    ItemStack output = recipe.getOutput();
                    if (output.isEmpty()) continue;
                    if (ElemenixInfo.isUnanalysableStrictly(output) || ElemenixInfo.hasCache(output.getItem())) {
                        continue;
                    }

                    if (recipe.getCopperState() != null) {
                        handleCopperRecipe(recipe, output, false, tOutputs);
                    } else {
                        handleNormalRecipe(recipe, output, false, tOutputs);
                    }
                }

                LOGGER.info("TRecipeHelper processed {} recipes in {} ms",
                        recipes.size(), System.currentTimeMillis() - startTime);
            }
        } finally {
            handling = false;
            PASS_ITEM_CACHE.clear();
            handled = true;
        }
    }

    /**
     * Solves an item-output recipe, applies adjustments and caches the result.
     *
     * @return true if the result was cached
     */
    private static boolean handleNormalRecipe(TechnicalRecipe recipe, ItemStack output, boolean strict, Set<Item> tOutputs) {
        Constituents result = calculateInputConstituents(recipe, output, strict, tOutputs);
        if (result == null || result.isUnanalysable()) return false;

        applyCommonAdjustments(recipe, result, output.getCount());

        Item container = recipe.getContainer();
        if (container != null) {
            Constituents containerCons = getConstituentsSafe(new ItemStack(container));
            if (containerCons != null) result.add(containerCons);
        }

        if (!result.isUnanalysable()) {
            ElemenixInfo.addCache(output.getItem(), result);
            return true;
        }
        return false;
    }


    /**
     * Derives constituents for a fluid-output recipe and stores them as a derived fluid mapping.
     *
     * @return true if a new fluid mapping was created
     */
    private static boolean handleFluidRecipesOnce(Set<Item> tOutputs) {
        boolean changed = false;
        for (TechnicalRecipe recipe : recipes) {
            if (!recipe.isFluidOutput()) continue;

            Fluid fluid = recipe.getFluidOutput().getFluid();
            if (FluidElemenixInfo.hasConstituents(fluid)) continue;

            Constituents result = calculateInputConstituents(recipe, null, true, tOutputs);
            if (result == null || result.isUnanalysable()) continue;

            applyCommonAdjustments(recipe, result, recipe.getFluidOutputCount());

            if (!result.isUnanalysable() && result.getSum() > 0) {
                FluidElemenixInfo.putDerived(fluid, result);
                PASS_ITEM_CACHE.clear();
                changed = true;
            }
        }
        return changed;
    }

    private static boolean handleCopperRecipe(TechnicalRecipe recipe, ItemStack output, boolean strict, Set<Item> tOutputs) {
        Constituents totalInput = calculateInputConstituents(recipe, output, strict, tOutputs);
        if (totalInput == null || totalInput.isUnanalysable()) return false;

        double rate = recipe.getCopperState().getOxidizingRate();
        int metallix = totalInput.get(Elemenix.METALLIX);
        int oxidized = (int) (metallix * rate);
        totalInput.add(Elemenix.TERRIX, oxidized);
        totalInput.deduct(Elemenix.METALLIX, oxidized);

        if (!totalInput.isUnanalysable()) {
            ElemenixInfo.addCache(output.getItem(), totalInput);
            return true;
        }
        return false;
    }


    /**
     * Applies offcuts, multiplier, additions/deductions and the removed-Elemenix rule.
     */
    private static void applyCommonAdjustments(TechnicalRecipe recipe, Constituents result, int outputCount) {
        for (ItemStack offcut : recipe.getOffcuts()) {
            Constituents offcutCons = getConstituentsSafe(offcut);
            if (offcutCons == null) continue;
            offcutCons.multiply(offcut.getCount());
            result.deduct(offcutCons);
        }

        for (FluidStack offcut : recipe.getFluidOffcuts()) {
            Constituents perUnit = FluidElemenixInfo.getMappedConstituents(offcut.getFluid());
            if (perUnit == null) continue;
            if (perUnit.isUnanalysable()) continue;
            long rawUnits = offcut.getAmount() / FluidElemenixInfo.FLUID_UNIT;
            if (rawUnits <= 0) continue;
            int units = (int) rawUnits;
            perUnit = perUnit.copy();
            perUnit.multiply(units);
            result.deduct(perUnit);
        }

        result.multiply(recipe.getMultiplier());
        result.multiply(1.0 / outputCount);
        result.add(recipe.getAdditions());
        result.deduct(recipe.getDeductions());

        Elemenix removed = recipe.getRemovedElemenix();
        if (removed != null) result.set(removed, 0);
    }

    /**
     * Sums item and fluid input constituents.
     * In strict mode, an unresolved T-recipe output among the inputs returns null.
     */
    @Nullable
    private static Constituents calculateInputConstituents(TechnicalRecipe recipe, @Nullable ItemStack output,
                                                           boolean strict, Set<Item> tOutputs) {
        Constituents total = new Constituents();

        for (ItemStack input : recipe.getInputs()) {
            if (input.isEmpty()) continue;

            if (strict && tOutputs.contains(input.getItem()) && !ElemenixInfo.hasCache(input.getItem())) {
                return null;
            }

            Constituents inputCons = getConstituentsSafe(input);
            if (inputCons == null) continue;

            if (output != null && input.is(ModTags.EGGS_WITH_TERRIX_SHELL) && output.is(ModTags.C_FOODS)) {
                inputCons = inputCons.copy();
                inputCons.set(Elemenix.TERRIX, 0);
            }

            inputCons.multiply(input.getCount());
            total.add(inputCons);
        }

        for (FluidStack fluidInput : recipe.getFluidInputs()) {
            Fluid fluid = fluidInput.getFluid();
            Constituents perUnit = FluidElemenixInfo.getMappedConstituents(fluid);

            if (perUnit == null) {
                if (strict) {
                    return null;
                }
                continue;
            }
            if (perUnit.isUnanalysable()) {
                return new Constituents(true);
            }

            long rawUnits = fluidInput.getAmount() / FluidElemenixInfo.FLUID_UNIT;
            if (rawUnits <= 0) continue;
            int units = (int) rawUnits;

            perUnit = perUnit.copy();
            perUnit.multiply(units);
            total.add(perUnit);
        }

        return total;
    }

    private static Constituents getConstituentsSafe(ItemStack stack) {
        Constituents constituents = ElemenixInfo.getConstituents(stack);
        return (constituents != null && !constituents.isUnanalysable()) ? constituents : null;
    }

    private static Set<Item> collectTRecipeOutputItems() {
        Set<Item> set = new HashSet<>();
        for (TechnicalRecipe recipe : recipes) {
            ItemStack output = recipe.getOutput();
            if (!output.isEmpty()) {
                set.add(output.getItem());
            }
        }
        return set;
    }
}
