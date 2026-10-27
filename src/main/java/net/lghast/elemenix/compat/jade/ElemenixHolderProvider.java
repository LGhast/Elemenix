package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ElemenixHolderProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ElemenixHolder holder) {
            long[] elemenix = new long[6];
            for (Elemenix type : Elemenix.values()) {
                elemenix[type.getIndex()] = holder.getStored(type);
            }
            data.putLongArray("ElemenixStorage", elemenix);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("ElemenixStorage", CompoundTag.TAG_LONG_ARRAY)) {
            ElemenicStorage storage = new ElemenicStorage(data.getLongArray("ElemenixStorage"));
            if (!storage.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.elemenix.analyzer_storage").withStyle(ChatFormatting.GRAY));
                tooltip.add(storage.toComponentFormer());
                tooltip.add(storage.toComponentLatter());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ElemenixJadePlugin.UID_ELEMENIX_STORAGE;
    }
}
