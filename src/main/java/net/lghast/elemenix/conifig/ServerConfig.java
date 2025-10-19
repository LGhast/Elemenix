package net.lghast.elemenix.conifig;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<List<? extends String>> ELEMENIX_MAPPINGS;

    static {
        BUILDER.push("核心内容 Core Content");

        ELEMENIX_MAPPINGS = BUILDER
                .comment("元质附加映射，用于自定义物品元质成分，可覆盖原有内容",
                        "Elemenix Additional Mappings, used to customize the Elemenic composition of items, allowing overwriting of original content",
                        "元质值顺序为：有机质, 矿化质, 流体质, 金属质, 激发质, 神秘质",
                        "The order of Elemenic values is: Organix, Terrix, Flumix, Metallix, Energix, Arcanix.")
                .defineListAllowEmpty(
                        List.of("elemenix_mappings"),
                        Arrays::asList,
                        null,
                        it -> it instanceof String && ((String) it).matches("#?[a-z_:]+,[0-9]+,[0-9]+,[0-9]+,[0-9]+,[0-9]+,[0-9]+")
                );

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
