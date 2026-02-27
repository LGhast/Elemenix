package net.lghast.elemenix.conifig;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    private static final String CONFIG_PREFIX = "config.elemenix.";

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<List<? extends String>> ELEMENIX_MAPPINGS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> UNANALYSABLE_LIST;
    public static final ModConfigSpec.DoubleValue DC_DISCOUNT;
    public static final ModConfigSpec.DoubleValue RC_PREMIUM;

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
    public static ModConfigSpec.IntValue ENRICHER_ENRICHING_INTERVAL;

    public static ModConfigSpec.IntValue EJECTOR_DC_INTERVAL;
    public static ModConfigSpec.IntValue EJECTOR_ENRICHING_INTERVAL;
    public static ModConfigSpec.IntValue EJECTING_INTERVAL;

    static {
        BUILDER.push("核心内容 Core Content");

        ELEMENIX_MAPPINGS = BUILDER
                .comment("元质附加映射，用于自定义物品元质成分，可覆盖原有内容。",
                        "若以#开头，表示标签。若一个物品处于多个标签，选择元质总量最大的。",
                        "元质值顺序为：有机质, 矿化质, 流体质, 金属质, 激发质, 神秘质。",
                        "例：\"minecraft:apple,32,0,15,0,0,0\", \"#minecraft:redstone_ores,0,42,0,0,6,0\"",
                        "Elemenix Additional Mappings, used to customize the Elemenic composition of items, allowing overwriting of original content.",
                        "If it starts with #, it indicates an item tag. ",
                        "If an item matches multiple tags, the one with the highest total Elemenic value will be selected.",
                        "The order of Elemenic values is: Organix, Terrix, Flumix, Metallix, Energix, Arcanix.",
                        "Examples: \"minecraft:apple,32,0,15,0,0,0\", \"#minecraft:redstone_ores,0,42,0,0,6,0\"")
                .translation(CONFIG_PREFIX + "elemenix_mappings")
                .defineList("elemenix_mappings",
                        List::of,
                        () -> "",
                        it -> it instanceof String
                );

        UNANALYSABLE_LIST = BUILDER
                .comment("不可解析物品附加表，用于直接设定某物品为不可解析，可覆盖原有内容。",
                        "输入物品ID，若以#开头，表示标签。",
                        "Additional list for unanalysable items, used to directly set an item as unanalysable, overwriting original content.",
                        "Enter item ID, if it starts with #, it indicates a tag.")
                .translation(CONFIG_PREFIX + "unanalysable_list")
                .defineList("unanalysable_list",
                        List::of,
                        () -> "",
                        it -> it instanceof String
                );

        DC_DISCOUNT = BUILDER
                .comment("解构贬值：",
                        "当元质解析器、转化仪、注入塔、富集塔、射流塔解构物品时，",
                        "获得的元质 = 原始元质 × 解构贬值（向上取整）",
                        "Deconstruction Discount:",
                        "When Elemenic Analyzer, Transformer, Infuser, Enricher or Ejector deconstructs items,",
                        "the obtained constituents = original constituents × discount (rounded up).")
                .translation(CONFIG_PREFIX + "deconstruction_discount")
                .defineInRange("deconstruction_discount", 1.0, Double.MIN_VALUE, 1.0);

        RC_PREMIUM = BUILDER
                .comment("重构溢价：",
                        "当元质解析器重构物品时，",
                        "消耗的元质 = 原始元质 × （1 + 重构溢价）（向下取整）",
                        "Reconstruction Premium:",
                        "When elemenic analyzer reconstructs items, ",
                        "consumed constituents = original constituents × (1 + premium) (rounded down).")
                .translation(CONFIG_PREFIX + "reconstruction_premium")
                .defineInRange("reconstruction_premium", 0.0, 0.0, 99.0);

        BUILDER.pop();

        BUILDER.push("元质转换仪 Elemenic Transformers");

        GS_CONSUMPTION = BUILDER
                .comment("地质模拟仪每次消耗输入元质的量",
                        "Amount of input elemenix consumed per operation by the Geological Simulator")
                .translation(CONFIG_PREFIX + "geological_simulator_consumption")
                .defineInRange("geological_simulator_consumption", 90, 1, Integer.MAX_VALUE);

        GS_PRODUCTION = BUILDER
                .comment("地质模拟仪每次转化产生元质的量",
                        "Amount of elemenix produced per transformation by the Geological Simulator")
                .translation(CONFIG_PREFIX + "geological_simulator_production")
                .defineInRange("geological_simulator_production", 90, 1, Integer.MAX_VALUE);

        GS_DC_INTERVAL = BUILDER
                .comment("地质模拟仪解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Geological Simulator to deconstruct input items")
                .translation(CONFIG_PREFIX + "geological_simulator_dc_interval")
                .defineInRange("geological_simulator_dc_interval", 8, 1, Integer.MAX_VALUE);

        GS_RC_INTERVAL = BUILDER
                .comment("地质模拟仪重构输出物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Geological Simulator to reconstruct output items")
                .translation(CONFIG_PREFIX + "geological_simulator_rc_interval")
                .defineInRange("geological_simulator_rc_interval", 15, 1, Integer.MAX_VALUE);

        MA_CONSUMPTION = BUILDER
                .comment("激化锻冶仪每次消耗输入元质的量",
                        "Amount of input elemenix consumed per operation by the Metallurgical Activator")
                .translation(CONFIG_PREFIX + "metallurgical_activator_consumption")
                .defineInRange("metallurgical_activator_consumption", 90, 1, Integer.MAX_VALUE);

        MA_PRODUCTION = BUILDER
                .comment("激化锻冶仪每次转化产生元质的量",
                        "Amount of elemenix produced per transformation by the Metallurgical Activator")
                .translation(CONFIG_PREFIX + "metallurgical_activator_production")
                .defineInRange("metallurgical_activator_production", 90, 1, Integer.MAX_VALUE);

        MA_DC_INTERVAL = BUILDER
                .comment("激化锻冶仪解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Metallurgical Activator to deconstruct input items")
                .translation(CONFIG_PREFIX + "metallurgical_activator_dc_interval")
                .defineInRange("metallurgical_activator_dc_interval", 8, 1, Integer.MAX_VALUE);

        MA_RC_INTERVAL = BUILDER
                .comment("激化锻冶仪重构输出物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Metallurgical Activator to reconstruct output items")
                .translation(CONFIG_PREFIX + "metallurgical_activator_rc_interval")
                .defineInRange("metallurgical_activator_rc_interval", 15, 1, Integer.MAX_VALUE);

        GA_CONSUMPTION = BUILDER
                .comment("蕃孕加速仪每次消耗输入元质的量",
                        "Amount of input elemenix consumed per operation by the Germinal Accelerator")
                .translation(CONFIG_PREFIX + "germinal_accelerator_consumption")
                .defineInRange("germinal_accelerator_consumption", 90, 1, Integer.MAX_VALUE);

        GA_PRODUCTION = BUILDER
                .comment("蕃孕加速仪每次转化产生元质的量",
                        "Amount of elemenix produced per transformation by the Germinal Accelerator")
                .translation(CONFIG_PREFIX + "germinal_accelerator_production")
                .defineInRange("germinal_accelerator_production", 90, 1, Integer.MAX_VALUE);

        GA_DC_INTERVAL = BUILDER
                .comment("蕃孕加速仪解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Germinal Accelerator to deconstruct input items")
                .translation(CONFIG_PREFIX + "germinal_accelerator_dc_interval")
                .defineInRange("germinal_accelerator_dc_interval", 8, 1, Integer.MAX_VALUE);

        GA_RC_INTERVAL = BUILDER
                .comment("蕃孕加速仪重构输出物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Germinal Accelerator to reconstruct output items")
                .translation(CONFIG_PREFIX + "germinal_accelerator_rc_interval")
                .defineInRange("germinal_accelerator_rc_interval", 15, 1, Integer.MAX_VALUE);

        TI_CONSUMPTION = BUILDER
                .comment("蒸腾焚化仪每次消耗输入元质的量",
                        "Amount of input elemenix consumed per operation by the Transpiring Incinerator")
                .translation(CONFIG_PREFIX + "transpiring_incinerator_consumption")
                .defineInRange("transpiring_incinerator_consumption", 90, 1, Integer.MAX_VALUE);

        TI_PRODUCTION = BUILDER
                .comment("蒸腾焚化仪每次转化产生元质的量",
                        "Amount of elemenix produced per transformation by the Transpiring Incinerator")
                .translation(CONFIG_PREFIX + "transpiring_incinerator_production")
                .defineInRange("transpiring_incinerator_production", 90, 1, Integer.MAX_VALUE);

        TI_DC_INTERVAL = BUILDER
                .comment("蒸腾焚化仪解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Transpiring Incinerator to deconstruct input items")
                .translation(CONFIG_PREFIX + "transpiring_incinerator_dc_interval")
                .defineInRange("transpiring_incinerator_dc_interval", 8, 1, Integer.MAX_VALUE);

        TI_RC_INTERVAL = BUILDER
                .comment("蒸腾焚化仪重构输出物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Transpiring Incinerator to reconstruct output items")
                .translation(CONFIG_PREFIX + "transpiring_incinerator_rc_interval")
                .defineInRange("transpiring_incinerator_rc_interval", 15, 1, Integer.MAX_VALUE);

        OC_CONSUMPTION = BUILDER
                .comment("光能捕获仪每次消耗输入元质的量",
                        "Amount of input elemenix consumed per operation by the Optical Capturer")
                .translation(CONFIG_PREFIX + "optical_capturer_consumption")
                .defineInRange("optical_capturer_consumption", 90, 1, Integer.MAX_VALUE);

        OC_PRODUCTION = BUILDER
                .comment("光能捕获仪每次转化产生元质的量",
                        "Amount of elemenix produced per transformation by the Optical Capturer")
                .translation(CONFIG_PREFIX + "optical_capturer_production")
                .defineInRange("optical_capturer_production", 90, 1, Integer.MAX_VALUE);

        OC_DC_INTERVAL = BUILDER
                .comment("光能捕获仪解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Optical Capturer to deconstruct input items")
                .translation(CONFIG_PREFIX + "optical_capturer_dc_interval")
                .defineInRange("optical_capturer_dc_interval", 8, 1, Integer.MAX_VALUE);

        OC_RC_INTERVAL = BUILDER
                .comment("光能捕获仪重构输出物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Optical Capturer to reconstruct output items")
                .translation(CONFIG_PREFIX + "optical_capturer_rc_interval")
                .defineInRange("optical_capturer_rc_interval", 20, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("元质解构塔 Elemenic Deconstructors");

        INFUSER_DC_INTERVAL = BUILDER
                .comment("元质注入塔解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Infuser to deconstruct input items")
                .translation(CONFIG_PREFIX + "infuser_dc_interval")
                .defineInRange("infuser_dc_interval", 8, 1, Integer.MAX_VALUE);

        INFUSION_INTERVAL = BUILDER
                .comment("元质注入塔注入元质的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Infuser to infuse elemenix")
                .translation(CONFIG_PREFIX + "infusion_interval")
                .defineInRange("infusion_interval", 15, 1, Integer.MAX_VALUE);

        MAX_INFUSION = BUILDER
                .comment("元质注入塔每次注入元质的最大值",
                        "Maximum amount of elemenix infused per operation by the Elemenic Infuser")
                .translation(CONFIG_PREFIX + "max_infusion")
                .defineInRange("max_infusion", 7776, 1, Integer.MAX_VALUE);

        ENRICHER_DC_INTERVAL = BUILDER
                .comment("元质富集塔解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Enricher to deconstruct input items")
                .translation(CONFIG_PREFIX + "enricher_dc_interval")
                .defineInRange("enricher_dc_interval", 8, 1, Integer.MAX_VALUE);

        ENRICHER_ENRICHING_INTERVAL = BUILDER
                .comment("元质富集塔生产纯质的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Enricher to produce essence")
                .translation(CONFIG_PREFIX + "enricher_enriching_interval")
                .defineInRange("enricher_enriching_interval", 15, 1, Integer.MAX_VALUE);

        EJECTOR_DC_INTERVAL = BUILDER
                .comment("元质射流塔解构输入物品的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Ejector to deconstruct input items")
                .translation(CONFIG_PREFIX + "ejector_dc_interval")
                .defineInRange("ejector_dc_interval", 8, 1, Integer.MAX_VALUE);

        EJECTOR_ENRICHING_INTERVAL = BUILDER
                .comment("元质射流塔生产纯质的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Ejector to produce essence")
                .translation(CONFIG_PREFIX + "ejector_enriching_interval")
                .defineInRange("ejector_enriching_interval", 15, 1, Integer.MAX_VALUE);

        EJECTING_INTERVAL = BUILDER
                .comment("元质射流塔发射纯质的时间间隔（刻）",
                        "Time interval (in ticks) for the Elemenic Ejector to eject essence")
                .translation(CONFIG_PREFIX + "ejecting_interval")
                .defineInRange("ejecting_interval", 15, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
