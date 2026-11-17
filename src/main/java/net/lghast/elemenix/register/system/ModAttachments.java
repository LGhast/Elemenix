package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Elemenics.MOD_ID);

    public static final Supplier<AttachmentType<RegistrarCollection>> REGISTRAR_COLLECTION = ATTACHMENTS.register(
            "registrar_collection",
            () -> AttachmentType.builder(RegistrarCollection::empty)
                    .serialize(RegistrarCollection.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
