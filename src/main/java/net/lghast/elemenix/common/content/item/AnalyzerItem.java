package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AnalyzerItem extends StorageItem {

    public AnalyzerItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    public static UuidData getOrCreateUuid(ItemStack stack) {
        UuidData uuid = stack.get(ModDataComponents.ANALYZER_UUID);

        if (uuid == null) {
            uuid = UuidData.createRandom();
            stack.set(ModDataComponents.ANALYZER_UUID, uuid);
        }

        return uuid;
    }

    public static ItemStack findAnalyzerByUuid(Player player, UUID uuid) {
        ItemStack mainHand = player.getMainHandItem();
        if (isSameAnalyzer(mainHand, uuid)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (isSameAnalyzer(offHand, uuid)) {
            return offHand;
        }

        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (isSameAnalyzer(stack, uuid)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isSameAnalyzer(ItemStack stack, UUID uuid) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof AnalyzerItem)) {
            return false;
        }

        UuidData currentUuid = stack.get(ModDataComponents.ANALYZER_UUID);
        return currentUuid != null && uuid.equals(currentUuid.uuid());
    }

    public static UUID getUuid(ItemStack stack) {
        return getOrCreateUuid(stack).uuid();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        getOrCreateUuid(stack);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        player.awardStat(ModStats.OPEN_ANALYZER.get());

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) ->
                            new AnalyzerMenu(windowId, playerInventory, stack),
                    Component.translatable("gui.elemenix.analyzer")
            ));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> components,
            TooltipFlag tooltipFlag
    ) {
        if (ClientConfig.SHOW_ANALYZER_UUID_TOOLTIPS.get()) {
            components.add(Component.literal(getUuid(stack).toString()));
        }

        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
