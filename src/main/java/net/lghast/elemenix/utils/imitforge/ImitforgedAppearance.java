package net.lghast.elemenix.utils.imitforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ImitforgedAppearance(ItemStack itemStack) {
    public static final Codec<ImitforgedAppearance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("itemStack").forGetter(ImitforgedAppearance::itemStack)
    ).apply(instance, ImitforgedAppearance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImitforgedAppearance> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            ImitforgedAppearance::itemStack,
            ImitforgedAppearance::new
    );

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImitforgedAppearance that = (ImitforgedAppearance) o;
        return ItemStack.isSameItemSameComponents(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(itemStack);
    }
}
