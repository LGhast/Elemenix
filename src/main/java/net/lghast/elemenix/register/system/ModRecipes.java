package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.compat.jei.recipe.EnrichingRecipe;
import net.lghast.elemenix.compat.jei.recipe.TransformingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Elemenics.MOD_ID);

    public static final Supplier<RecipeType<EnrichingRecipe>> ENRICHING =
            RECIPE_TYPES.register("enriching", () -> new RecipeType<>() {});

    public static final Supplier<RecipeType<TransformingRecipe>> TRANSFORMING =
            RECIPE_TYPES.register("transforming", () -> new RecipeType<>() {});

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
