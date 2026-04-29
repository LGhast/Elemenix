package net.lghast.elemenix.compat.lootr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.MenuBuilder;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;

import javax.annotation.Nullable;

public class LootrCompat {
    public static boolean isLootrLoaded() {
        return ModList.get().isLoaded("lootr");
    }

    public static boolean isLootrContainer(BlockEntity blockEntity) {
        if (!isLootrLoaded()) return false;
        return blockEntity instanceof ILootrInfoProvider;
    }

    @Nullable
    public static ILootrInventory getPlayerInventory(ServerPlayer player, BlockEntity blockEntity) {
        if (!isLootrContainer(blockEntity)) return null;
        ILootrInfoProvider provider = (ILootrInfoProvider) blockEntity;

        return LootrAPI.getInventory(provider, player, (MenuBuilder) null);
    }
}
