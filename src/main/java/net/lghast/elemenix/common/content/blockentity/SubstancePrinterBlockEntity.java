package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class SubstancePrinterBlockEntity extends AbstractPrinterBlockEntity {
    public SubstancePrinterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUBSTANCE_PRINTER.get(), pos, state);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.substance_printer");
    }

    @Override
    public Item getMemorizerItem() {
        return ModItems.ELEMENIC_MEMORIZER.get();
    }

    @Override
    protected double getLossMultiplier() {
        return CommonConfig.PRINTER_LOSS.get();
    }

    @Override
    protected int getReconstructionInterval() {
        return CommonConfig.PRINTER_RECONSTRUCTION_INTERVAL.get();
    }

    @Override
    @Nullable
    public List<String> getMemoryKeys() {
        ItemStack memorizer = getItem(MEMORIZER_SLOT);
        if (memorizer.is(ModItems.ELEMENIC_MEMORIZER)) {
            MemoryData memoryData = MemorizerItem.getOrCreateMemories(memorizer);
            List<String> keys = new ArrayList<>();
            for (ResourceLocation id : memoryData.resolvedItems()) {
                keys.add(id.toString());
            }
            return keys;
        }
        return null;
    }

    @Override
    public ItemStack createEntryStack(String key) {
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.get(id));
    }
}
