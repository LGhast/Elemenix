package net.lghast.elemenix.utils;

import net.lghast.elemenix.conifig.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

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
}
