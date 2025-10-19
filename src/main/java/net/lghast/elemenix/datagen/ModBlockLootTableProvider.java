package net.lghast.elemenix.datagen;

import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected  ModBlockLootTableProvider(HolderLookup.Provider registries){
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(),registries);
    }
    @Override
    protected void generate() {
        dropSelf(ModBlocks.GEOLOGICAL_SIMULATOR.get());
        dropSelf(ModBlocks.METALLURGICAL_ACTIVATOR.get());
        dropSelf(ModBlocks.GERMINAL_ACCELERATOR.get());
        dropSelf(ModBlocks.TRANSPIRING_INCINERATOR.get());
        dropSelf(ModBlocks.OPTICAL_CAPTURER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks(){
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                ::iterator;
    }
}
