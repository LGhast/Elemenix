package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.AnalyzerUuid;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnalyzerItem extends Item {
    public AnalyzerItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE)
                .component(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(new long[6]))
                .component(ModDataComponents.ANALYZER_UUID, AnalyzerUuid.createRandom()));
    }

    public static ElemenicStorage getOrCreateData(ItemStack stack) {
        ElemenicStorage data = stack.get(ModDataComponents.ELEMENIC_STORAGE.get());
        if (data == null) {
            data = new ElemenicStorage(new long[6]);
        }
        return data;
    }

    public static AnalyzerUuid getOrCreateUuid(ItemStack stack) {
        AnalyzerUuid uuid = stack.get(ModDataComponents.ANALYZER_UUID);
        if (uuid == null) {
            uuid = AnalyzerUuid.createRandom();
        }
        return uuid;
    }

    public static ItemStack findAnalyzerByUuid(Player player, UUID uuid) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AnalyzerItem && uuid.equals(getUuid(mainHand))) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof AnalyzerItem && uuid.equals(getUuid(offHand))) {
            return offHand;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof AnalyzerItem && uuid.equals(getUuid(stack))) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static UUID getUuid(ItemStack stack) {
        return getOrCreateUuid(stack).uuid();
    }

    private static void randomizeUuid(ItemStack stack){
        stack.set(ModDataComponents.ANALYZER_UUID, AnalyzerUuid.createRandom());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        randomizeUuid(stack);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(ClientConfig.SHOW_ANALYZER_UUID_TOOLTIPS.get()) {
            components.add(Component.literal(getUuid(stack).toString()));
        }
        if(ClientConfig.SHOW_STORAGE_TOOLTIPS.get()) {
            ElemenicStorage storage = getOrCreateData(stack);
            components.add(Component.translatable("tooltip.elemenix.analyzer_storage").withStyle(ChatFormatting.GRAY));
            components.add(storage.toComponentFormer());
            components.add(storage.toComponentLatter());
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
