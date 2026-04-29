package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.compat.lootr.LootrCompat;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ScannerItem extends Item{
    public ScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        var player = context.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        if(CommonConfig.DISABLE_SCANNER_SCANNING.get()){
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.disabled"), true);
            return InteractionResult.FAIL;
        }

        var pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BaseContainerBlockEntity)) {
            return InteractionResult.FAIL;
        }

        BlockState state = blockEntity.getBlockState();
        if (ScanHelper.isScannerBlacklisted(state)) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.blacklisted", state.getBlock().getName()), true);
            return InteractionResult.FAIL;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            return InteractionResult.FAIL;
        }
        server.execute(() -> scan(serverPlayer, pos));

        return InteractionResult.SUCCESS;
    }

    private static void scan(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseContainerBlockEntity)) return;

        Container targetContainer;
        if (LootrCompat.isLootrContainer(blockEntity)) {
            targetContainer = LootrCompat.getPlayerInventory(player, blockEntity);
        } else {
            Container container = (Container) blockEntity;
            if (blockEntity instanceof RandomizableContainerBlockEntity rce) {
                rce.unpackLootTable(player);
            }
            targetContainer = container;
        }

        if (targetContainer == null) return;

        ScanHelper.scanContainer(player, level, pos, targetContainer, false, null);
    }
}
