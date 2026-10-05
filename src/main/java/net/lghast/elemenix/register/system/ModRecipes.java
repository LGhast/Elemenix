package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.system.recipe.*;
import net.lghast.elemenix.compat.jei.recipe.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Elemenics.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Elemenics.MOD_ID);

    public static final Supplier<RecipeType<EnrichingRecipe>> ENRICHING =
            RECIPE_TYPES.register("enriching", () -> new RecipeType<>() {});

    public static final Supplier<RecipeType<TransformingRecipe>> TRANSFORMING =
            RECIPE_TYPES.register("transforming", () -> new RecipeType<>() {});

    public static final Supplier<RecipeType<MineralizingRecipe>> MINERALIZING =
            RECIPE_TYPES.register("mineralizing", () -> new RecipeType<>() {});

    public static final Supplier<RecipeType<GerminatingRecipe>> GERMINATING =
            RECIPE_TYPES.register("germinating", () -> new RecipeType<>() {});

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MemorizerStylingRecipe>> MEMORIZER_STYLING =
            RECIPE_SERIALIZERS.register("crafting_special_memorizer_styling",
                    () -> new SimpleCraftingRecipeSerializer<>(MemorizerStylingRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MineralizingRecipe>> MINERALIZING_SERIALIZER =
            RECIPE_SERIALIZERS.register("mineralizing", MineralizingRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GerminatingRecipe>> GERMINATING_SERIALIZER =
            RECIPE_SERIALIZERS.register("germinating", GerminatingRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
