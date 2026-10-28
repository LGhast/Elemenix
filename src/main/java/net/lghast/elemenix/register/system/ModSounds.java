package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Elemenics.MOD_ID);

    public static final Supplier<SoundEvent> SCANNING = register("scanning");
    public static final Supplier<SoundEvent> WRENCH = register("wrench");
    public static final Supplier<SoundEvent> WRENCH_RECORD = register("wrench_record");

    private static Supplier<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Elemenics.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
