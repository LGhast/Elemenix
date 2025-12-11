package net.lghast.elemenix.common.system.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Style(int style) {
    private static final int STYLE_COUNT = 21;

    private static boolean isValidStyle(int value) {
        return value >= 0 && value <= STYLE_COUNT;
    }

    public static final Codec<Style> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT
                            .validate(value ->
                                    isValidStyle(value)
                                            ? DataResult.success(value)
                                            : DataResult.error(() -> "The style index must be between 0 and " + STYLE_COUNT)
                            )
                            .fieldOf("style")
                            .forGetter(Style::style)
            ).apply(instance, Style::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Style> STREAM_CODEC =
            StreamCodec.of(
                    (buf, valve) -> buf.writeByte(valve.style()),
                    buf -> {
                        int style = buf.readByte();
                        if (!isValidStyle(style)) {
                            throw new IllegalArgumentException("Invalid style index: " + style);
                        }
                        return new Style(style);
                    }
            );

    public Style {
        if (!isValidStyle(style)) {
            throw new IllegalArgumentException("Style index must be between 0 and " + STYLE_COUNT +", got: " + style);
        }
    }
    public String getStyleKey() {
       String keyEnd = switch (this.style){
            case 1 -> "white";
            case 2 -> "red";
            case 3 -> "orange";
            case 4 -> "yellow";
            case 5 -> "green";
            case 6 -> "cyan";
            case 7 -> "blue";
            case 8 -> "purple";
            case 9 -> "black";
            case 10 -> "gray";
            case 11 -> "lime";
            case 12 -> "pink";
            case 13 -> "magenta";
            case 14 -> "light_gray";
            case 15 -> "light_blue";
            case 16 -> "brown";
            case 17 -> "prismarine";
            case 18 -> "breeze";
            case 19 -> "purpur";
            case 20 -> "golden";
            case 21 -> "diamond";
            default -> "";
        };
       return "item.elemenix.elemenic_memorizer.style." + keyEnd;
    }
}
