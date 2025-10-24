package net.lghast.elemenix.conifig;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<List<? extends String>> ELEMENIX_MAPPINGS;
    public static ModConfigSpec.IntValue GS_CONSUMPTION;
    public static ModConfigSpec.IntValue GS_PRODUCTION;
    public static ModConfigSpec.IntValue GS_DC_INTERVAL;
    public static ModConfigSpec.IntValue GS_RC_INTERVAL;

    public static ModConfigSpec.IntValue MA_CONSUMPTION;
    public static ModConfigSpec.IntValue MA_PRODUCTION;
    public static ModConfigSpec.IntValue MA_DC_INTERVAL;
    public static ModConfigSpec.IntValue MA_RC_INTERVAL;

    public static ModConfigSpec.IntValue GA_CONSUMPTION;
    public static ModConfigSpec.IntValue GA_PRODUCTION;
    public static ModConfigSpec.IntValue GA_DC_INTERVAL;
    public static ModConfigSpec.IntValue GA_RC_INTERVAL;

    public static ModConfigSpec.IntValue TI_CONSUMPTION;
    public static ModConfigSpec.IntValue TI_PRODUCTION;
    public static ModConfigSpec.IntValue TI_DC_INTERVAL;
    public static ModConfigSpec.IntValue TI_RC_INTERVAL;

    public static ModConfigSpec.IntValue OC_CONSUMPTION;
    public static ModConfigSpec.IntValue OC_PRODUCTION;
    public static ModConfigSpec.IntValue OC_DC_INTERVAL;
    public static ModConfigSpec.IntValue OC_RC_INTERVAL;

    public static ModConfigSpec.IntValue INFUSER_DC_INTERVAL;
    public static ModConfigSpec.IntValue INFUSION_INTERVAL;
    public static ModConfigSpec.IntValue MAX_INFUSION;

    public static ModConfigSpec.IntValue ENRICHER_DC_INTERVAL;
    public static ModConfigSpec.IntValue ENRICHING_INTERVAL;

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

        BUILDER.push("元质转换仪 Elemenic Transformers");

        GS_CONSUMPTION = BUILDER
                .comment("地质模拟仪每次消耗输入元质的量")
                .comment("Amount of input elemenix consumed per operation by the Geological Simulator")
                .defineInRange("geological_simulator_consumption", 90, 1, Integer.MAX_VALUE);
        GS_PRODUCTION = BUILDER
                .comment("地质模拟仪每次转化产生元质的量")
                .comment("Amount of elemenix produced per transformation by the Geological Simulator")
                .defineInRange("geological_simulator_production", 90, 1, Integer.MAX_VALUE);
        GS_DC_INTERVAL = BUILDER
                .comment("地质模拟仪解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Geological Simulator to deconstruct input items")
                .defineInRange("geological_simulator_dc_interval", 20, 1, Integer.MAX_VALUE);
        GS_RC_INTERVAL = BUILDER
                .comment("地质模拟仪重构输出物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Geological Simulator to reconstruct output items")
                .defineInRange("geological_simulator_rc_interval", 15, 1, Integer.MAX_VALUE);

        MA_CONSUMPTION = BUILDER
                .comment("激化锻冶仪每次消耗输入元质的量")
                .comment("Amount of input elemenix consumed per operation by the Metallurgical Activator")
                .defineInRange("metallurgical_activator_consumption", 90, 1, Integer.MAX_VALUE);
        MA_PRODUCTION = BUILDER
                .comment("激化锻冶仪每次转化产生元质的量")
                .comment("Amount of elemenix produced per transformation by the Metallurgical Activator")
                .defineInRange("metallurgical_activator_production", 90, 1, Integer.MAX_VALUE);
        MA_DC_INTERVAL = BUILDER
                .comment("激化锻冶仪解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Metallurgical Activator to deconstruct input items")
                .defineInRange("metallurgical_activator_dc_interval", 20, 1, Integer.MAX_VALUE);
        MA_RC_INTERVAL = BUILDER
                .comment("激化锻冶仪重构输出物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Metallurgical Activator to reconstruct output items")
                .defineInRange("metallurgical_activator_rc_interval", 15, 1, Integer.MAX_VALUE);

        GA_CONSUMPTION = BUILDER
                .comment("蕃孕加速仪每次消耗输入元质的量")
                .comment("Amount of input elemenix consumed per operation by the Germinal Accelerator")
                .defineInRange("germinal_accelerator_consumption", 90, 1, Integer.MAX_VALUE);
        GA_PRODUCTION = BUILDER
                .comment("蕃孕加速仪每次转化产生元质的量")
                .comment("Amount of elemenix produced per transformation by the Germinal Accelerator")
                .defineInRange("germinal_accelerator_production", 90, 1, Integer.MAX_VALUE);
        GA_DC_INTERVAL = BUILDER
                .comment("蕃孕加速仪解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Germinal Accelerator to deconstruct input items")
                .defineInRange("germinal_accelerator_dc_interval", 20, 1, Integer.MAX_VALUE);
        GA_RC_INTERVAL = BUILDER
                .comment("蕃孕加速仪重构输出物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Germinal Accelerator to reconstruct output items")
                .defineInRange("germinal_accelerator_rc_interval", 15, 1, Integer.MAX_VALUE);

        TI_CONSUMPTION = BUILDER
                .comment("蒸腾焚化仪每次消耗输入元质的量")
                .comment("Amount of input elemenix consumed per operation by the Transpiring Incinerator")
                .defineInRange("transpiring_incinerator_consumption", 90, 1, Integer.MAX_VALUE);
        TI_PRODUCTION = BUILDER
                .comment("蒸腾焚化仪每次转化产生元质的量")
                .comment("Amount of elemenix produced per transformation by the Transpiring Incinerator")
                .defineInRange("transpiring_incinerator_production", 90, 1, Integer.MAX_VALUE);
        TI_DC_INTERVAL = BUILDER
                .comment("蒸腾焚化仪解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Transpiring Incinerator to deconstruct input items")
                .defineInRange("transpiring_incinerator_dc_interval", 20, 1, Integer.MAX_VALUE);
        TI_RC_INTERVAL = BUILDER
                .comment("蒸腾焚化仪重构输出物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Transpiring Incinerator to reconstruct output items")
                .defineInRange("transpiring_incinerator_rc_interval", 15, 1, Integer.MAX_VALUE);

        OC_CONSUMPTION = BUILDER
                .comment("光能捕获仪每次消耗输入元质的量")
                .comment("Amount of input elemenix consumed per operation by the Optical Capturer")
                .defineInRange("optical_capturer_consumption", 90, 1, Integer.MAX_VALUE);
        OC_PRODUCTION = BUILDER
                .comment("光能捕获仪每次转化产生元质的量")
                .comment("Amount of elemenix produced per transformation by the Optical Capturer")
                .defineInRange("optical_capturer_production", 90, 1, Integer.MAX_VALUE);
        OC_DC_INTERVAL = BUILDER
                .comment("光能捕获仪解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Optical Capturer to deconstruct input items")
                .defineInRange("optical_capturer_dc_interval", 25, 1, Integer.MAX_VALUE);
        OC_RC_INTERVAL = BUILDER
                .comment("光能捕获仪重构输出物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Optical Capturer to reconstruct output items")
                .defineInRange("optical_capturer_rc_interval", 20, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("元质解构塔 Elemenic Deconstructors");

        INFUSER_DC_INTERVAL = BUILDER
                .comment("元质注入塔解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Elemenic Infuser to deconstruct input items")
                .defineInRange("infuser_dc_interval", 15, 1, Integer.MAX_VALUE);
        INFUSION_INTERVAL = BUILDER
                .comment("元质注入塔注入元质的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Elemenic Infuser to infuse elemenix")
                .defineInRange("infusion_interval", 15, 1, Integer.MAX_VALUE);
        MAX_INFUSION = BUILDER
                .comment("元质注入塔每次注入元质的最大值")
                .comment("Maximum amount of elemenix infused per operation by the Elemenic Infuser")
                .defineInRange("max_infusion", 7776, 1, Integer.MAX_VALUE);

        ENRICHER_DC_INTERVAL = BUILDER
                .comment("元质富集塔解构输入物品的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Elemenic Enricher to deconstruct input items")
                .defineInRange("enricher_dc_interval", 15, 1, Integer.MAX_VALUE);
        ENRICHING_INTERVAL = BUILDER
                .comment("元质富集塔生产纯质的时间间隔（刻）")
                .comment("Time interval (in ticks) for the Elemenic Enricher to produce essence")
                .defineInRange("enriching_interval", 15, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
