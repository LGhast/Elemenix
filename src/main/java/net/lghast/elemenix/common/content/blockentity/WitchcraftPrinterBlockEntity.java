package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class WitchcraftPrinterBlockEntity extends AbstractPrinterBlockEntity {
    private static final char KEY_SEPARATOR = '|';

    public WitchcraftPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCHCRAFT_PRINTER.get(), pos, state);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.witchcraft_printer");
    }

    @Override
    public Item getMemorizerItem() {
        return ModItems.WITCHCRAFT_MEMORIZER.get();
    }

    @Override
    protected double getLossMultiplier() {
        return CommonConfig.WITCHCRAFT_PRINTER_LOSS.get();
    }

    @Override
    protected int getReconstructionInterval() {
        return CommonConfig.WITCHCRAFT_PRINTER_RECONSTRUCTION_INTERVAL.get();
    }

    @Override
    @Nullable
    public List<String> getMemoryKeys() {
        ItemStack memorizer = getItem(MEMORIZER_SLOT);
        if (memorizer.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            List<PotionMemoryData.Entry> entries = WitchcraftMemorizerItem.getOrCreateMemories(memorizer).entries();
            List<String> keys = new ArrayList<>();
            for (PotionMemoryData.Entry entry : entries) {
                keys.add(entry.itemId() + String.valueOf(KEY_SEPARATOR) + entry.potionId());
            }
            return keys;
        }
        return null;
    }

    @Override
    public ItemStack createEntryStack(String key) {
        int separator = key.indexOf(KEY_SEPARATOR);
        if (separator <= 0 || separator >= key.length() - 1) return ItemStack.EMPTY;

        ResourceLocation itemId = ResourceLocation.tryParse(key.substring(0, separator));
        ResourceLocation potionId = ResourceLocation.tryParse(key.substring(separator + 1));
        if (itemId == null || potionId == null) return ItemStack.EMPTY;

        Item item = BuiltInRegistries.ITEM.get(itemId);
        Optional<Holder.Reference<Potion>> potionHolder =
                BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, potionId));
        return potionHolder.map(potionReference -> PotionContents.createItemStack(item, potionReference)).orElse(ItemStack.EMPTY);
    }
}
