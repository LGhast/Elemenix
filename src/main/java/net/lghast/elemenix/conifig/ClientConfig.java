package net.lghast.elemenix.conifig;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue DIGIT_GROUPING_BY_FOURS;

    public static ModConfigSpec.BooleanValue SHOW_CONSTITUENT_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_UNANALYSABLE_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_MEMORY_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_READONLY_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_STORAGE_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_ANALYZER_UUID_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_VALVE_OPENNESS_TOOLTIPS;

    static {
        BUILDER.push("提示信息配置 Tooltip Configurations");

        DIGIT_GROUPING_BY_FOURS = BUILDER
                .comment("表示元质数值时是否使用四位分节法（万W，亿Y，兆Z，京J）",
                        "Whether to use four-digit grouping for Elemenix values (W for ten-thousand, Y for hundred-million, Z for trillion, J for ten-trillion)",
                        "若不启用，则采用三位分节法（千K，百万M, 十亿B，兆T，千兆Q）",
                        "If disabled, use three-digit grouping (K for thousand, M for million, B for billion, T for trillion, Q for quadrillion)")
                .define("digit_grouping_by_fours", true);

        SHOW_CONSTITUENT_TOOLTIPS = BUILDER
                .comment("是否显示物品元质成分",
                        "Whether to display item elemenix constituents")
                .define("show_constituent_tooltips", true);

        SHOW_UNANALYSABLE_TOOLTIPS = BUILDER
                .comment("不可解析物品是否显示不可解析信息，仅在“是否显示物品元质成分”开启时生效",
                        "Whether unanalysable items show unanalysable information, only effective when 'Whether to display item elemenix constituents' is enabled")
                .define("show_unanalysable_tooltips", true);

        SHOW_MEMORY_TOOLTIPS = BUILDER
                .comment("是否显示元质记忆盘记忆物品个数",
                        "Whether to display the number of memorized items in elemenix memorizer")
                .define("show_memory_tooltips", true);

        SHOW_READONLY_TOOLTIPS = BUILDER
                .comment("涂蜡元质记忆盘是否显示只读信息",
                        "Whether waxed elemenix memorizers show read-only information")
                .define("show_readonly_tooltips", true);

        SHOW_STORAGE_TOOLTIPS = BUILDER
                .comment("是否显示元质解析仪储存元质量",
                        "Whether to display the amount of stored elemenix in elemenix analyzer")
                .define("show_storage_tooltips", true);

        SHOW_ANALYZER_UUID_TOOLTIPS = BUILDER
                .comment("是否显示元质解析仪UUID，仅用于调试",
                        "Whether to display elemenix analyzer UUID. For debugging purposes only")
                .define("show_analyzer_uuid_tooltips", false);

        SHOW_VALVE_OPENNESS_TOOLTIPS = BUILDER
                .comment("是否显示节流阀开度",
                        "Whether to display openness for Throttle Valves")
                .define("show_valve_openness_tooltips", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
