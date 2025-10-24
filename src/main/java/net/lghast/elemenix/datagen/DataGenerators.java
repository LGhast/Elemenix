package net.lghast.elemenix.datagen;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator=event.getGenerator();
        PackOutput packOutput=generator.getPackOutput();
        ExistingFileHelper existingFileHelper=event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider=event.getLookupProvider();

        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)),lookupProvider));

        BlockTagsProvider blockTagsProvider = new ModBlockTagProvider(packOutput,lookupProvider,existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        ItemTagsProvider itemTagsProvider = new ModItemTagProvider(packOutput,lookupProvider,blockTagsProvider.contentsGetter(),existingFileHelper);
        generator.addProvider(event.includeServer(), itemTagsProvider);

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput,existingFileHelper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput,existingFileHelper));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput,lookupProvider));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(packOutput, lookupProvider, existingFileHelper));
    }
}
