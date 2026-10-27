package net.lghast.elemenix.utils.misc;

import net.lghast.elemenix.common.content.block.TankBlock;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Reads and writes stored elemenix on "storage-like" items: the dedicated
 * storage items ({@code ElemenicStorage} data component) and the elemenic tank
 * block item (block entity data component).
 */
public final class ElemenixItemStorage {
    private ElemenixItemStorage() {
    }

    public static boolean isTank(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TankBlock;
    }

    public static long[] getStored(ItemStack stack) {
        if (isTank(stack)) {
            return TankBlock.getStoredElemenix(stack);
        }
        return StorageItem.getOrCreateData(stack).elemenix();
    }

    public static void setStored(ItemStack stack, long[] elemenix) {
        if (isTank(stack)) {
            TankBlock.setStoredElemenix(stack, elemenix);
        } else {
            stack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(elemenix));
        }
    }

    public static boolean isEmpty(ItemStack stack) {
        for (long value : getStored(stack)) {
            if (value != 0L) {
                return false;
            }
        }
        return true;
    }
}
