package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModStats;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        int count = getStoredStacks(stack).size();
        if (count > 0) {
            components.add(Component.translatable("tooltip.elemenix.memorizers", count).withColor(Palette.GRAY_DARK));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
