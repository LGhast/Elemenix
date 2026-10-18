package net.lghast.elemenix.utils.imitforge;

import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Imitforging {
    public static boolean isDisabled() {
        return CommonConfig.DISABLE_IMITFORGING.get();
    }

    public static boolean isImitforgable(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.IMITFORGABLE);
    }

    public static boolean isAllowedAppearance(ItemStack stack) {
        return stack.isEmpty() || !stack.is(ModTags.IMITFORGING_APPEARANCE_FORBIDDEN);
    }

    public static boolean isImitforged(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ImitforgedAppearance appearance = stack.get(ModDataComponents.IMITFORGED_APPEARANCE.get());
        return appearance != null && !appearance.itemStack().isEmpty();
    }

    public static boolean canApply(ItemStack target, ItemStack appearance) {
        if (isDisabled()) return false;
        if (target.isEmpty()) return false;
        if (!isImitforgable(target)) return false;
        if (isImitforged(target) && appearance.isEmpty()) return true;
        if (appearance.isEmpty()) return false;
        if (target.getItem() == appearance.getItem()) return false;
        return isAllowedAppearance(appearance);
    }

    public static ItemStack forgeResult(ItemStack target, ItemStack appearance) {
        if (isImitforged(target) && appearance.isEmpty()) {
            return removalResult(target);
        }

        ItemStack result = target.copy();
        result.setCount(1);

        ResourceLocation origin = result.get(ModDataComponents.IMITFORGED_ORIGIN.get());
        if (origin == null) {
            origin = BuiltInRegistries.ITEM.getKey(target.getItem());
        }

        result.set(ModDataComponents.IMITFORGED_APPEARANCE.get(), new ImitforgedAppearance(appearance.copy()));
        result.set(ModDataComponents.IMITFORGED_ORIGIN.get(), origin);
        return result;
    }

    public static ItemStack removalResult(ItemStack target) {
        ResourceLocation origin = target.get(ModDataComponents.IMITFORGED_ORIGIN.get());
        Item item = origin != null ? BuiltInRegistries.ITEM.get(origin) : target.getItem();
        return new ItemStack(item);
    }

    public static ItemStack getAppearanceStackOrOriginal(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        ImitforgedAppearance appearance = stack.get(ModDataComponents.IMITFORGED_APPEARANCE.get());
        if (appearance != null && !appearance.itemStack().isEmpty()) {
            return appearance.itemStack();
        }
        return stack;
    }

    public static int getEnergixCost(ItemStack target) {
        if (target.isEmpty()) return -1;
        Constituents constituents = ElemenixInfo.getConstituents(target);
        if (constituents == null || constituents.isUnanalysable()) return -1;

        long sum = constituents.getSum();
        if (sum <= 0) return -1;

        return (int) Math.max(1, Math.ceil(sum * CommonConfig.IMITFORGING_ENERGIX_COST_RATIO.get()));
    }
}
