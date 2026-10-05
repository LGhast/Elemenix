package net.lghast.elemenix.common.system.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MineralizingRecipeSerializer implements RecipeSerializer<MineralizingRecipe> {
    @Override
    public @NotNull MapCodec<MineralizingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("mineral").forGetter(MineralizingRecipe::mineral),
                Ingredient.CODEC.fieldOf("medium").forGetter(MineralizingRecipe::medium),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MineralizingRecipe::result),
                Codec.DOUBLE.optionalFieldOf("flumix_cost", MineralizingRecipe.DEFAULT_FLUMIX_COST)
                        .forGetter(MineralizingRecipe::flumixCost)
        ).apply(instance, MineralizingRecipe::new));
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, MineralizingRecipe> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull MineralizingRecipe decode(RegistryFriendlyByteBuf buffer) {
                Ingredient mineral = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Ingredient medium = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                double flumixCost = buffer.readDouble();
                return new MineralizingRecipe(mineral, medium, result, flumixCost);
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buffer, MineralizingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.mineral());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.medium());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                buffer.writeDouble(recipe.flumixCost());
            }
        };
    }
}
