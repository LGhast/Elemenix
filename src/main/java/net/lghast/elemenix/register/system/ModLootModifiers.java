package net.lghast.elemenix.register.system;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.system.loot.WitchTroveLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@SuppressWarnings("unused")
public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRAR =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Elemenics.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<WitchTroveLootModifier>> WITCH_TROVE =
            REGISTRAR.register("witch_trove", () -> WitchTroveLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
