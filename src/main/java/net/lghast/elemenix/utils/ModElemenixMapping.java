package net.lghast.elemenix.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModElemenixMapping {
    private static final Map<String, Supplier<Map<String, Constituents>>> MOD_ELEMENIX_MAPPINGS = new HashMap<>();

    static {
        registerModMappings("farmersdelight", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("farmersdelight:straw", new Constituents(Elemenix.ORGANIX, 24));
            map.put("farmersdelight:tomato", new Constituents(20, 0, 12, 0, 0, 0));
            map.put("farmersdelight:rotten_tomato", new Constituents(18, 0, 10, 0, 0, 0));
            map.put("farmersdelight:onion", new Constituents(20, 0, 10, 0, 0, 0));
            map.put("farmersdelight:cabbage", new Constituents(26, 0, 8, 0, 0, 0));
            map.put("farmersdelight:rice_panicle", new Constituents(24, 0, 5, 0, 0, 0));
            map.put("farmersdelight:rice", new Constituents(24, 0, 0, 0, 0, 0));
            map.put("farmersdelight:tree_bark", new Constituents(Elemenix.ORGANIX, 7));
            map.put("farmersdelight:rope", new Constituents(12, 0, 0, 0, 0, 0));
            map.put("farmersdelight:sandy_shrub", new Constituents(6, 0, 2, 0, 0, 0));
            map.put("farmersdelight:canvas", new Constituents(Elemenix.ORGANIX, 96));
            map.put("farmersdelight:tatami", new Constituents(Elemenix.ORGANIX, 120));
            map.put("farmersdelight:full_tatami_mat", new Constituents(Elemenix.ORGANIX, 60));
            map.put("#farmersdelight:wild_crops", new Constituents(64, 0, 24, 0, 0, 0));
            map.put("farmersdelight:brown_mushroom_colony", new Constituents(90, 0, 30, 0, 0, 0));
            map.put("farmersdelight:red_mushroom_colony", new Constituents(90, 0, 30, 0, 0, 0));
            map.put("farmersdelight:wheat_dough", new Constituents(24, 0, 4, 0, 0, 0));
            map.put("farmersdelight:chicken_cuts", new Constituents(15, 0, 4, 0, 0, 0));
            map.put("farmersdelight:cod_slice", new Constituents(12, 0, 4, 0, 0, 0));
            map.put("farmersdelight:salmon_slice", new Constituents(15, 0, 4, 0, 0, 0));
            map.put("farmersdelight:ham", new Constituents(86, 24, 16, 0, 0, 0));
            map.put("farmersdelight:smoked_ham", new Constituents(86, 24, 3, 0, 10, 0));
            map.put("farmersdelight:cake_slice", new Constituents(17, 0, 1, 0, 0, 0));
            map.put("farmersdelight:apple_pie_slice", new Constituents(70, 0, 11, 0, 0, 0));
            map.put("farmersdelight:chocolate_pie_slice", new Constituents(42, 0, 5, 0, 0, 0));
            map.put("farmersdelight:sweet_berry_cheesecake_slice", new Constituents(30, 0, 9, 0, 0, 0));
            map.put("farmersdelight:roast_chicken", new Constituents(72, 0, 10, 0, 7, 0));
            map.put("farmersdelight:shepherds_pie", new Constituents(51, 0, 5, 0, 12, 0));
            map.put("farmersdelight:stuffed_pumpkin", new Constituents(64, 0, 27, 0, 0, 0));
            map.put("farmersdelight:honey_glazed_ham", new Constituents(74, 0, 6, 0, 2, 0));
            map.put("farmersdelight:rich_soil", new Constituents(86, 34, 0, 0, 0, 0));
            map.put("farmersdelight:rich_soil_farmland", new Constituents(86, 34, 0, 0, 0, 0));
            return map;
        });

        registerModMappings("twilightforest", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("twilightforest:chiseled_canopy_bookshelf", new Constituents(Elemenix.ORGANIX, 180));
            map.put("twilightforest:mangrove_root", new Constituents(Elemenix.ORGANIX, 18));
            map.put("twilightforest:root", new Constituents(Elemenix.ORGANIX, 18));
            map.put("twilightforest:moss_patch", new Constituents(3, 0, 2, 0, 0, 0));
            map.put("twilightforest:mining_log_core", new Constituents(96, 0, 0, 30, 0, 500));
            map.put("twilightforest:time_log_core", new Constituents(96, 0, 0, 30, 50, 450));
            map.put("twilightforest:transformation_log_core", new Constituents(96, 0, 0, 30, 50, 450));
            map.put("twilightforest:sorting_log_core", new Constituents(96, 0, 0, 30, 0, 500));
            map.put("twilightforest:mayapple", new Constituents(6, 0, 2, 0, 0, 0));
            map.put("twilightforest:clover_patch", new Constituents(6, 0, 2, 0, 0, 0));
            map.put("twilightforest:fiddlehead", new Constituents(6, 0, 2, 0, 0, 0));
            map.put("twilightforest:mushgloom", new Constituents(18, 0, 6, 0, 2, 0));
            map.put("twilightforest:huge_mushgloom", new Constituents(36, 0, 12, 0, 4, 0));
            map.put("twilightforest:huge_mushgloom_stem", new Constituents(36, 0, 12, 0, 0, 0));
            map.put("twilightforest:torchberries", new Constituents(8, 0, 4, 0, 2, 0));
            map.put("twilightforest:torchberry_plant", new Constituents(24, 0, 10, 0, 5, 0));
            map.put("twilightforest:fallen_leaves", new Constituents(4, 0, 2, 0, 0, 0));
            map.put("twilightforest:root_strand", new Constituents(Elemenix.ORGANIX, 12));
            map.put("twilightforest:hedge", new Constituents(18, 0, 8, 0, 0, 0));
            map.put("twilightforest:firefly", new Constituents(18, 2, 5, 0, 3, 0));
            map.put("twilightforest:cicada", new Constituents(20, 0, 8, 0, 0, 0));
            map.put("twilightforest:moonworm", new Constituents(18, 0, 5, 0, 2, 0));
            map.put("twilightforest:moonworm_queen", new Constituents(180, 0, 60, 0, 50, 50));
            map.put("twilightforest:nagastone", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:etched_nagastone", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:nagastone_head", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:nagastone_pillar", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:nagastone_stairs_left", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:nagastone_stairs_right", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:cracked_nagastone_stairs_left", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:cracked_nagastone_stairs_right", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:mossy_nagastone_stairs_left", new Constituents(6, 24, 0, 0, 0, 0));
            map.put("twilightforest:mossy_nagastone_stairs_right", new Constituents(6, 24, 0, 0, 0, 0));
            map.put("twilightforest:mazestone", new Constituents(Elemenix.TERRIX, 40));
            map.put("twilightforest:wrought_iron_bar", new Constituents(0, 3, 0, 80, 0, 0));
            map.put("twilightforest:huge_water_lily", new Constituents(18, 0, 10, 0, 0, 0));
            map.put("twilightforest:huge_lily_pad", new Constituents(72, 0, 40, 0, 0, 0));
            map.put("twilightforest:underbrick", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:smoker", new Constituents(80, 0, 10, 0, 30, 30));
            map.put("twilightforest:fire_jet", new Constituents(80, 0, 0, 0, 80, 50));
            map.put("twilightforest:aurora_block", new Constituents(Elemenix.ENERGIX, 24));
            map.put("twilightforest:uberous_soil", new Constituents(86, 34, 0, 0, 0, 16));
            map.put("twilightforest:deadrock", new Constituents(Elemenix.TERRIX, 200));
            map.put("twilightforest:cracked_deadrock", new Constituents(Elemenix.TERRIX, 180));
            map.put("twilightforest:weathered_deadrock", new Constituents(Elemenix.TERRIX, 160));
            map.put("twilightforest:giant_cobblestone", new Constituents(Elemenix.TERRIX, 1536));
            map.put("twilightforest:giant_log", new Constituents(Elemenix.ORGANIX, 6144));
            map.put("twilightforest:giant_leaves", new Constituents(384, 0, 128, 0, 0, 0));
            map.put("twilightforest:giant_obsidian", new Constituents(Elemenix.TERRIX, 7680));
            map.put("twilightforest:trollsteinn", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:castle_brick", new Constituents(Elemenix.TERRIX, 24));
            map.put("twilightforest:huge_stalk", new Constituents(96, 0, 30, 0, 0, 0));
            map.put("twilightforest:trollvidr", new Constituents(Elemenix.ORGANIX, 12));
            map.put("twilightforest:unripe_trollber", new Constituents(16, 0, 4, 0, 0, 0));
            map.put("twilightforest:trollber", new Constituents(32, 0, 26, 0, 12, 0));
            map.put("twilightforest:raven_feather", new Constituents(24, 0, 0, 0, 0, 2));
            map.put("twilightforest:maze_map_focus", new Constituents(24, 0, 0, 0, 0, 80));
            map.put("twilightforest:charm_of_life_1", new Constituents(20, 0, 0, 120, 0, 50));
            map.put("twilightforest:charm_of_keeping_1", new Constituents(120, 0, 0, 20, 0, 50));
            map.put("twilightforest:transformation_powder", new Constituents(48, 0, 0, 0, 5, 30));
            map.put("twilightforest:raw_venison", new Constituents(40, 0, 8, 0, 0, 0));
            map.put("twilightforest:raw_meef", new Constituents(30, 0, 8, 0, 0, 0));
            map.put("twilightforest:maze_wafer", new Constituents(Elemenix.ORGANIX, 50));
            map.put("twilightforest:steeleaf_ingot", new Constituents(100, 0, 0, 540, 0, 200));
            map.put("twilightforest:liveroot", new Constituents(36, 0, 0, 0, 0, 200));
            map.put("twilightforest:liveroot_block", new Constituents(45, 0, 0, 0, 0, 200));
            map.put("twilightforest:ironwood_ingot", new Constituents(18, 0, 0, 136, 0, 100));
            map.put("twilightforest:borer_essence", new Constituents(20, 20, 0, 0, 12, 1));
            map.put("twilightforest:naga_scale", new Constituents(1200, 120, 0, 0, 0, 400));
            map.put("twilightforest:trophy_pedestal", new Constituents(0, 5000, 0, 50, 0, 150));
            map.put("twilightforest:ghast_trap", new Constituents(464, 0, 0, 360, 400, 800));
            map.put("twilightforest:antibuilder", new Constituents(1200, 0, 0, 10000, 900, 5000));
            map.put("twilightforest:locked_vanishing_block", new Constituents(60, 2, 0, 0, 1, 0));
            map.put("twilightforest:tower_key", new Constituents(0, 0, 0, 720, 0, 500));
            map.put("twilightforest:red_thread", new Constituents(42, 0, 0, 0, 5, 0));
            map.put("twilightforest:arctic_fur", new Constituents(36, 0, 4, 0, 0, 0));
            map.put("twilightforest:alpha_yeti_fur", new Constituents(1350, 0, 30, 0, 0, 350));
            map.put("twilightforest:armor_shard", new Constituents(0, 0, 0, 62, 0, 25));
            map.put("twilightforest:knightmetal_block", new Constituents(0, 0, 0, 5022, 0, 2025));
            map.put("twilightforest:diamond_minotaur_axe", new Constituents(24, 62000, 0, 0, 0, 1300));
            map.put("twilightforest:golden_minotaur_axe", new Constituents(24, 0, 0, 1555, 0, 100));
            map.put("twilightforest:fiery_tears", new Constituents(60, 0, 500, 0, 3400, 450));
            map.put("twilightforest:fiery_blood", new Constituents(60, 0, 500, 0, 3400, 450));
            map.put("twilightforest:magic_beans", new Constituents(12288, 0, 0, 0, 0, 100));
            map.put("twilightforest:crown_splinter", new Constituents(0, 0, 0, 5000, 0, 150));
            map.put("twilightforest:exanimate_essence", new Constituents(0, 200, 0, 0, 2000, 100));
            map.put("twilightforest:fluffy_cloud", new Constituents(Elemenix.FLUMIX, 4));
            map.put("twilightforest:wispy_cloud", new Constituents(Elemenix.FLUMIX, 4));
            map.put("twilightforest:rainy_cloud", new Constituents(Elemenix.FLUMIX, 32));
            map.put("twilightforest:snowy_cloud", new Constituents(Elemenix.FLUMIX, 32));
            map.put("twilightforest:greater_potion_flask", new Constituents(Elemenix.TERRIX, 32));
            map.put("twilightforest:brittle_potion_flask", new Constituents(Elemenix.TERRIX, 32));
            map.put("twilightforest:coronation_carpet", new Constituents(Elemenix.ORGANIX, 80));
            map.put("twilightforest:ore_magnet", new Constituents(0, 0, 0, 432, 0, 100));
            map.put("twilightforest:ice_bomb", new Constituents(0, 10, 36, 0, 0, 0));
            map.put("twilightforest:ice_sword", new Constituents(24, 0, 700, 124, 0, 0));
            map.put("twilightforest:ice_bow", new Constituents(72, 0, 700, 124, 0, 0));
            map.put("twilightforest:glass_sword", new Constituents(24, 2400, 0, 0, 0, 10));
            map.put("twilightforest:phantom_helmet", new Constituents(0, 0, 0, 50000, 0, 2400));
            map.put("twilightforest:phantom_chestplate", new Constituents(0, 0, 0, 80000, 0, 3500));
            map.put("twilightforest:triple_bow", new Constituents(72, 0, 0, 2592, 0, 500));
            map.put("twilightforest:ender_bow", new Constituents(72, 0, 0, 2592, 0, 900));
            map.put("twilightforest:seeker_bow", new Constituents(72, 0, 0, 2592, 0, 750));
            map.put("twilightforest:lamp_of_cinders", new Constituents(0, 0, 0, 2916, 0, 2000));
            map.put("twilightforest:emperors_cloth", new Constituents(160, 0, 80, 0, 0, 300));
            map.put("twilightforest:ore_meter", new Constituents(160, 0, 0, 1500, 0, 1200));
            map.put("twilightforest:pocket_watch", new Constituents(0, 24, 0, 1200, 24, 1200));
            map.put("twilightforest:moon_dial", new Constituents(18, 24, 0, 2400, 24, 400));
            map.put("twilightforest:crumble_horn", new Constituents(2400, 0, 0, 0, 0, 2000));
            map.put("twilightforest:peacock_fan", new Constituents(1200, 0, 300, 0, 0, 1000));
            map.put("twilightforest:twilight_scepter", new Constituents(60, 240, 0, 0, 3000, 2000));
            map.put("twilightforest:zombie_scepter", new Constituents(3060, 240, 0, 0, 0, 2000));
            map.put("twilightforest:lifedrain_scepter", new Constituents(60, 240, 3000, 0, 0, 2000));
            map.put("twilightforest:fortification_scepter", new Constituents(60, 240, 0, 3000, 0, 2000));
            map.put("twilightforest:carminite", new Constituents(104, 152, 0, 0, 72, 204));
            map.put("twilightforest:fiery_ingot", new Constituents(60, 0, 500, 216, 3400, 450));
            map.put("twilightforest:hydra_chop", new Constituents(405, 0, 50, 0, 50, 30));
            map.put("twilightforest:experiment_115", new Constituents(115, 115, 115, 115, 115, 115));
            map.put("twilightforest:meef_stroganoff", new Constituents(178, 0, 46, 0, 2, 200));
            map.put("twilightforest:stronghold_shield", new Constituents(0, 60000, 0, 3200, 0, 800));
            map.put("twilightforest:mazebreaker_pickaxe", new Constituents(24, 200, 0, 35000, 0, 5000));
            map.put("twilightforest:skull_chest", new Constituents(0, 600, 0, 0, 0, 10));
            map.put("twilightforest:keepsake_casket", new Constituents(0, 600, 0, 320, 0, 50));
            map.put("twilightforest:chipped_keepsake_casket", new Constituents(0, 480, 0, 256, 0, 40));
            map.put("twilightforest:damaged_keepsake_casket", new Constituents(0, 300, 0, 160, 0, 25));
            return map;
        });

        registerModMappings("mynethersdelight", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("mynethersdelight:powder_cannon", new Constituents(6, 0, 0, 0, 1, 0));
            map.put("mynethersdelight:stripped_powdery_block", new Constituents(54, 0, 0, 0, 9, 0));
            map.put("mynethersdelight:bullet_pepper", new Constituents(24, 0, 0, 0, 12, 0));
            map.put("mynethersdelight:pepper_powder", new Constituents(2, 0, 0, 0, 12, 0));
            map.put("mynethersdelight:hoglin_loin", new Constituents(35, 0, 8, 0, 0, 0));
            map.put("mynethersdelight:hoglin_sausage", new Constituents(17, 0, 4, 0, 0, 0));
            map.put("mynethersdelight:ghasta", new Constituents(24, 0, 0, 0, 2, 2));
            map.put("mynethersdelight:ghasmati", new Constituents(22, 0, 0, 0, 2, 2));
            map.put("mynethersdelight:hoglin_hide", new Constituents(Elemenix.ORGANIX, 160));
            map.put("mynethersdelight:strider_slice", new Constituents(36, 0, 6, 0, 2, 0));
            map.put("mynethersdelight:minced_strider", new Constituents(6, 0, 3, 0, 1, 0));
            map.put("mynethersdelight:strider_rock", new Constituents(30, 24, 8, 0, 5, 0));
            map.put("mynethersdelight:strider_egg", new Constituents(30, 5, 8, 0, 3, 0));
            map.put("mynethersdelight:enchanted_golden_egg", new Constituents(30, 0, 8, 10015, 1000, 1000));
            map.put("mynethersdelight:crimson_fungus_colony", new Constituents(225, 0, 85, 0, 0, 0));
            map.put("mynethersdelight:warped_fungus_colony", new Constituents(225, 0, 85, 0, 0, 0));
            map.put("mynethersdelight:resurgent_soil", new Constituents(80, 50, 14, 0, 8, 0));
            map.put("mynethersdelight:resurgent_soil_farmland", new Constituents(80, 50, 14, 0, 8, 0));
            map.put("mynethersdelight:cold_striderloaf", new Constituents(72, 0, 15, 0, 0, 0));
            map.put("mynethersdelight:plate_of_striderloaf", new Constituents(25, 0, 3, 0, 1, 0));
            map.put("mynethersdelight:plate_of_cold_striderloaf", new Constituents(25, 0, 3, 0, 0, 0));
            map.put("mynethersdelight:hot_cream_cone", new Constituents(9, 57, 1, 0, 29, 0));
            map.put("mynethersdelight:hot_wings_bucket", new Constituents(111, 27, 42, 648, 36, 0));
            map.put("mynethersdelight:slices_of_bread", new Constituents(Elemenix.ORGANIX, 20));
            map.put("mynethersdelight:roast_ear", new Constituents(23, 1, 8, 0, 0, 0));
            map.put("mynethersdelight:plate_of_stuffed_hoglin_snout", new Constituents(182, 4, 43, 0, 1, 0));
            map.put("mynethersdelight:plate_of_stuffed_hoglin_ham", new Constituents(158, 3, 39, 0, 1, 0));
            map.put("mynethersdelight:plate_of_stuffed_hoglin", new Constituents(158, 3, 39, 0, 1, 0));
            map.put("mynethersdelight:magma_cake_slice", new Constituents(12, 25, 0, 0, 28, 1));
            map.put("mynethersdelight:hoglin_trophy", new Constituents(286, 120, 0, 432, 0, 0));
            map.put("mynethersdelight:skoglin_trophy", new Constituents(126, 120, 0, 432, 0, 0));
            map.put("mynethersdelight:golden_trophy", new Constituents(126, 120, 0, 6912, 0, 0));
            return map;
        });

        registerModMappings("twilightdelight", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("twilightdelight:mushgloom_colony", new Constituents(90, 0, 30, 0, 10, 0));
            map.put("twilightdelight:experiment_110", new Constituents(110, 110, 110, 110, 110, 110));
            map.put("twilightdelight:experiment_113", new Constituents(113, 113, 113, 113, 113, 113));
            map.put("twilightdelight:raw_tomahawk_smeak", new Constituents(60, 0, 20, 0, 0, 0));
            map.put("twilightdelight:aurora_pie_slice", new Constituents(46, 0, 0, 0, 18, 0));
            map.put("twilightdelight:torchberry_pie_slice", new Constituents(52, 0, 3, 0, 1, 0));
            map.put("twilightdelight:plate_of_fiery_snakes", new Constituents(395, 30, 138, 0, 856, 216));
            map.put("twilightdelight:plate_of_lily_chicken", new Constituents(94, 0, 23, 0, 7, 0));
            map.put("twilightdelight:meef_wellington", new Constituents(70, 4, 11, 0, 1, 0));
            return map;
        });

        registerModMappings("endersdelight", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("endersdelight:mite_crust", new Constituents(6, 0, 0, 0, 0, 1));
            map.put("endersdelight:enderman_sight", new Constituents(16, 0, 0, 0, 0, 12));
            map.put("endersdelight:shulker_mollusk", new Constituents(40, 0, 10, 0, 0, 10));
            map.put("endersdelight:stuffed_shulker_bowl", new Constituents(51, 32, 7, 0, 0, 9));
            map.put("endersdelight:ethereal_saffron", new Constituents(24, 0, 6, 0, 0, 5));
            map.put("endersdelight:amberveil", new Constituents(18, 0, 6, 0, 0, 3));
            map.put("endersdelight:chorusflame", new Constituents(18, 0, 8, 0, 2, 2));
            map.put("endersdelight:voidpepper", new Constituents(24, 0, 6, 0, 0, 3));
            return map;
        });

        registerModMappings("rusticdelight", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("rusticdelight:cotton_boll", new Constituents(Elemenix.ORGANIX, 24));
            map.put("rusticdelight:bell_pepper_green", new Constituents(18, 0, 8, 0, 0, 0));
            map.put("rusticdelight:bell_pepper_red", new Constituents(18, 0, 8, 0, 0, 0));
            map.put("rusticdelight:bell_pepper_yellow", new Constituents(18, 0, 8, 0, 0, 0));
            map.put("rusticdelight:calamari", new Constituents(25, 0, 10, 0, 0, 0));
            map.put("rusticdelight:coffee_beans", new Constituents(Elemenix.ORGANIX, 20));
            return map;
        });

        registerModMappings("jiagureappear", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("jiagureappear:sour_berries", new Constituents(8, 0, 6, 0, 0, 0));
            map.put("jiagureappear:shadow_berries", new Constituents(8, 0, 4, 0, 0, 2));
            map.put("jiagureappear:turtle_plastron", new Constituents(12, 0, 64, 0, 0, 0));
            map.put("jiagureappear:yolime", new Constituents(35, 0, 10, 0, 0, 10));
            map.put("jiagureappear:zucchini", new Constituents(72, 0, 68, 0, 0, 0));
            map.put("#jiagureappear:amethyst_inscriptions", new Constituents(true));
            return map;
        });

        registerModMappings("crittersandcompanions", () -> {
            Map<String, Constituents> map = new HashMap<>();
            map.put("crittersandcompanions:dragonfly_wing", new Constituents(Elemenix.ORGANIX, 16));
            map.put("crittersandcompanions:koi_fish", new Constituents(20, 2, 12, 0, 0, 0));
            map.put("crittersandcompanions:clam", new Constituents(15, 36, 5, 0, 0, 0));
            map.put("crittersandcompanions:pearl", new Constituents(Elemenix.TERRIX, 24));
            map.put("crittersandcompanions:silk", new Constituents(Elemenix.ORGANIX, 24));
            map.put("crittersandcompanions:silk_cocoon", new Constituents(Elemenix.ORGANIX, 72));
            map.put("crittersandcompanions:sea_bunny_slime_bottle", new Constituents(30, 8, 15, 0, 0, 0));
            map.put("crittersandcompanions:koi_fish_bucket", new Constituents(20, 29, 12, 648, 0, 0));
            map.put("crittersandcompanions:sea_bunny_bucket", new Constituents(30, 27, 15, 648, 0, 0));
            map.put("crittersandcompanions:dumbo_octopus_bucket", new Constituents(30, 27, 15, 648, 0, 0));
            return map;
        });
    }

    public static void registerModMappings(String modId, Supplier<Map<String, Constituents>> mappingSupplier) {
        MOD_ELEMENIX_MAPPINGS.put(modId, mappingSupplier);
    }

    public static Map<String, Constituents> getModMappings(String modId) {
        Supplier<Map<String, Constituents>> supplier = MOD_ELEMENIX_MAPPINGS.get(modId);
        return supplier != null ? supplier.get() : Collections.emptyMap();
    }

    public static Set<String> getAvailableModMappings() {
        return MOD_ELEMENIX_MAPPINGS.keySet();
    }
}
