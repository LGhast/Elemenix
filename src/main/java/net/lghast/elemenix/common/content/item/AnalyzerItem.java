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
        super(properties.rarity(Rarity.RARE).component(ModDataComponents.ANALYZER_UUID, UuidData.createRandom()));
    }

    public static UuidData getOrCreateUuid(ItemStack stack) {
        UuidData uuid = stack.get(ModDataComponents.ANALYZER_UUID);
        if (uuid == null) {
            uuid = UuidData.createRandom();
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
        stack.set(ModDataComponents.ANALYZER_UUID, UuidData.createRandom());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        randomizeUuid(stack);

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(ClientConfig.SHOW_ANALYZER_UUID_TOOLTIPS.get()) {
            components.add(Component.literal(getUuid(stack).toString()));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
