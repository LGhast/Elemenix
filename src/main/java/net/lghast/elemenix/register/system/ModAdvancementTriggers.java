package net.lghast.elemenix.register.system;

import net.lghast.elemenix.common.system.advancement.ValveTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOffTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOnTrigger;
import net.neoforged.bus.api.IEventBus;

public class ModAdvancementTriggers {
    public static void register(IEventBus eventBus) {
        WaxOnTrigger.TRIGGER_TYPES.register(eventBus);
        WaxOffTrigger.TRIGGER_TYPES.register(eventBus);
        ValveTrigger.TRIGGER_TYPES.register(eventBus);
    }
}
