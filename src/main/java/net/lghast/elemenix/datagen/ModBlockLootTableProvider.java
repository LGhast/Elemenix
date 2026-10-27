package net.lghast.elemenix.datagen;

import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

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
        dropSelf(ModBlocks.ELEMENIC_INFUSER.get());
        dropSelf(ModBlocks.ELEMENIC_ENRICHER.get());
        dropSelf(ModBlocks.ELEMENIC_EJECTOR.get());
        dropSelf(ModBlocks.ELEMENIC_OUTLET.get());
        dropSelf(ModBlocks.ELEMENIC_CHANNEL.get());
        dropSelf(ModBlocks.ELEMENIC_DECONSTRUCTOR.get());
        add(ModBlocks.ELEMENIC_TANK.get(), LootTable.lootTable().setParamSet(LootContextParamSets.BLOCK));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks(){
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                ::iterator;
    }
}
