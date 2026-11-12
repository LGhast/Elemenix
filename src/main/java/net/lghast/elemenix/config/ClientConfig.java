package net.lghast.elemenix.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final String CONFIG_PREFIX = "config.elemenix.";

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue DIGIT_GROUPING_BY_FOURS;

    public static ModConfigSpec.BooleanValue SHOW_CONSTITUENT_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_UNANALYSABLE_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_CONSTITUENT_TOOLTIPS_WHEN_SHIFT;
    public static ModConfigSpec.BooleanValue SHOW_MEMORY_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_READONLY_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_STYLE_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_STORAGE_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_ANALYZER_UUID_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_VALVE_OPENNESS_TOOLTIPS;
    public static ModConfigSpec.BooleanValue SHOW_REMOTE_BINDING_PROMPT;
    public static ModConfigSpec.BooleanValue SHOW_REMOTE_COORDINATES;
    public static ModConfigSpec.BooleanValue SHOW_REMOTE_CONNECTION_STATUS;

    public static ModConfigSpec.IntValue ANALYTICAL_WRENCH_FADE_TICKS;
    public static ModConfigSpec.IntValue ANALYTICAL_WRENCH_OUTLINE_COLOR;
    public static ModConfigSpec.DoubleValue ANALYTICAL_WRENCH_TEXT_SCALE;

    public static ModConfigSpec.DoubleValue PRINTER_ITEM_ROTATION_SPEED;
    public static ModConfigSpec.BooleanValue SHOW_PRINTER_ITEM_PREVIEW;

    static {
        BUILDER.push("分析扳手 Analytical Wrench");

        ANALYTICAL_WRENCH_FADE_TICKS = BUILDER
                .comment("分析扳手边框与文本的淡出时长（刻），移开视线后淡出到消失所用的时间",
                        "Fade duration (in ticks) for the Analytical Wrench outline and text after looking away")
                .translation(CONFIG_PREFIX + "analytical_wrench_fade_ticks")
                .defineInRange("analytical_wrench_fade_ticks", 15, 1, 120);

        ANALYTICAL_WRENCH_OUTLINE_COLOR = BUILDER
                .comment("分析扳手默认边框颜色（RGB 十六进制，不带 alpha 字节）",
                        "Default outline color of the Analytical Wrench (RGB hex, without alpha byte)")
                .translation(CONFIG_PREFIX + "analytical_wrench_outline_color")
                .defineInRange("analytical_wrench_outline_color", 0xA1FBE8, 0x000000, 0xFFFFFF);

        ANALYTICAL_WRENCH_TEXT_SCALE = BUILDER
                .comment("分析扳手文本显示大小（相对于标准名称标签大小 0.025 的比例）",
                        "Text display scale of the Analytical Wrench (relative to the standard nametag scale of 0.025)")
                .translation(CONFIG_PREFIX + "analytical_wrench_text_scale")
                .defineInRange("analytical_wrench_text_scale", 0.02, 0.005, 0.05);

        BUILDER.pop();

        BUILDER.push("提示信息配置 Tooltip Configurations");

        DIGIT_GROUPING_BY_FOURS = BUILDER
                .comment("表示元质数值时是否使用四位分节法（万W，亿Y，兆Z，京J）",
                        "若不启用，则采用三位分节法（千K，百万M, 十亿B，兆T，千兆Q）",
                        "Whether to use four-digit grouping for Elemenix values (W for ten-thousand, Y for hundred-million, Z for trillion, J for ten-trillion)",
                        "If disabled, use three-digit grouping (K for thousand, M for million, B for billion, T for trillion, Q for quadrillion)")
                .translation(CONFIG_PREFIX + "digit_grouping_by_fours")
                .define("digit_grouping_by_fours", true);

        SHOW_CONSTITUENT_TOOLTIPS = BUILDER
                .comment("是否显示物品元质成分",
                        "Whether to display item elemenix constituents")
                .translation(CONFIG_PREFIX + "show_constituent_tooltips")
                .define("show_constituent_tooltips", true);

        SHOW_UNANALYSABLE_TOOLTIPS = BUILDER
                .comment("不可解析物品是否显示不可解析信息，仅在'显示元质成分'开启时生效",
                        "Whether unanalysable items show unanalysable information, only effective when 'Show Constituents' is enabled")
                .translation(CONFIG_PREFIX + "show_unanalysable_tooltips")
                .define("show_unanalysable_tooltips", true);

        SHOW_CONSTITUENT_TOOLTIPS_WHEN_SHIFT = BUILDER
                .comment("是否只在按住Shift键时显示物品元质成分信息",
                        "Whether to display item elemenix constituents only when holding Shift key")
                .translation(CONFIG_PREFIX + "show_constituent_tooltips_when_shift")
                .define("show_constituent_tooltips_when_shift", true);

        SHOW_MEMORY_TOOLTIPS = BUILDER
                .comment("是否显示元质记忆盘记忆物品个数",
                        "Whether to display the number of memorized items in elemenix memorizer")
                .translation(CONFIG_PREFIX + "show_memory_tooltips")
                .define("show_memory_tooltips", true);

        SHOW_READONLY_TOOLTIPS = BUILDER
                .comment("涂蜡元质记忆盘是否显示只读信息",
                        "Whether waxed elemenix memorizers show read-only information")
                .translation(CONFIG_PREFIX + "show_readonly_tooltips")
                .define("show_readonly_tooltips", true);

        SHOW_STYLE_TOOLTIPS = BUILDER
                .comment("元质记忆盘是否显示样式信息",
                        "Whether elemenix memorizers show style information")
                .translation(CONFIG_PREFIX + "show_style_tooltips")
                .define("show_style_tooltips", true);

        SHOW_STORAGE_TOOLTIPS = BUILDER
                .comment("是否显示元质解析仪储存元质量",
                        "Whether to display the amount of stored elemenix in elemenix analyzer")
                .translation(CONFIG_PREFIX + "show_storage_tooltips")
                .define("show_storage_tooltips", true);

        SHOW_ANALYZER_UUID_TOOLTIPS = BUILDER
                .comment("是否显示元质解析仪UUID，仅用于调试",
                        "Whether to display elemenix analyzer UUID. For debugging purposes only")
                .translation(CONFIG_PREFIX + "show_analyzer_uuid_tooltips")
                .define("show_analyzer_uuid_tooltips", false);

        SHOW_VALVE_OPENNESS_TOOLTIPS = BUILDER
                .comment("是否显示节流阀开度",
                        "Whether to display openness for Throttle Valves")
                .translation(CONFIG_PREFIX + "show_valve_openness_tooltips")
                .define("show_valve_openness_tooltips", true);

        SHOW_REMOTE_BINDING_PROMPT = BUILDER
                .comment("是否为未绑定的远程储存器显示绑定操作提示",
                        "Whether to display the binding instruction prompt for unbound Remote Storage items")
                .translation(CONFIG_PREFIX + "show_remote_binding_prompt")
                .define("show_remote_binding_prompt", true);

        SHOW_REMOTE_COORDINATES = BUILDER
                .comment("是否显示远程储存器绑定的坐标",
                        "Whether to display the bound coordinates of Remote Storage")
                .translation(CONFIG_PREFIX + "show_remote_coordinates")
                .define("show_remote_coordinates", true);

        SHOW_REMOTE_CONNECTION_STATUS = BUILDER
                .comment("是否显示远程储存器连接有效性和绑定的元质储量",
                        "Whether to display the connection validity status and the Elemenix storage amount at the bound location of Remote Storage")
                .translation(CONFIG_PREFIX + "show_remote_connection_status")
                .define("show_remote_connection_status", true);

        BUILDER.pop();

        BUILDER.push("打印机 Printer");

        PRINTER_ITEM_ROTATION_SPEED = BUILDER
                .comment("实物打印机和巫术打印机内渲染物品的旋转速度（度/刻）",
                        "Rotation speed (degrees per tick) of the item rendered inside the Substance Printer and Witchcraft Printer")
                .translation(CONFIG_PREFIX + "printer_item_rotation_speed")
                .defineInRange("printer_item_rotation_speed", 3.0, 0.0, 30.0);

        SHOW_PRINTER_ITEM_PREVIEW = BUILDER
                .comment("是否显示实物打印机和巫术打印机内正在打印的物品预览",
                        "Whether to render the preview of the item being printed inside the Substance Printer and Witchcraft Printer")
                .translation(CONFIG_PREFIX + "show_printer_item_preview")
                .define("show_printer_item_preview", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
