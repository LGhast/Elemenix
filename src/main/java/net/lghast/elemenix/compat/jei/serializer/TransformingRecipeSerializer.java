package net.lghast.elemenix.compat.jei.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lghast.elemenix.compat.jei.recipe.TransformingRecipe;
import net.lghast.elemenix.utils.Elemenix;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class TransformingRecipeSerializer implements RecipeSerializer<TransformingRecipe> {

    public static final TransformingRecipeSerializer INSTANCE = new TransformingRecipeSerializer();

    @Override
    public MapCodec<TransformingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("instrument").forGetter(TransformingRecipe::instrument),
                Codec.STRING.xmap(
                        name -> Elemenix.valueOf(name.toUpperCase()),
                        elemenix -> elemenix.name().toLowerCase()
                ).fieldOf("elemenix1").forGetter(TransformingRecipe::elemenix1),
                Codec.STRING.xmap(
                        name -> Elemenix.valueOf(name.toUpperCase()),
                        elemenix -> elemenix.name().toLowerCase()
                ).fieldOf("elemenix2").forGetter(TransformingRecipe::elemenix2),
                Codec.INT.fieldOf("amount").forGetter(TransformingRecipe::amount),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(TransformingRecipe::output)
        ).apply(instance, TransformingRecipe::new));
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TransformingRecipe> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public TransformingRecipe decode(RegistryFriendlyByteBuf buffer) {
                Ingredient instrument = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Elemenix elemenix1 = buffer.readEnum(Elemenix.class);
                Elemenix elemenix2 = buffer.readEnum(Elemenix.class);
                int amount = buffer.readVarInt();
                ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
                return new TransformingRecipe(instrument, elemenix1, elemenix2, amount, output);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, TransformingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.instrument());
                buffer.writeEnum(recipe.elemenix1());
                buffer.writeEnum(recipe.elemenix2());
                buffer.writeVarInt(recipe.amount());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.output());
            }
        };
    }
}