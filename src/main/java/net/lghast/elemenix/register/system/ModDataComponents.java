package net.lghast.elemenix.register.system;

import com.mojang.serialization.Codec;
import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.system.datacomponent.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Elemenics.MOD_ID);

    public static final Codec<ElemenicStorage> ELEMENIC_STORAGE_CODEC = ElemenicStorage.CODEC;

    public static final Codec<MemoryData> MEMORY_DATA_CODEC = MemoryData.CODEC;

    public static final Codec<Waxed> WAXED_CODEC = Waxed.CODEC;

    public static final Codec<UuidData> ANALYZER_UUID_CODEC = UuidData.CODEC;

    public static final Codec<UuidData> BOX_UUID_CODEC = UuidData.CODEC;

    public static final Codec<ValveOpenness> VALVE_OPENNESS_CODEC = ValveOpenness.CODEC;

    public static final Codec<Style> STYLE_CODEC = Style.CODEC;

    public static final Codec<RemoteStorageBinding> REMOTE_STORAGE_BINDING_CODEC = RemoteStorageBinding.CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, ElemenicStorage> ELEMENIC_STORAGE_STREAM_CODEC =
            StreamCodec.of(
                    (buf, storage) -> {
                        long[] elemenix = storage.elemenix();
                        for (int i = 0; i < 6; i++) {
                            buf.writeLong(elemenix[i]);
                        }
                    },
                    buf -> {
                        long[] array = new long[6];
                        for (int i = 0; i < 6; i++) {
                            array[i] = buf.readLong();
                        }
                        return new ElemenicStorage(array);
                    }
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, MemoryData> MEMORY_DATA_STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    MemoryData::resolvedItems,
                    MemoryData::new
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, Waxed> WAXED_STREAM_CODEC =
            StreamCodec.of(
                    (buf, waxed) -> buf.writeBoolean(waxed.waxed()),
                    buf -> new Waxed(buf.readBoolean())
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ValveOpenness> VALVE_OPENNESS_STREAM_CODEC =
            ValveOpenness.STREAM_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, Style> STYLE_STREAM_CODEC =
            Style.STREAM_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteStorageBinding> REMOTE_STORAGE_BINDING_STREAM_CODEC =
            RemoteStorageBinding.STREAM_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, UuidData> ANALYZER_UUID_STREAM_CODEC =
            UuidData.STREAM_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, UuidData> BOX_UUID_STREAM_CODEC =
            UuidData.STREAM_CODEC;

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ElemenicStorage>> ELEMENIC_STORAGE =
            REGISTRAR.registerComponentType("elemenic_storage", builder ->
                    builder.persistent(ELEMENIC_STORAGE_CODEC)
                            .networkSynchronized(ELEMENIC_STORAGE_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MemoryData>> MEMORY_DATA =
            REGISTRAR.registerComponentType("memory_data", builder ->
                    builder.persistent(MEMORY_DATA_CODEC)
                            .networkSynchronized(MEMORY_DATA_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Waxed>> WAXED =
            REGISTRAR.registerComponentType("waxed", builder ->
                    builder.persistent(WAXED_CODEC)
                            .networkSynchronized(WAXED_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UuidData>> ANALYZER_UUID =
            REGISTRAR.registerComponentType("analyzer_uuid", builder ->
                    builder.persistent(ANALYZER_UUID_CODEC)
                            .networkSynchronized(ANALYZER_UUID_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UuidData>> BOX_UUID =
            REGISTRAR.registerComponentType("box_uuid", builder ->
                    builder.persistent(BOX_UUID_CODEC)
                            .networkSynchronized(BOX_UUID_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ValveOpenness>> VALVE_OPENNESS =
            REGISTRAR.registerComponentType("valve_openness", builder ->
                    builder.persistent(VALVE_OPENNESS_CODEC)
                            .networkSynchronized(VALVE_OPENNESS_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Style>> STYLE =
            REGISTRAR.registerComponentType("style", builder ->
                    builder.persistent(STYLE_CODEC)
                            .networkSynchronized(STYLE_STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteStorageBinding>> REMOTE_STORAGE_BINDING =
            REGISTRAR.registerComponentType("remote_storage_binding", builder ->
                    builder.persistent(REMOTE_STORAGE_BINDING_CODEC)
                            .networkSynchronized(REMOTE_STORAGE_BINDING_STREAM_CODEC)
            );

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}