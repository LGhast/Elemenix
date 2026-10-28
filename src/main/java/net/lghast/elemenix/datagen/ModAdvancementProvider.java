package net.lghast.elemenix.datagen;

import net.lghast.elemenix.common.system.advancement.*;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, existingFileHelper, List.of(new IdiomAdvancementGenerator()));
    }

    private static class IdiomAdvancementGenerator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                    .display(
                            ModItems.ELEMENIC_ANALYZER,
                            Component.translatable("advancements.elemenix.root.title"),
                            Component.translatable("advancements.elemenix.root.description"),
                            ResourceLocation.parse("textures/block/blast_furnace_top.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_elemenic_analyzer", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.ELEMENIC_ANALYZER.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/root"), existingFileHelper);

            AdvancementHolder obtainMemorizer = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModItems.ELEMENIC_MEMORIZER,
                            Component.translatable("advancements.elemenix.obtain_memorizer.title"),
                            Component.translatable("advancements.elemenix.obtain_memorizer.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_elemenic_memorizer", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.ELEMENIC_MEMORIZER.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_memorizer"), existingFileHelper);

            AdvancementHolder enrichEssence = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModItems.ENERGIX_ESSENCE,
                            Component.translatable("advancements.elemenix.enrich_essence.title"),
                            Component.translatable("advancements.elemenix.enrich_essence.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_essence", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModTags.ESSENCES).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/enrich_essence"), existingFileHelper);

            AdvancementHolder enrichEquilibrium = Advancement.Builder.advancement()
                    .parent(enrichEssence)
                    .display(
                            ModItems.ELEMENIC_EQUILIBRIUM,
                            Component.translatable("advancements.elemenix.enrich_equilibrium.title"),
                            Component.translatable("advancements.elemenix.enrich_equilibrium.description"),
                            null,
                            AdvancementType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_equilibrium", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModTags.EQUILIBRIUM).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/enrich_equilibrium"), existingFileHelper);

            AdvancementHolder obtainTransformer = Advancement.Builder.advancement()
                    .parent(enrichEssence)
                    .display(
                            ModItems.TRANSFORMING_CHIP,
                            Component.translatable("advancements.elemenix.obtain_transformer.title"),
                            Component.translatable("advancements.elemenix.obtain_transformer.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_transformer", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModTags.ELEMENIC_TRANSFORMERS).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_transformer"), existingFileHelper);

            AdvancementHolder obtainTank = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModBlocks.ELEMENIC_TANK,
                            Component.translatable("advancements.elemenix.obtain_tank.title"),
                            Component.translatable("advancements.elemenix.obtain_tank.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_tank", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_TANK).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_tank"), existingFileHelper);

            AdvancementHolder obtainDeconstructor = Advancement.Builder.advancement()
                    .parent(obtainTank)
                    .display(
                            ModBlocks.ELEMENIC_DECONSTRUCTOR,
                            Component.translatable("advancements.elemenix.obtain_deconstructor.title"),
                            Component.translatable("advancements.elemenix.obtain_deconstructor.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_deconstructor", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_DECONSTRUCTOR).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_deconstructor"), existingFileHelper);

            AdvancementHolder obtainInfuser = Advancement.Builder.advancement()
                    .parent(obtainDeconstructor)
                    .display(
                            ModBlocks.ELEMENIC_INFUSER,
                            Component.translatable("advancements.elemenix.obtain_infuser.title"),
                            Component.translatable("advancements.elemenix.obtain_infuser.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_infuser", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_INFUSER).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_infuser"), existingFileHelper);

            AdvancementHolder obtainEnricher = Advancement.Builder.advancement()
                    .parent(obtainInfuser)
                    .display(
                            ModBlocks.ELEMENIC_ENRICHER,
                            Component.translatable("advancements.elemenix.obtain_enricher.title"),
                            Component.translatable("advancements.elemenix.obtain_enricher.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_enricher", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_ENRICHER).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_enricher"), existingFileHelper);

            AdvancementHolder obtainEjector = Advancement.Builder.advancement()
                    .parent(obtainEnricher)
                    .display(
                            ModBlocks.ELEMENIC_EJECTOR,
                            Component.translatable("advancements.elemenix.obtain_ejector.title"),
                            Component.translatable("advancements.elemenix.obtain_ejector.description"),
                            null,
                            AdvancementType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_ejector", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_EJECTOR).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_ejector"), existingFileHelper);

            AdvancementHolder obtainStraightener = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModItems.FLOW_STRAIGHTENER,
                            Component.translatable("advancements.elemenix.obtain_flow_straightener.title"),
                            Component.translatable("advancements.elemenix.obtain_flow_straightener.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_straightener", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.FLOW_STRAIGHTENER).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_flow_straightener"), existingFileHelper);

            AdvancementHolder waxOnMemorizer = Advancement.Builder.advancement()
                    .parent(obtainMemorizer)
                    .display(
                            Items.HONEYCOMB,
                            Component.translatable("advancements.elemenix.wax_on_memorizer.title"),
                            Component.translatable("advancements.elemenix.wax_on_memorizer.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("wax_on", WaxOnTrigger.waxOn())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/wax_on_memorizer"), existingFileHelper);

            AdvancementHolder waxOffMemorizer = Advancement.Builder.advancement()
                    .parent(waxOnMemorizer)
                    .display(
                            Items.NETHERITE_AXE,
                            Component.translatable("advancements.elemenix.wax_off_memorizer.title"),
                            Component.translatable("advancements.elemenix.wax_off_memorizer.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("wax_off", WaxOffTrigger.waxOff())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/wax_off_memorizer"), existingFileHelper);

            AdvancementHolder closeValve = Advancement.Builder.advancement()
                    .parent(obtainStraightener)
                    .display(
                            ModItems.THROTTLE_VALVE,
                            Component.translatable("advancements.elemenix.close_throttle_valve.title"),
                            Component.translatable("advancements.elemenix.close_throttle_valve.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("close_valve", ValveTrigger.valveOpen(0))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/close_throttle_valve"), existingFileHelper);

            AdvancementHolder novicePlumber = Advancement.Builder.advancement()
                    .parent(closeValve)
                    .display(
                            ModItems.WRENCH,
                            Component.translatable("advancements.elemenix.novice_plumber.title"),
                            Component.translatable("advancements.elemenix.novice_plumber.description"),
                            null,
                            AdvancementType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_equipment", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_CHANNEL.get()).build(),
                            ItemPredicate.Builder.item().of(ModBlocks.ELEMENIC_OUTLET.get()).build(),
                            ItemPredicate.Builder.item().of(ModItems.WRENCH.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/novice_plumber"), existingFileHelper);

            AdvancementHolder obtainBurner = Advancement.Builder.advancement()
                    .parent(obtainMemorizer)
                    .display(
                            ModItems.MEMORY_BURNER,
                            Component.translatable("advancements.elemenix.obtain_memory_burner.title"),
                            Component.translatable("advancements.elemenix.obtain_memory_burner.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_burner", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.MEMORY_BURNER).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_burner"), existingFileHelper);

            AdvancementHolder obtainFondantCake = Advancement.Builder.advancement()
                    .parent(obtainBurner)
                    .display(
                            ModItems.FONDANT_CAKE,
                            Component.translatable("advancements.elemenix.obtain_fondant_cake.title"),
                            Component.translatable("advancements.elemenix.obtain_fondant_cake.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            true
                    )
                    .addCriterion("get_fondant_cake", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.FONDANT_CAKE.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_fondant_cake"), existingFileHelper);

            AdvancementHolder obtainStorage = Advancement.Builder.advancement()
                    .parent(obtainInfuser)
                    .display(
                            ModItems.ELEMENIC_STORAGE,
                            Component.translatable("advancements.elemenix.obtain_storage.title"),
                            Component.translatable("advancements.elemenix.obtain_storage.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_storage", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.ELEMENIC_STORAGE.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_storage"), existingFileHelper);

            AdvancementHolder obtainRemoteStorage = Advancement.Builder.advancement()
                    .parent(obtainStorage)
                    .display(
                            ModItems.REMOTE_ELEMENIC_STORAGE,
                            Component.translatable("advancements.elemenix.obtain_remote_storage.title"),
                            Component.translatable("advancements.elemenix.obtain_remote_storage.description"),
                            null,
                            AdvancementType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_remote_storage", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.REMOTE_ELEMENIC_STORAGE.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_remote_storage"), existingFileHelper);

            AdvancementHolder scanContainer = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModItems.ELEMENIC_SCANNER,
                            Component.translatable("advancements.elemenix.scan_container.title"),
                            Component.translatable("advancements.elemenix.scan_container.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("scan_container", ScanTrigger.scan())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/scan_container"), existingFileHelper);

            AdvancementHolder obtainScanningStorage = Advancement.Builder.advancement()
                    .parent(scanContainer)
                    .display(
                            ModItems.SCANNING_STORAGE,
                            Component.translatable("advancements.elemenix.obtain_scanning_storage.title"),
                            Component.translatable("advancements.elemenix.obtain_scanning_storage.description"),
                            null,
                            AdvancementType.GOAL,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_scanning_storage", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.SCANNING_STORAGE.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_scanning_storage"), existingFileHelper);

            AdvancementHolder obtainAnalysisTerminal = Advancement.Builder.advancement()
                    .parent(enrichEquilibrium)
                    .display(
                            ModItems.ANALYSIS_TERMINAL,
                            Component.translatable("advancements.elemenix.obtain_analysis_terminal.title"),
                            Component.translatable("advancements.elemenix.obtain_analysis_terminal.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_analysis_terminal", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.ANALYSIS_TERMINAL.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_analysis_terminal"), existingFileHelper);

            AdvancementHolder mineralize = Advancement.Builder.advancement()
                    .parent(obtainTransformer)
                    .display(
                            ModBlocks.GEOLOGICAL_SIMULATOR,
                            Component.translatable("advancements.elemenix.mineralize.title"),
                            Component.translatable("advancements.elemenix.mineralize.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("mineralize", MineralizeTrigger.mineralize())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/mineralize"), existingFileHelper);

            AdvancementHolder germinate = Advancement.Builder.advancement()
                    .parent(obtainTransformer)
                    .display(
                            ModBlocks.GERMINAL_ACCELERATOR,
                            Component.translatable("advancements.elemenix.germinate.title"),
                            Component.translatable("advancements.elemenix.germinate.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("germinate", GerminateTrigger.germinate())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/germinate"), existingFileHelper);

            AdvancementHolder imitforge = Advancement.Builder.advancement()
                    .parent(obtainTransformer)
                    .display(
                            ModBlocks.METALLURGICAL_ACTIVATOR,
                            Component.translatable("advancements.elemenix.imitforge.title"),
                            Component.translatable("advancements.elemenix.imitforge.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("imitforge", ImitforgeTrigger.imitforge())
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/imitforge"), existingFileHelper);

            AdvancementHolder obtainWitchcraftTrove = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            ModItems.WITCHCRAFT_TROVE,
                            Component.translatable("advancements.elemenix.obtain_witchcraft_trove.title"),
                            Component.translatable("advancements.elemenix.obtain_witchcraft_trove.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_witchcraft_trove", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.WITCHCRAFT_TROVE.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_witchcraft_trove"), existingFileHelper);

            AdvancementHolder obtainWitchcraftMemorizer = Advancement.Builder.advancement()
                    .parent(obtainWitchcraftTrove)
                    .display(
                            ModItems.WITCHCRAFT_MEMORIZER,
                            Component.translatable("advancements.elemenix.obtain_witchcraft_memorizer.title"),
                            Component.translatable("advancements.elemenix.obtain_witchcraft_memorizer.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_witchcraft_memorizer", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModItems.WITCHCRAFT_MEMORIZER.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_witchcraft_memorizer"), existingFileHelper);
        }
    }
}
