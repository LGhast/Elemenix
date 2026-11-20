package net.lghast.elemenix.compat.avaritia;

import committee.nova.mods.avaritia.common.component.InfinityContainerContents;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.core.resolver.BaseResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class AvaritiaCompat {
    private AvaritiaCompat() {
    }

    public static Constituents apply(ItemStack stack, Constituents constituents, @Nullable Level level) {
        Singularity singularity = SingularityUtils.getSingularity(stack);
        if (singularity == null) {
            return constituents;
        }

        Ingredient material = singularity.getIngredient();
        int count = singularity.getCount();
        if (material == null || material == Ingredient.EMPTY || count <= 0) {
            return constituents;
        }

        Constituents materialConstituents = getCheapestMaterialConstituents(material, level);
        if (materialConstituents == null || materialConstituents.isUnanalysable()) {
            return new Constituents(true);
        }

        Constituents result = materialConstituents.copy();
        result.multiply(count);
        return result;
    }

    @Nullable
    private static Constituents getCheapestMaterialConstituents(Ingredient ingredient, @Nullable Level level) {
        Constituents best = null;
        long bestSum = Long.MAX_VALUE;

        for (ItemStack item : ingredient.getItems()) {
            if (item.isEmpty()) {
                continue;
            }
            Constituents constituents = BaseResolver.getConstituents(item.getItem(), level);
            if (constituents == null || constituents.isUnanalysable()) {
                continue;
            }
            long sum = constituents.getSum();
            if (sum <= 0 || sum == Long.MAX_VALUE) {
                continue;
            }
            if (sum < bestSum) {
                bestSum = sum;
                best = constituents;
            }
        }

        return best;
    }

    public static boolean hasStoredItems(ItemStack stack) {
        InfinityContainerContents contents = stack.get(ModDataComponents.MATTER_CLUSTER);
        return contents != null && contents.nonEmptyItems().iterator().hasNext();
    }

    public static List<ItemStack> getContentStacks(ItemStack stack) {
        InfinityContainerContents contents = stack.get(ModDataComponents.MATTER_CLUSTER);
        if (contents == null) {
            return List.of();
        }

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : contents.nonEmptyItems()) {
            items.add(item);
        }
        return items;
    }
}
