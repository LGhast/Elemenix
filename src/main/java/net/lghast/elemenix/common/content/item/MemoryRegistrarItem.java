package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.lghast.elemenix.common.system.menu.RegistrarMenu;
import net.lghast.elemenix.network.registrar.ResponseRegistrarSyncPayload;
import net.lghast.elemenix.register.system.ModAttachments;
import net.lghast.elemenix.register.system.ModStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public class MemoryRegistrarItem extends BlockItem {

    public MemoryRegistrarItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            openRegistrarGui(serverPlayer);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            InteractionResult result = super.useOn(context);
            if (result.consumesAction() && player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return result;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            openRegistrarGui(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static void openRegistrarGui(ServerPlayer player) {
        player.awardStat(ModStats.MEMORY_REGISTRAR_OPENS.get());
        RegistrarCollection collection = player.getData(ModAttachments.REGISTRAR_COLLECTION);
        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new RegistrarMenu(windowId),
                Component.translatable("gui.elemenix.registrar")
        ));
        PacketDistributor.sendToPlayer(player, new ResponseRegistrarSyncPayload(
                new ArrayList<>(collection.items()), new ArrayList<>(collection.potions())));
    }
}
