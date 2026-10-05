package net.lghast.elemenix.common.system.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class GerminatingRecipeSerializer implements RecipeSerializer<GerminatingRecipe> {

    @Override
    public @NotNull MapCodec<GerminatingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("template").forGetter(GerminatingRecipe::template),
                ItemStack.STRICT_CODEC.listOf(1, GerminatingRecipe.MAX_PRODUCTS)
                        .fieldOf("products").forGetter(GerminatingRecipe::products)
        ).apply(instance, GerminatingRecipe::new));
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GerminatingRecipe> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull GerminatingRecipe decode(RegistryFriendlyByteBuf buffer) {
                Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                int count = Math.min(buffer.readVarInt(), GerminatingRecipe.MAX_PRODUCTS);
                List<ItemStack> products = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    products.add(ItemStack.STREAM_CODEC.decode(buffer));
                }
                return new GerminatingRecipe(template, products);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, GerminatingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template());
                buffer.writeVarInt(recipe.products().size());
                for (ItemStack product : recipe.products()) {
                    ItemStack.STREAM_CODEC.encode(buffer, product);
                }
            }
        };
    }
}
