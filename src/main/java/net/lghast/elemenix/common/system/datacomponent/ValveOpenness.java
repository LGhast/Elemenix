package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ValveOpenness(int openness) {
    private static boolean isValidOpenness(int value) {
        return value >= 0 && value <= 5;
    }

    public static final Codec<ValveOpenness> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT
                            .validate(value ->
                                    isValidOpenness(value)
                                            ? DataResult.success(value)
                                            : DataResult.error(() -> "Valve style must be between 0 and 5")
                            )
                            .fieldOf("style")
                            .forGetter(ValveOpenness::openness)
            ).apply(instance, ValveOpenness::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ValveOpenness> STREAM_CODEC =
            StreamCodec.of(
                    (buf, valve) -> buf.writeByte(valve.openness()),
                    buf -> {
                        int openness = buf.readByte();
                        if (!isValidOpenness(openness)) {
                            throw new IllegalArgumentException("Invalid valve style: " + openness);
                        }
                        return new ValveOpenness(openness);
                    }
            );

    public ValveOpenness {
        if (!isValidOpenness(openness)) {
            throw new IllegalArgumentException("Valve style must be between 0 and 5, got: " + openness);
        }
    }

    public ValveOpenness decrement() {
        if(this.openness == 0){
            return new ValveOpenness(5);
        }
        return new ValveOpenness(this.openness - 1);
    }

    public String getOpennessPercentage() {
        return switch (this.openness){
            case 0 -> "0%";
            case 1 -> "2%";
            case 2 -> "12.5%";
            case 3 -> "25%";
            case 4 -> "50%";
            default -> "100%";
        };
    }
}
