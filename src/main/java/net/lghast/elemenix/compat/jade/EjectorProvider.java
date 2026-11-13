package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EjectorProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof EjectorBlockEntity ejector) {
            data.putInt("FilterIndex", ejector.getFilterIndex());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("FilterIndex")) {
            int filterIndex = data.getInt("FilterIndex");
            Component filterName;
            if (filterIndex == EjectorBlockEntity.FILTER_ALL) {
                filterName = Component.translatable("status.elemenix.all");
            } else {
                filterName = Component.literal(Elemenix.values()[filterIndex].getName());
            }
            tooltip.add(Component.translatable("status.elemenix.filter", filterName).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ElemenixJadePlugin.UID_EJECTOR_FILTER;
    }
}
