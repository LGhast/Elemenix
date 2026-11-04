package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.content.blockentity.ShuntBlockEntity;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ShuntProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ShuntBlockEntity shunt) {
            data.putInt("FilterIndex", shunt.getFilterIndex());
            data.putBoolean("Bound", shunt.isBound());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("FilterIndex")) {
            int filterIndex = data.getInt("FilterIndex");
            Component filterName;
            if (filterIndex == ShuntBlockEntity.FILTER_UNSPECIFIED) {
                filterName = Component.translatable("gui.elemenix.shunt_unspecified");
            } else {
                filterName = Component.literal(Elemenix.values()[filterIndex].getName());
            }
            tooltip.add(Component.translatable("jade.elemenix.shunt_filter", filterName).withStyle(ChatFormatting.GRAY));
        }
        if (data.contains("Bound")) {
            boolean bound = data.getBoolean("Bound");
            MutableComponent boundName = bound
                    ? Component.translatable("jade.elemenix.shunt_group_bound")
                    : Component.translatable("jade.elemenix.shunt_group_unbound");
            tooltip.add(boundName.withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ElemenixJadePlugin.UID_SHUNT_FILTER;
    }
}
