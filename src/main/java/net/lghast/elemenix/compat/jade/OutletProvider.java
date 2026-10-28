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

            OutletBlockEntity.OutletPath path = outlet.getPath();
            data.putBoolean("PathValid", path.valid());
            if (path.valid() && path.target() != null && path.source() != null) {
                data.putString("SourceId", path.source().getDescriptionId());
                data.putString("TargetId", path.target().getDescriptionId());
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (data.contains("PathValid")) {
            if (data.getBoolean("PathValid")) {
                Component sourceName = Component.translatable(data.getString("SourceId"));
                Component targetName = Component.translatable(data.getString("TargetId"));
                tooltip.add(Component.translatable("gui.elemenix.outlet_path", sourceName, targetName).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("gui.elemenix.outlet_path_invalid").withStyle(ChatFormatting.RED));
            }
        }

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
