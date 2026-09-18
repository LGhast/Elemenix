package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

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

    public static void randomizeUuid(ItemStack stack) {
        stack.set(ModDataComponents.BOX_UUID, UuidData.createRandom());
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
        }

        return InteractionResultHolder.success(stack);
    }
}
