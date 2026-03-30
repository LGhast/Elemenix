package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.advancement.ScanTrigger;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.*;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

        scan(serverPlayer, pos);

        return InteractionResult.SUCCESS;
    }

    private static void scan(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) return;

        player.awardStat(ModStats.SCANNER_SCANS.get());

        if (blockEntity instanceof RandomizableContainerBlockEntity rce) {
            rce.unpackLootTable(player);
        }

        ScanTrigger.TRIGGER.get().trigger(player);
        showEffects(level, pos);

        long[] totals = new long[6];
        boolean overflow = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || ElemenixInfo.isUnanalysable(stack)) continue;
            if (stack.is(ModTags.IGNORED_BY_SCANNING)) continue;

            Constituents c = ElemenixInfo.getConstituents(stack);
            if (c == null) continue;

            int count = stack.getCount();

            for (Elemenix type : Elemenix.values()) {
                int idx = type.getIndex();
                long add = (long) c.get(type) * count;

                if (totals[idx] > Long.MAX_VALUE - add) {
                    overflow = true;
                    break;
                }
                totals[idx] += add;
            }
            if (overflow) break;
        }

        if (overflow) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.exceed"), true);
            return;
        }

        boolean isScanEmpty = true;
        for (long val : totals) {
            if (val > 0) {
                isScanEmpty = false;
                break;
            }
        }
        if (isScanEmpty) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.empty"), true);
            return;
        }

        player.displayClientMessage(ScanHelper.getTotalComponent(totals), true);
    }

    private static void showEffects(ServerLevel level, BlockPos pos){
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);

        if(ClientConfig.SHOW_SCANNER_PARTICLES.get()) {
            ModUtils.spawnParticles(level, ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0.4, 0.2, 0.4, 50, 0.01);
        }
    }
}
