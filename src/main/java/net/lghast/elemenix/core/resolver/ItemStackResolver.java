package net.lghast.elemenix.core.resolver;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.item.MemorizerBoxItem;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.common.system.datacomponent.Waxed;
import net.lghast.elemenix.compat.avaritia.AvaritiaCompat;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.core.Elemenix;
import net.lghast.elemenix.core.data.JsonPotionlikeLoader;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves one item stack to its elemenic constituents: the item kind's base value plus stack-specific modifiers.
 * <p>Besides the values it reports a {@link StackState}.
 * Any state other than {@link StackState#OK} means the stack cannot be analysed as a whole, and the returned constituents are frozen.
 */
public class ItemStackResolver {
    private static final int MAX_CONTENT_DEPTH = 8;
    private static final ThreadLocal<Integer> CONTENT_DEPTH = ThreadLocal.withInitial(() -> 0);

    /**
     * Resets the thread-local container depth; called when the shared caches are cleared.
     */
    public static void clear() {
        CONTENT_DEPTH.remove();
    }

    /**
     * Resolves the stack against the current level.
     */
    public static StackResult getResult(ItemStack stack) {
        return getResult(stack, Elemenics.getCurrentLevel());
    }

    /**
     * Resolves the stack: the base item value, then every stack-specific modifier, then the stored items
     * of a bundle, shulker box, or memorizer box.
     */
    public static StackResult getResult(ItemStack stack, @Nullable Level level) {
        Constituents result = resolveBase(stack, level);

        if (ModList.get().isLoaded("avaritia")) {
            result = AvaritiaCompat.apply(stack, result, level);
        }
        result = modifyOminousBottle(stack, result);
        result = modifyEnchantedStack(stack, result);
        result = modifyPotionContent(stack, result);
        result = modifyMemorizerWaxed(stack, result, level);
        result = modifyMemorizerStyle(stack, result, level);

        if (result.isUnanalysable()) {
            return new StackResult(result, StackState.UNANALYSABLE);
        }

        return modifyContainerContents(stack, result, level);
    }

    /**
     * State of the stack's last resolution; see {@link StackState}.
     */
    public static StackState getState(ItemStack stack) {
        return getResult(stack).state();
    }

    /**
     * True when the stack is a container holding at least one item that cannot be analysed.
     */
    public static boolean isContentRestricted(ItemStack stack) {
        return hasStoredItems(stack) && getState(stack) != StackState.OK;
    }

    /**
     * Damaged stacks are scaled by their remaining-durability ratio (remaining / max).
     */
    private static Constituents resolveBase(ItemStack stack, @Nullable Level level) {
        Constituents constituents = BaseResolver.getConstituents(stack.getItem(), level);
        if (!stack.isDamaged()) {
            return constituents;
        }

        constituents = constituents.copy();
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        double damageMultiple = (maxDamage - damage) / (double) maxDamage;

        constituents.multiply(damageMultiple);
        return constituents;
    }

    /**
     * Adds (enchantmentTypes - 1) * 5 + totalLevels * 10 arcanix for enchanted stacks.
     */
    private static Constituents modifyEnchantedStack(ItemStack stack, Constituents constituents) {
        if (constituents == null || constituents.isUnanalysable()) {
            return constituents;
        }

        ItemEnchantments normal = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (normal.isEmpty() && stored.isEmpty()) {
            return constituents;
        }

        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(normal);
        for (Holder<Enchantment> enchantment : stored.keySet()) {
            merged.upgrade(enchantment, stored.getLevel(enchantment));
        }

        int enchantmentTypes = 0;
        int totalLevels = 0;
        for (Holder<Enchantment> enchantment : merged.keySet()) {
            enchantmentTypes++;
            totalLevels += merged.getLevel(enchantment);
        }

        int bonus = (enchantmentTypes - 1) * 5 + totalLevels * 10;
        if (bonus <= 0) {
            return constituents;
        }

        Constituents result = constituents.copy();
        result.add(Elemenix.ARCANIX, bonus);
        return result;
    }

    /**
     * Adds 20 energix and 20 arcanix per amplifier level on ominous bottles.
     */
    private static Constituents modifyOminousBottle(ItemStack stack, Constituents constituents) {
        if (constituents == null || constituents.isUnanalysable()) {
            return constituents;
        }

        if (!stack.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER)) {
            return constituents;
        }

        int amplifier = stack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0);
        if (amplifier <= 0) {
            return constituents;
        }

        Constituents scaled = constituents.copy();
        scaled.add(Elemenix.ENERGIX, amplifier * 20L);
        scaled.add(Elemenix.ARCANIX, amplifier * 20L);
        return scaled;
    }

    /**
     * Adds the constituents of a potion's content, scaled by the item's
     * content ratio. Potions without a resolvable potion type (custom potions)
     * are frozen as unanalysable.
     */
    private static Constituents modifyPotionContent(ItemStack stack, Constituents constituents) {
        if (constituents == null || constituents.isUnanalysable()) {
            return constituents;
        }

        if (!stack.is(ModTags.POTIONLIKE)) {
            return constituents;
        }

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return constituents;
        }

        Holder<Potion> potion = contents.potion().orElse(Potions.WATER);

        Constituents potionConstituents = PotionResolver.getConstituents(potion);
        if (potionConstituents.isUnanalysable()) {
            return new Constituents(true);
        }

        double ratio = JsonPotionlikeLoader.getContentRatio(stack.getItem());

        Constituents result = constituents.copy();
        potionConstituents = potionConstituents.copy();
        potionConstituents.multiply(ratio);
        result.add(potionConstituents);
        return result;
    }

    /**
     * Adds the constituents of a honeycomb to waxed (read-only) memorizers.
     */
    private static Constituents modifyMemorizerWaxed(ItemStack stack, Constituents constituents, @Nullable Level level) {
        if (constituents == null || constituents.isUnanalysable()) {
            return constituents;
        }

        if (!(stack.getItem() instanceof MemorizerItem) && !(stack.getItem() instanceof WitchcraftMemorizerItem)) {
            return constituents;
        }

        Waxed waxed = stack.get(ModDataComponents.WAXED);
        if (waxed == null || !waxed.waxed()) {
            return constituents;
        }

        Constituents honeycomb = BaseResolver.getConstituents(Items.HONEYCOMB, level);
        if (honeycomb.isUnanalysable()) {
            return constituents;
        }

        Constituents result = constituents.copy();
        result.add(honeycomb);
        return result;
    }

    /**
     * Adds the constituents of the style item to styled memorizers, both elemenic and witchcraft
     * (the default copper-ingot style is skipped).
     */
    private static Constituents modifyMemorizerStyle(ItemStack stack, Constituents constituents, @Nullable Level level) {
        if (constituents == null || constituents.isUnanalysable()) {
            return constituents;
        }

        if (!(stack.getItem() instanceof MemorizerItem) && !(stack.getItem() instanceof WitchcraftMemorizerItem)) {
            return constituents;
        }

        Style style = stack.get(ModDataComponents.STYLE);
        if (style == null || style.style() == 0) {
            return constituents;
        }

        Item styleItem = MemorizerItem.getStyleItemFor(style.style());
        if (styleItem == null || styleItem == Items.AIR) {
            return constituents;
        }

        Constituents styleConstituents = BaseResolver.getConstituents(styleItem, level);
        if (styleConstituents.isUnanalysable()) {
            return constituents;
        }

        Constituents result = constituents.copy();
        result.add(styleConstituents);
        return result;
    }

    /**
     * Sums the constituents of the stored items, scaled by their counts.
     * <p>Child states are merged with nesting &gt; overflow &gt; unanalysable contents priority.
     */
    private static StackResult modifyContainerContents(ItemStack stack, Constituents constituents, @Nullable Level level) {
        if (!hasStoredItems(stack)) {
            return new StackResult(constituents, StackState.OK);
        }

        int depth = CONTENT_DEPTH.get();
        if (depth >= MAX_CONTENT_DEPTH) {
            return new StackResult(new Constituents(true), StackState.NESTED_CONTAINER);
        }

        CONTENT_DEPTH.set(depth + 1);
        try {
            Constituents result = constituents.copy();
            StackState state = StackState.OK;

            for (ItemStack content : getContentStacks(stack)) {
                if (content.isEmpty()) continue;

                StackResult child = getResult(content, level);
                state = combineState(state, contentState(child.state()));
                if (child.state() != StackState.OK) continue;

                Constituents addition = child.constituents().copy();
                long count = content.getCount();

                for (Elemenix type : Elemenix.values()) {
                    long value = addition.get(type);
                    if (value <= 0) continue;

                    if (count > 1 && value > Long.MAX_VALUE / count) {
                        state = StackState.EXCEEDS_LIMIT;
                        break;
                    }

                    long scaled = value * count;
                    if (result.get(type) > Long.MAX_VALUE - scaled) {
                        state = StackState.EXCEEDS_LIMIT;
                        break;
                    }

                    result.add(type, scaled);
                }
            }

            if (state != StackState.OK) {
                return new StackResult(new Constituents(true), state);
            }
            return new StackResult(result, StackState.OK);
        } finally {
            CONTENT_DEPTH.set(depth);
        }
    }

    /**
     * True when the stack is a bundle, shulker box, memorizer box, or Avaritia matter cluster
     * that currently holds at least one item.
     */
    public static boolean hasStoredItems(ItemStack stack) {
        switch (stack.getItem()) {
            case BundleItem ignored -> {
                BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
                return contents != null && !contents.isEmpty();
            }
            case BlockItem blockItem when blockItem.getBlock() instanceof ShulkerBoxBlock -> {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                return contents != null && contents.nonEmptyItems().iterator().hasNext();
            }
            case MemorizerBoxItem ignored -> {
                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                return contents != null && contents.nonEmptyItems().iterator().hasNext();
            }
            default -> {
            }
        }

        return ModList.get().isLoaded("avaritia") && AvaritiaCompat.hasStoredItems(stack);
    }

    /**
     * Stored stacks of a bundle, shulker box, memorizer box, or Avaritia matter cluster;
     * empty for any other item.
     */
    private static List<ItemStack> getContentStacks(ItemStack stack) {
        List<ItemStack> contents = new ArrayList<>();

        switch (stack.getItem()) {
            case BundleItem ignored -> {
                BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
                if (bundleContents != null) {
                    bundleContents.items().forEach(contents::add);
                }
            }
            case BlockItem blockItem when blockItem.getBlock() instanceof ShulkerBoxBlock -> addContainerContents(stack, contents);
            case MemorizerBoxItem ignored -> addContainerContents(stack, contents);
            default -> {}
        }

        if (ModList.get().isLoaded("avaritia")) {
            contents.addAll(AvaritiaCompat.getContentStacks(stack));
        }
        return contents;
    }

    private static void addContainerContents(ItemStack stack, List<ItemStack> contents) {
        ItemContainerContents containerContents = stack.get(DataComponents.CONTAINER);
        if (containerContents != null) {
            for (ItemStack itemStack : containerContents.nonEmptyItems()) {
                contents.add(itemStack);
            }
        }
    }

    /**
     * Maps a child state to the state it contributes to its parent:
     * <p>A child's own unanalysable kind becomes "contains unanalysable".
     */
    private static StackState contentState(StackState state) {
        return state == StackState.UNANALYSABLE ? StackState.CONTAINS_UNANALYSABLE : state;
    }

    /**
     * Merges two non-{@code OK} states by priority.
     */
    private static StackState combineState(StackState current, StackState incoming) {
        if (current == StackState.NESTED_CONTAINER || incoming == StackState.NESTED_CONTAINER) {
            return StackState.NESTED_CONTAINER;
        }
        if (current == StackState.EXCEEDS_LIMIT || incoming == StackState.EXCEEDS_LIMIT) {
            return StackState.EXCEEDS_LIMIT;
        }
        if (current == StackState.CONTAINS_UNANALYSABLE || incoming == StackState.CONTAINS_UNANALYSABLE) {
            return StackState.CONTAINS_UNANALYSABLE;
        }
        return StackState.OK;
    }

    /**
     * Outcome of resolving one stack.
     */
    public enum StackState {
        OK,
        /**
         * The item kind itself cannot be resolved.
         */
        UNANALYSABLE,
        /**
         * At least one stored item cannot be resolved.
         */
        CONTAINS_UNANALYSABLE,
        /**
         * A value overflowed while summing the stored items.
         */
        EXCEEDS_LIMIT,
        /**
         * Stored containers are nested deeper than the allowed depth.
         */
        NESTED_CONTAINER
    }

    /**
     * Resolved constituents plus the state explaining any failure.
     */
    public record StackResult(Constituents constituents, StackState state) {}
}
