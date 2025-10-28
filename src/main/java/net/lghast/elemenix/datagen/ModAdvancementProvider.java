package net.lghast.elemenix.datagen;

import net.lghast.elemenix.common.system.advancement.ValveTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOffTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOnTrigger;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

            AdvancementHolder obtainFondantCake = Advancement.Builder.advancement()
                    .parent(obtainMemorizer)
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
                            ItemPredicate.Builder.item().of(ModItems.ELEMENIC_EQUILIBRIUM.get()).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/enrich_equilibrium"), existingFileHelper);

            AdvancementHolder obtainTransformer = Advancement.Builder.advancement()
                    .parent(enrichEssence)
                    .display(
                            ModBlocks.GEOLOGICAL_SIMULATOR,
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

            AdvancementHolder obtainAllTransformers = Advancement.Builder.advancement()
                    .parent(obtainTransformer)
                    .display(
                            ModBlocks.GERMINAL_ACCELERATOR,
                            Component.translatable("advancements.elemenix.obtain_all_transformers.title"),
                            Component.translatable("advancements.elemenix.obtain_all_transformers.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    .addCriterion("get_geological_simulator", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.GEOLOGICAL_SIMULATOR).build()
                    ))
                    .addCriterion("get_metallurgical_activator", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.METALLURGICAL_ACTIVATOR).build()
                    ))
                    .addCriterion("get_germinal_accelerator", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.GERMINAL_ACCELERATOR).build()
                    ))
                    .addCriterion("get_transpiring_incinerator", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.TRANSPIRING_INCINERATOR).build()
                    ))
                    .addCriterion("get_optical_capturer", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(ModBlocks.OPTICAL_CAPTURER).build()
                    ))
                    .save(saver, ResourceLocation.parse("elemenix:elemenics/obtain_all_transformers"), existingFileHelper);


            AdvancementHolder obtainInfuser = Advancement.Builder.advancement()
                    .parent(rootAdvancement)
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
                    .parent(obtainEjector)
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
                    .parent(obtainEjector)
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
        }
    }
}
