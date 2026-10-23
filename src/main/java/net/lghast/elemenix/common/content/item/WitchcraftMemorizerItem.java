package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.advancement.WaxOffTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOnTrigger;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.common.system.datacomponent.Waxed;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class WitchcraftMemorizerItem extends Item {
    public WitchcraftMemorizerItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponents.POTION_MEMORY_DATA, new PotionMemoryData(new ArrayList<>()))
                .component(ModDataComponents.WAXED, Waxed.unwaxed())
        );
    }

    public static PotionMemoryData getOrCreateMemories(ItemStack stack) {
        if (!(stack.getItem() instanceof WitchcraftMemorizerItem)) {
            return new PotionMemoryData(new ArrayList<>());
        }
        PotionMemoryData data = stack.get(ModDataComponents.POTION_MEMORY_DATA);
        if (data == null) {
            data = new PotionMemoryData(new ArrayList<>());
        }
        return data;
    }

    public static boolean isReadonly(ItemStack stack) {
        if (!(stack.getItem() instanceof WitchcraftMemorizerItem)) {
            return true;
        }
        Waxed waxedData = stack.get(ModDataComponents.WAXED);
        if (waxedData == null) {
            waxedData = Waxed.unwaxed();
        }
        return waxedData.waxed();
    }

    public static boolean isChangeable(ItemStack stack) {
        return stack.is(ModItems.WITCHCRAFT_MEMORIZER) && !isReadonly(stack);
    }

    public static boolean isNotFull(ItemStack stack) {
        return !stack.is(ModItems.WITCHCRAFT_MEMORIZER) || !getOrCreateMemories(stack).isFull();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack memorizerStack = player.getItemInHand(usedHand);
        if (usedHand != InteractionHand.MAIN_HAND) {
            return super.use(level, player, usedHand);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(memorizerStack);
        }

        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.isEmpty()) {
            return InteractionResultHolder.pass(memorizerStack);
        }

        if (isReadonly(memorizerStack)) {
            waxOff(serverLevel, player, memorizerStack, offHandItem);
        } else {
            waxOn(serverLevel, player, memorizerStack, offHandItem);
        }
        return InteractionResultHolder.success(memorizerStack);
    }

    private void waxOn(ServerLevel serverLevel, Player player, ItemStack memorizerStack, ItemStack offHandItem) {
        if (!offHandItem.is(Items.HONEYCOMB)) {
            return;
        }

        if (!player.isCreative()) {
            offHandItem.shrink(1);
        }
        memorizerStack.set(ModDataComponents.WAXED, Waxed.waxedBy(player));
        ModUtils.spawnParticles(serverLevel, ParticleTypes.WAX_ON,
                player.getX(), player.getY() + 1.2, player.getZ(), 0.4, 0.3, 0.4, 8, 0.01);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS);

        player.awardStat(ModStats.MEMORIZERS_WAXED_ON.get());
        if (player instanceof ServerPlayer serverPlayer) {
            WaxOnTrigger.TRIGGER.get().trigger(serverPlayer);
        }
    }

    private void waxOff(ServerLevel serverLevel, Player player, ItemStack memorizerStack, ItemStack offHandItem) {
        if (!offHandItem.is(ItemTags.AXES)) {
            return;
        }

        Waxed waxedData = memorizerStack.get(ModDataComponents.WAXED);
        if (waxedData == null) {
            waxedData = Waxed.unwaxed();
        }

        if (!waxedData.isOwnedBy(player)) {
            if (waxedData.hasOwner()) {
                Waxed finalWaxedData = waxedData;
                player.displayClientMessage(
                        Component.translatable("message.elemenix.memorizer.wax_off_owner_only",
                                waxedData.ownerName().orElseGet(() -> finalWaxedData.ownerUuid().map(UUID::toString).orElse("?"))), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.elemenix.memorizer.wax_off_permanent"), true);
            }
            return;
        }

        offHandItem.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
        memorizerStack.set(ModDataComponents.WAXED, Waxed.unwaxed());
        ModUtils.spawnParticles(serverLevel, ParticleTypes.WAX_OFF,
                player.getX(), player.getY() + 1, player.getZ(), 0.4, 0.3, 0.4, 8, 0.01);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS);

        player.awardStat(ModStats.MEMORIZERS_WAXED_OFF.get());
        if (player instanceof ServerPlayer serverPlayer) {
            WaxOffTrigger.TRIGGER.get().trigger(serverPlayer);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (isReadonly(stack) && ClientConfig.SHOW_READONLY_TOOLTIPS.get()) {
            components.add(getReadonlyTooltip(stack).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (ClientConfig.SHOW_MEMORY_TOOLTIPS.get()) {
            PotionMemoryData memoryData = getOrCreateMemories(stack);
            if (!memoryData.isEmpty()) {
                String memories = String.format(Component.translatable("tooltip.elemenix.memories").getString(), memoryData.entries().size());
                components.add(Component.literal(memories).withStyle(memoryData.isFull() ? ChatFormatting.RED : ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }

    private static MutableComponent getReadonlyTooltip(ItemStack stack) {
        Waxed waxedData = stack.get(ModDataComponents.WAXED);
        if (waxedData != null && waxedData.ownerName().isPresent()) {
            return Component.translatable("tooltip.elemenix.readonly.owner", waxedData.ownerName().get());
        }
        if (waxedData != null && waxedData.ownerUuid().isPresent()) {
            return Component.translatable("tooltip.elemenix.readonly.owner", waxedData.ownerUuid().get().toString());
        }
        return Component.translatable("tooltip.elemenix.readonly.permanent");
    }
}
