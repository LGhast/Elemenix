package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.compat.lootr.LootrCompat;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.lghast.elemenix.utils.ScanHelper.scanContainer;

public class ScanningStorageItem extends StorageItem{
    public ScanningStorageItem(Properties properties) {
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

        if(CommonConfig.DISABLE_SCANNING_STORAGE_SCANNING.get()){
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.disabled"), true);
            return InteractionResult.FAIL;
        }

        var pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BaseContainerBlockEntity)) {
            return InteractionResult.FAIL;
        }

        BlockState state = blockEntity.getBlockState();
        if (ScanHelper.isStorageBlacklisted(state)) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.blacklisted", state.getBlock().getName()), true);
            return InteractionResult.FAIL;
        }

        Objects.requireNonNull(serverPlayer.getServer()).execute(() -> scan(serverPlayer, pos, context.getItemInHand()));

        return InteractionResult.SUCCESS;
    }

    private static void scan(ServerPlayer player, BlockPos pos, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseContainerBlockEntity bcEntity)) return;

        Container targetContainer;
        boolean isLoot = LootrCompat.isLootrContainer(blockEntity);

        if (isLoot) {
            targetContainer = LootrCompat.getPlayerInventory(player, blockEntity);
            bcEntity.startOpen(player);
        } else {
            Container c = (Container) blockEntity;
            if (blockEntity instanceof RandomizableContainerBlockEntity rc) {
                rc.unpackLootTable(player);
            }
            bcEntity.startOpen(player);
            targetContainer = c;
        }

        if (targetContainer == null) return;

        scanContainer(player, level, pos, targetContainer, true, stack);

        if (bcEntity instanceof ShulkerBoxBlockEntity) {
            bcEntity.stopOpen(player);
        }
    }
}
