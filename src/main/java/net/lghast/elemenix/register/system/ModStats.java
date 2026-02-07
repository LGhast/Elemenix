package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ModStats {
    public static final DeferredRegister<ResourceLocation> STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, Elemenics.MOD_ID);

    private static final List<Runnable> STAT_SETUP = new ArrayList<>();

    public static final DeferredHolder<ResourceLocation, ResourceLocation> OPEN_ANALYZER =
            makeStat("open_analyzer");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> DECONSTRUCTED_ITEMS =
            makeStat("deconstructed_items");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> RECONSTRUCTED_ITEMS =
            makeStat("reconstructed_items");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEMORIZER_RECORDS_ADDED =
            makeStat("memorizer_records_added");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> STORAGE_TRANSFERS =
            makeStat("storage_transfers");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> REMOTE_STORAGE_TRANSFERS =
            makeStat("remote_storage_transfers");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEMORIZERS_WAXED_ON =
            makeStat("memorizers_waxed_on");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEMORIZERS_WAXED_OFF =
            makeStat("memorizers_waxed_off");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEMORIZERS_STYLED =
            makeStat("memorizers_styled");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEMORY_BURNER_OPENS =
            makeStat("memory_burner_opens");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_MOVE_UP_ACTIONS =
            makeStat("burner_move_up_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_MOVE_DOWN_ACTIONS =
            makeStat("burner_move_down_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_INSERT_BEFORE_ACTIONS =
            makeStat("burner_insert_before_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_SWAP_ACTIONS =
            makeStat("burner_swap_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_DELETE_ACTIONS =
            makeStat("burner_delete_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_CONCATENATE_ACTIONS =
            makeStat("burner_concatenate_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> BURNER_COPY_ACTIONS =
            makeStat("burner_copy_actions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> THROTTLE_VALVE_ADJUSTMENTS =
            makeStat("throttle_valve_adjustments");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> INFUSER_INTERACTIONS =
            makeStat("infuser_interactions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> ENRICHER_INTERACTIONS =
            makeStat("enricher_interactions");

    public static final DeferredHolder<ResourceLocation, ResourceLocation> EJECTOR_INTERACTIONS =
            makeStat("ejector_interactions");

    private static DeferredHolder<ResourceLocation, ResourceLocation> makeStat(String key) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Elemenics.MOD_ID, key);
        STAT_SETUP.add(() -> Stats.CUSTOM.get(resourceLocation, StatFormatter.DEFAULT));
        return STATS.register(key, () -> resourceLocation);
    }

    public static void register(IEventBus eventBus) {
        STATS.register(eventBus);
    }

    public static void init() {
        STAT_SETUP.forEach(Runnable::run);
    }
}
