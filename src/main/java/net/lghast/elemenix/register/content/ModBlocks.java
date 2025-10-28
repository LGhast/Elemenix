package net.lghast.elemenix.register.content;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.lghast.elemenix.common.content.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ElemenixAnalyzer.MOD_ID);

    public static final DeferredBlock<Block> GEOLOGICAL_SIMULATOR = registerBlock("geological_simulator",
            ()-> new GeologicalSimulatorBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> METALLURGICAL_ACTIVATOR = registerBlock("metallurgical_activator",
            ()-> new MetallurgicalActivatorBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> GERMINAL_ACCELERATOR = registerBlock("germinal_accelerator",
            ()-> new GerminalAcceleratorBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> TRANSPIRING_INCINERATOR = registerBlock("transpiring_incinerator",
            ()-> new TranspiringIncineratorBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> OPTICAL_CAPTURER = registerBlock("optical_capturer",
            ()-> new OpticalCapturerBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> ELEMENIC_INFUSER = registerBlock("elemenic_infuser",
            ()-> new InfuserBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> ELEMENIC_ENRICHER = registerBlock("elemenic_enricher",
            ()-> new EnricherBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> ELEMENIC_EJECTOR = registerBlock("elemenic_ejector",
            ()-> new EjectorBlock(BlockBehaviour.Properties.of()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name,toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name,block);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name,()-> new BlockItem(block.get(),new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
