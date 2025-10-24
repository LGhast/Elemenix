package net.lghast.elemenix.register.system;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ElemenixAnalyzer.MOD_ID);

    public static final Supplier<CreativeModeTab> ELEMENIX_TAB = CREATIVE_MODE_TAB.register("elemenix_tab",
            ()-> CreativeModeTab.builder().icon(()->new ItemStack(ModItems.ELEMENIC_ANALYZER.get()))
                    .title(Component.translatable("creativetab.elemenix.elemenix"))
                    .displayItems((itemDisplayParameters,output)->{
                        output.accept(ModItems.ELEMENIC_ANALYZER);
                        output.accept(ModItems.ELEMENIC_MEMORIZER);
                        output.accept(ModItems.FONDANT_CAKE);
                        output.accept(ModItems.ORGANIX_ESSENCE);
                        output.accept(ModItems.TERRIX_ESSENCE);
                        output.accept(ModItems.FLUMIX_ESSENCE);
                        output.accept(ModItems.METALLIX_ESSENCE);
                        output.accept(ModItems.ENERGIX_ESSENCE);
                        output.accept(ModItems.ARCANIX_ESSENCE);
                        output.accept(ModItems.ELEMENIC_EQUILIBRIUM);
                        output.accept(ModItems.ORGANIX_ESSENPLEX);
                        output.accept(ModItems.TERRIX_ESSENPLEX);
                        output.accept(ModItems.FLUMIX_ESSENPLEX);
                        output.accept(ModItems.METALLIX_ESSENPLEX);
                        output.accept(ModItems.ENERGIX_ESSENPLEX);
                        output.accept(ModItems.ARCANIX_ESSENPLEX);
                        output.accept(ModBlocks.GEOLOGICAL_SIMULATOR);
                        output.accept(ModBlocks.METALLURGICAL_ACTIVATOR);
                        output.accept(ModBlocks.GERMINAL_ACCELERATOR);
                        output.accept(ModBlocks.TRANSPIRING_INCINERATOR);
                        output.accept(ModBlocks.OPTICAL_CAPTURER);
                        output.accept(ModBlocks.ELEMENIC_INFUSER);
                        output.accept(ModBlocks.ELEMENIC_ENRICHER);
                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
