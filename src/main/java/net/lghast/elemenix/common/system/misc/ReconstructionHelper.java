package net.lghast.elemenix.common.system.misc;

import net.lghast.elemenix.common.system.menu.LongContainerData;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.core.Elemenix;
import net.minecraft.world.item.ItemStack;

public class ReconstructionHelper {
    private ReconstructionHelper() {}

    public static int maxCraftable(Constituents required, LongContainerData storage, int limit) {
        if (required == null || required.isUnanalysable() || limit <= 0) {
            return 0;
        }

        int max = limit;
        for (Elemenix type : Elemenix.values()) {
            long req = required.get(type);
            if (req <= 0) continue;

            long available = Math.max(0L, storage.getLong(type.getIndex()));
            max = (int) Math.min(max, available / req);
        }

        return max;
    }

    public static boolean isUnaffordable(Constituents required, LongContainerData storage) {
        return maxCraftable(required, storage, 1) <= 0;
    }

    public static void consume(Constituents required, LongContainerData storage, int amount) {
        if (required == null || amount <= 0) {
            return;
        }

        for (Elemenix type : Elemenix.values()) {
            long req = required.get(type);
            if (req <= 0) continue;

            long remaining = storage.getLong(type.getIndex()) - req * amount;
            storage.setLong(type.getIndex(), Math.max(0L, remaining));
        }
    }

    public static boolean isRepellent(ItemStack carried, ItemStack result) {
        return !carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, result);
    }

    public static int clampToCarriedRoom(ItemStack carried, int maxStackSize, int amount) {
        if (carried.isEmpty()) {
            return amount;
        }

        int room = maxStackSize - carried.getCount();
        if (room <= 0) {
            return 0;
        }
        return Math.min(amount, room);
    }
}
