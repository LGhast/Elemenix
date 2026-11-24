package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.common.system.datacomponent.MemorizerSlotLock;
import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.lghast.elemenix.common.util.ModUtils;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModSounds;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MemorizerBoxItem extends Item implements ICurioItem {

    public MemorizerBoxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void createUuid(ItemStack stack) {
        UuidData uuid = stack.get(ModDataComponents.BOX_UUID);

        if (uuid == null) {
            uuid = UuidData.createRandom();
            stack.set(ModDataComponents.BOX_UUID, uuid);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        createUuid(stack);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown() && ModUtils.tryEquipToCurios(player, stack, "memorizer_box")) {
            return InteractionResultHolder.success(stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) ->
                            new MemorizerBoxMenu(windowId, playerInventory, stack),
                    Component.translatable("gui.elemenix.memorizer_box")
            ));
            serverPlayer.awardStat(ModStats.MEMORIZER_BOX_OPENS.get());
        }

        return InteractionResultHolder.success(stack);
    }

    public static List<ItemStack> getStoredStacks(ItemStack box) {
        ItemContainerContents contents = box.get(DataComponents.CONTAINER);
        if (contents == null) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        contents.nonEmptyItems().forEach(stacks::add);
        return stacks;
    }

    public static MemorizerSlotLock getStyleLock(ItemStack stack) {
        MemorizerSlotLock lock = stack.get(ModDataComponents.STYLE_LOCK);
        return lock != null ? lock : MemorizerSlotLock.EMPTY;
    }

    public static boolean isStyleLockEnabled(ItemStack stack) {
        return stack.has(ModDataComponents.STYLE_LOCK.get());
    }

    public static void setStyleLock(ItemStack stack, MemorizerSlotLock lock) {
        stack.set(ModDataComponents.STYLE_LOCK, lock);
    }

    public static void clearStyleLock(ItemStack stack) {
        stack.remove(ModDataComponents.STYLE_LOCK.get());
    }

    public static MemorizerSlotLock captureStyleLock(Container container) {
        List<MemorizerSlotLock.Entry> entries = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE)) continue;
            if (stack.getItem() instanceof MemorizerBoxItem) continue;
            entries.add(new MemorizerSlotLock.Entry(i, BuiltInRegistries.ITEM.getKey(stack.getItem()), getStyleFor(stack)));
        }
        return new MemorizerSlotLock(entries);
    }

    public static boolean matchesLock(ItemStack stack, MemorizerSlotLock.Entry entry) {
        if (stack.isEmpty()) return false;
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(entry.kind())) return false;
        return getStyleFor(stack) == entry.style();
    }

    private static int getStyleFor(ItemStack stack) {
        Style style = stack.get(ModDataComponents.STYLE);
        return style != null ? style.style() : MemorizerSlotLock.NO_STYLE;
    }

    public static void collectMemorizers(ServerPlayer player, ItemStack box) {
        ItemContainerContents contents = box.get(DataComponents.CONTAINER);
        List<ItemStack> slots = new ArrayList<>(18);
        for (int i = 0; i < 18; i++) {
            slots.add(contents != null && i < contents.getSlots() ? contents.getStackInSlot(i).copy() : ItemStack.EMPTY);
        }

        MemorizerSlotLock lock = getStyleLock(box);
        boolean lockEnabled = isStyleLockEnabled(box);

        boolean collected = false;
        for (ItemStack memorizer : player.getInventory().items) {
            if (memorizer.isEmpty() || !memorizer.is(ModTags.MEMORIZER_SLOT_PLACEABLE)) continue;
            if (memorizer.getItem() instanceof MemorizerBoxItem) continue;

            int target = findCollectSlot(slots, lock, lockEnabled, memorizer);
            if (target < 0) continue;

            slots.set(target, memorizer.copy());
            memorizer.setCount(0);
            collected = true;
        }

        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
        if (collected) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MEMORIZER_PLACED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static int findCollectSlot(List<ItemStack> slots, MemorizerSlotLock lock, boolean lockEnabled, ItemStack memorizer) {
        if (lockEnabled) {
            for (int i = 0; i < 18; i++) {
                if (slots.get(i).isEmpty()) {
                    MemorizerSlotLock.Entry entry = lock.getEntry(i);
                    if (entry != null && matchesLock(memorizer, entry)) {
                        return i;
                    }
                }
            }
            for (int i = 0; i < 18; i++) {
                if (slots.get(i).isEmpty() && lock.getEntry(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < 18; i++) {
                if (slots.get(i).isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        int count = getStoredStacks(stack).size();
        if (count > 0) {
            components.add(Component.translatable("tooltip.elemenix.memorizers", count).withColor(Palette.GRAY_DARK));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
