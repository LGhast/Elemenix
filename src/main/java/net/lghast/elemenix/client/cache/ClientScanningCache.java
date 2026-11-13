package net.lghast.elemenix.client.cache;

import net.minecraft.core.BlockPos;

public class ClientScanningCache {
    private static BlockPos pos;
    private static boolean hasParseable;

    public static void update(BlockPos p, boolean h) {
        pos = p;
        hasParseable = h;
    }

    public static Boolean getHasParseable(BlockPos p) {
        return (pos != null && pos.equals(p)) ? hasParseable : null;
    }
}
