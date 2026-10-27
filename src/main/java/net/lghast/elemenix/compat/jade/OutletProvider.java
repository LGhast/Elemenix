package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.content.blockentity.OutletBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum OutletProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof OutletBlockEntity outlet) {
            data.putInt("OutputPercent", outlet.getDataAccess().get(0));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("OutputPercent")) {
            int percent = data.getInt("OutputPercent");
            tooltip.add(Component.translatable("tooltip.elemenix.valve_opening", percent + "%").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ElemenixJadePlugin.UID_OUTLET;
    }
}
