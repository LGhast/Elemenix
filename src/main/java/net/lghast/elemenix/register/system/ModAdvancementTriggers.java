package net.lghast.elemenix.register.system;

import net.lghast.elemenix.common.system.advancement.*;
import net.neoforged.bus.api.IEventBus;

public class ModAdvancementTriggers {
    public static void register(IEventBus eventBus) {
        WaxOnTrigger.TRIGGER_TYPES.register(eventBus);
        WaxOffTrigger.TRIGGER_TYPES.register(eventBus);
        ValveTrigger.TRIGGER_TYPES.register(eventBus);
        ScanTrigger.TRIGGER_TYPES.register(eventBus);
        MineralizeTrigger.TRIGGER_TYPES.register(eventBus);
        GerminateTrigger.TRIGGER_TYPES.register(eventBus);
        ImitforgeTrigger.TRIGGER_TYPES.register(eventBus);
    }
}
