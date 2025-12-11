package net.lghast.elemenix.compat.jei.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lghast.elemenix.compat.jei.recipe.EnrichingRecipe;
import net.lghast.elemenix.utils.Elemenix;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EnrichingRecipeSerializer implements RecipeSerializer<EnrichingRecipe> {

    public static final EnrichingRecipeSerializer INSTANCE = new EnrichingRecipeSerializer();

    @Override
    public @NotNull MapCodec<EnrichingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.xmap(
                        name -> Elemenix.valueOf(name.toUpperCase()),
                        elemenix -> elemenix.name().toLowerCase()
                ).fieldOf("elemenix").forGetter(EnrichingRecipe::elemenix),
                Codec.INT.fieldOf("amount").forGetter(EnrichingRecipe::amount),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(EnrichingRecipe::output),
                Codec.BOOL.fieldOf("portable").forGetter(EnrichingRecipe::portable)
        ).apply(instance, EnrichingRecipe::new));
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, EnrichingRecipe> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull EnrichingRecipe decode(RegistryFriendlyByteBuf buffer) {
                Elemenix elemenix = buffer.readEnum(Elemenix.class);
                int amount = buffer.readVarInt();
                ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
                boolean portable = buffer.readBoolean();
                return new EnrichingRecipe(elemenix, amount, output, portable);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, EnrichingRecipe recipe) {
                buffer.writeEnum(recipe.elemenix());
                buffer.writeVarInt(recipe.amount());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.output());
                buffer.writeBoolean(recipe.portable());
            }
        };
    }
}