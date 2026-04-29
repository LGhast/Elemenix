package net.lghast.elemenix.utils;

import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.advancement.ScanTrigger;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanHelper {
    private static final Set<ResourceLocation> SCANNER_BLACKLIST = new HashSet<>();
    private static final Set<ResourceLocation> STORAGE_BLACKLIST = new HashSet<>();

    public static void loadFromConfig() {
        SCANNER_BLACKLIST.clear();
        STORAGE_BLACKLIST.clear();

        loadList(CommonConfig.SCANNER_BLACKLIST.get(), SCANNER_BLACKLIST);
        loadList(CommonConfig.SCANNING_STORAGE_BLACKLIST.get(), STORAGE_BLACKLIST);
    }

    private static void loadList(List<? extends String> configList, Set<ResourceLocation> targetSet) {
        if (configList == null) return;
        for (String entry : configList) {
            if (entry == null || entry.trim().isEmpty()) continue;
            try {
                ResourceLocation id = ResourceLocation.parse(entry.trim());
                targetSet.add(id);
            } catch (Exception ignored) {}
        }
    }

    public static boolean isScannerBlacklisted(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return SCANNER_BLACKLIST.contains(id);
    }

    public static boolean isStorageBlacklisted(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return STORAGE_BLACKLIST.contains(id);
    }

    public static Component getTotalComponent(long[] totals){
        Component organix = Component.literal(ModUtils.formatNumber(totals[0], "O:%s "))
                .withColor(Elemenix.ORGANIX.getColor());
        Component terrix  = Component.literal(ModUtils.formatNumber(totals[1], "T:%s "))
                .withColor(Elemenix.TERRIX.getColor());
        Component flumix  = Component.literal(ModUtils.formatNumber(totals[2], "F:%s"))
                .withColor(Elemenix.FLUMIX.getColor());
        Component metallix= Component.literal(ModUtils.formatNumber(totals[3], " M:%s "))
                .withColor(Elemenix.METALLIX.getColor());
        Component energix = Component.literal(ModUtils.formatNumber(totals[4], "E:%s "))
                .withColor(Elemenix.ENERGIX.getColor());
        Component arcanix = Component.literal(ModUtils.formatNumber(totals[5], "A:%s"))
                .withColor(Elemenix.ARCANIX.getColor());

        return Component.translatable("message.elemenix.scanner.total")
                .append(organix).append(terrix).append(flumix).append(metallix).append(energix).append(arcanix);
    }

    private static void showScanningEffects(ServerLevel level, BlockPos pos){
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.1F, 1.5F);

        if(CommonConfig.SHOW_SCANNING_STORAGE_PARTICLES.get()) {
            ModUtils.spawnParticles(level, ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0.4, 0.2, 0.4, 50, 0.01);
        }
    }

    private static void showSuccessEffects(ServerLevel level, BlockPos pos){
        if(CommonConfig.SHOW_SCANNING_STORAGE_PARTICLES.get()) {
            ModUtils.spawnParticles(level, ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.3, 0.3, 0.3, 20, 0.01);
        }
    }

    public static void scanContainer(ServerPlayer player, ServerLevel level, BlockPos pos, Container container, boolean consumeItems, @Nullable ItemStack storageStack) {
        if (consumeItems) {
            player.awardStat(ModStats.SCANNING_STORAGE_SCANS.get());
        } else {
            player.awardStat(ModStats.SCANNER_SCANS.get());
        }

        if (!consumeItems) {
            ScanTrigger.TRIGGER.get().trigger(player);
        }

        showScanningEffects(level, pos);

        long[] totals = new long[6];
        int sum = 0;
        boolean overflow = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || ElemenixInfo.isUnanalysable(stack)) continue;
            if (stack.is(ModTags.IGNORED_BY_SCANNING)) continue;

            Constituents c = ElemenixInfo.getConstituents(stack);
            if (c == null) continue;

            int count = stack.getCount();
            sum += count;

            for (Elemenix type : Elemenix.values()) {
                int idx = type.getIndex();
                double multiplier = consumeItems ? Constituents.DISCOUNT : 1.0;
                long add = (long) (c.get(type) * count * multiplier);

                if (totals[idx] > Long.MAX_VALUE - add) {
                    overflow = true;
                    break;
                }
                totals[idx] += add;
            }

            if (consumeItems) {
                container.setItem(slot, ItemStack.EMPTY);
                container.setChanged();
            }

            if (overflow) break;
        }

        if (overflow) {
            player.displayClientMessage(Component.translatable("message.elemenix.scanner.exceed"), true);
            return;
        }

        boolean isScanEmpty = Arrays.stream(totals).allMatch(val -> val == 0);
        if (isScanEmpty) {
            if (consumeItems) {
                player.displayClientMessage(Component.translatable("message.elemenix.scanning_storage.empty"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.elemenix.scanner.empty"), true);
            }
            return;
        }

        if (consumeItems && storageStack != null) {
            ElemenicStorage currentStorage = StorageItem.getOrCreateData(storageStack);
            long[] newElemenix = currentStorage.elemenix().clone();
            for (Elemenix elemenix : Elemenix.values()) {
                int index = elemenix.getIndex();
                long available = totals[index];
                if (available > 0) {
                    long newValue = ModUtils.safeAdd(newElemenix[index], available);
                    newElemenix[index] = newValue;
                }
            }
            storageStack.set(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(newElemenix));
            player.displayClientMessage(Component.translatable("message.elemenix.scanning_storage.total", sum), true);
            showSuccessEffects(level, pos);
        } else {
            player.displayClientMessage(ScanHelper.getTotalComponent(totals), true);
        }
    }
}
