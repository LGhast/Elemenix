package net.lghast.elemenix.register.content;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.common.content.blockentity.EnricherBlockEntity;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ElemenixAnalyzer.MOD_ID);

    public static final Supplier<BlockEntityType<TransformerBlockEntity>> TRANSFORMER = BLOCK_ENTITY_TYPES.register(
            "elemenic_transformer",
            () -> BlockEntityType.Builder.of(
                            TransformerBlockEntity::new,
                            ModBlocks.GEOLOGICAL_SIMULATOR.get(),
                            ModBlocks.METALLURGICAL_ACTIVATOR.get(),
                            ModBlocks.GERMINAL_ACCELERATOR.get(),
                            ModBlocks.TRANSPIRING_INCINERATOR.get(),
                            ModBlocks.OPTICAL_CAPTURER.get()
                    )
                    .build(null)
    );

    public static final Supplier<BlockEntityType<InfuserBlockEntity>> INFUSER = BLOCK_ENTITY_TYPES.register(
            "elemenic_infuser",
            () -> BlockEntityType.Builder.of(
                            InfuserBlockEntity::new,
                            ModBlocks.ELEMENIC_INFUSER.get()
                    )
                    .build(null)
    );

    public static final Supplier<BlockEntityType<EnricherBlockEntity>> ENRICHER = BLOCK_ENTITY_TYPES.register(
            "elemenic_enricher",
            () -> BlockEntityType.Builder.of(
                            EnricherBlockEntity::new,
                            ModBlocks.ELEMENIC_ENRICHER.get()
                    )
                    .build(null)
    );

    public static final Supplier<BlockEntityType<EjectorBlockEntity>> EJECTOR = BLOCK_ENTITY_TYPES.register(
            "elemenic_ejector",
            () -> BlockEntityType.Builder.of(
                            EjectorBlockEntity::new,
                            ModBlocks.ELEMENIC_EJECTOR.get()
                    )
                    .build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
