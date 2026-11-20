package net.lghast.elemenix.common.system.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WitchLootModifier extends LootModifier {
    public static final MapCodec<WitchLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(ItemStack.CODEC.fieldOf("item").forGetter(m -> m.item))
                    .apply(inst, WitchLootModifier::new));

    private final ItemStack item;

    public WitchLootModifier(LootItemCondition[] conditions, ItemStack item) {
        super(conditions);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(item.copy());
        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<WitchLootModifier> codec() {
        return CODEC;
    }
}
