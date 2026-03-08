package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

        scan(serverPlayer, pos, context.getItemInHand());

        return InteractionResult.SUCCESS;
    }

    private static void scan(ServerPlayer player, BlockPos pos, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) return;

        if (!(blockEntity instanceof BaseContainerBlockEntity bcEntity)) {
            return;
        }
        if(blockEntity instanceof RandomizableContainerBlockEntity rcEntity){
            rcEntity.unpackLootTable(player);
        }

        bcEntity.startOpen(player);

        player.awardStat(ModStats.SCANNING_STORAGE_SCANS.get());
        showCommonEffects(level, pos);

        long[] totals = new long[6];
        int sum = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack inventoryStack = container.getItem(slot);
            if (inventoryStack.isEmpty() || ElemenixInfo.isUnanalysable(inventoryStack)) continue;

            if (inventoryStack.is(ModTags.IGNORED_BY_SCANNING)) continue;

            Constituents c = ElemenixInfo.getConstituents(inventoryStack);
            if (c == null) continue;

            int count = inventoryStack.getCount();
            sum += count;

            for (Elemenix type : Elemenix.values()) {
                int idx = type.getIndex();
                long add = (long) (c.get(type) * count * Constituents.DISCOUNT);

                if (totals[idx] > Long.MAX_VALUE - add) {
                    break;
                }
                totals[idx] += add;
            }

            container.setItem(slot, ItemStack.EMPTY);
            container.setChanged();
        }

        boolean isScanEmpty = true;
        for (long val : totals) {
            if (val > 0) {
                isScanEmpty = false;
                break;
            }
        }
        if (isScanEmpty) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanning_storage.empty"), true);
            if(bcEntity instanceof ShulkerBoxBlockEntity) {
                bcEntity.stopOpen(player);
            }
            return;
        }

        ElemenicStorage currentStorage = StorageItem.getOrCreateData(stack);
        long[] newElemenix = currentStorage.elemenix().clone();

        for (Elemenix elemenix : Elemenix.values()) {
            int index = elemenix.getIndex();
            long available = totals[index];

            if (available > 0) {
                long newValue = ModUtils.safeAdd(newElemenix[index], available);
                newElemenix[index] = newValue;
            }
        }

        player.displayClientMessage(Component.translatable("message.elemenix.scanning_storage.total", sum), true);

        stack.set(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(newElemenix));
        showSuccessEffects(level, pos);

        if(bcEntity instanceof ShulkerBoxBlockEntity) {
            bcEntity.stopOpen(player);
        }
    }

    private static void showCommonEffects(ServerLevel level, BlockPos pos){
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.1F, 1.5F);

        if(ClientConfig.SHOW_SCANNING_STORAGE_PARTICLES.get()) {
            ModUtils.spawnParticles(level, ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0.4, 0.2, 0.4, 50, 0.01);
        }
    }

    private static void showSuccessEffects(ServerLevel level, BlockPos pos){
        if(ClientConfig.SHOW_SCANNING_STORAGE_PARTICLES.get()) {
            ModUtils.spawnParticles(level, ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.3, 0.3, 0.3, 20, 0.01);
        }
    }
}
