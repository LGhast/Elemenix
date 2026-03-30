package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TRecipeHelper {
    private static final List<TechnicalRecipe> recipes = new ArrayList<>();
    private static boolean handled = false;

    static{
        recipes.add(new TechnicalRecipe("minecraft:written_book", 1).addInput("minecraft:writable_book", 1));
        recipes.add(new TechnicalRecipe("minecraft:farmland", 1).addInput("minecraft:dirt", 1));
        recipes.add(new TechnicalRecipe("minecraft:dirt_path", 1).addInput("minecraft:dirt", 1));
        recipes.add(new TechnicalRecipe("minecraft:bread", 1).addInput("minecraft:wheat", 3));
        recipes.add(new TechnicalRecipe("minecraft:melon_slice", 9).addInput("minecraft:melon", 1));
        recipes.add(new TechnicalRecipe("minecraft:disc_fragment_5", 9).addInput("minecraft:music_disc_5", 1));

        recipes.add(new TechnicalRecipe("minecraft:cookie", 8)
                .addInput("minecraft:wheat", 2)
                .addInput("minecraft:cocoa_beans", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:enchanted_book", 1)
                .addInput("minecraft:book", 1)
                .setAddition(Elemenix.ARCANIX, 10)
        );
        recipes.add(new TechnicalRecipe("minecraft:torch", 4)
                .addInput("minecraft:coal", 1)
                .addInput("minecraft:stick", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:book", 1)
                .addInput("minecraft:paper", 3)
                .addInput("minecraft:leather", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:white_bed", 1)
                .addInput("minecraft:white_wool", 3)
                .addInput("minecraft:oak_planks", 3)
        );
        recipes.add(new TechnicalRecipe("minecraft:lava_bucket", 1)
                .addInput("minecraft:bucket", 1)
                .addInput("minecraft:obsidian", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:carved_pumpkin", 1)
                .addInput("minecraft:pumpkin", 1)
                .addOffcut("minecraft:pumpkin_seeds", 4)
        );
        recipes.add(new TechnicalRecipe("minecraft:suspicious_stew", 1)
                .addInput("minecraft:bowl", 1)
                .addInput("minecraft:red_mushroom", 1)
                .addInput("minecraft:brown_mushroom", 1)
                .setAdditions(Constituents.flower())
        );
        recipes.add(new TechnicalRecipe("minecraft:enchanted_golden_apple", 1)
                .addInput("minecraft:apple", 1)
                .addInput("minecraft:gold_block", 4)
                .setAddition(Elemenix.ARCANIX, 1500)
        );
        recipes.add(new TechnicalRecipe("minecraft:dragon_breath", 1)
                .addInput("minecraft:glass_bottle", 1)
                .setAdditions(new Constituents(0, 0, 60, 0, 10, 50))
        );
        recipes.add(new TechnicalRecipe("minecraft:experience_bottle", 1)
                .addInput("minecraft:glass_bottle", 1)
                .setAdditions(new Constituents(0, 0, 0, 0, 4, 8))
        );
        recipes.add(new TechnicalRecipe("minecraft:chipped_anvil", 1)
                .addInput("minecraft:anvil", 1)
                .setMultiplier(0.7)
        );
        recipes.add(new TechnicalRecipe("minecraft:damaged_anvil", 1)
                .addInput("minecraft:anvil", 1)
                .setMultiplier(0.5)
        );
        recipes.add(new TechnicalRecipe("minecraft:iron_horse_armor", 1)
                .addInput("minecraft:iron_ingot", 6)
                .addInput("minecraft:leather", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:golden_horse_armor", 1)
                .addInput("minecraft:gold_ingot", 6)
                .addInput("minecraft:leather", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:diamond_horse_armor", 1)
                .addInput("minecraft:diamond", 6)
                .addInput("minecraft:leather", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:firework_star", 1)
                .addInput("minecraft:gunpowder", 1)
                .addInput("minecraft:red_dye", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:firework_rocket", 3)
                .addInput("minecraft:paper", 1)
                .addInput("minecraft:gunpowder", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:bundle", 1)
                .addInput("minecraft:leather", 1)
                .addInput("minecraft:string", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:golden_apple", 1)
                .addInput("minecraft:gold_ingot", 8)
                .addInput("minecraft:apple", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:golden_carrot", 1)
                .addInput("minecraft:gold_nugget", 8)
                .addInput("minecraft:carrot", 1)
        );
        recipes.add(new TechnicalRecipe("minecraft:honey_bottle", 1)
                .addInput("minecraft:glass_bottle", 1)
                .setAddition(Elemenix.ORGANIX, 60)
        );
        recipes.add(new TechnicalRecipe("minecraft:honey_block", 1)
                .addInput("minecraft:honey_bottle", 4)
                .addOffcut("minecraft:glass_bottle", 4)
        );

        addNewBucket("minecraft:water_bucket", Elemenix.FLUMIX, 4);
        addNewBucket("minecraft:powder_snow_bucket", Elemenix.FLUMIX, 32);
        addNewBucket("minecraft:milk_bucket", new Constituents(5, 0, 6, 0, 0, 0));
        addNewMobBucket("minecraft:cod_bucket", "minecraft:cod");
        addNewMobBucket("minecraft:salmon_bucket", "minecraft:salmon");
        addNewMobBucket("minecraft:tropical_fish_bucket", "minecraft:tropical_fish");
        addNewMobBucket("minecraft:pufferfish_bucket", "minecraft:pufferfish");
        addNewMobBucket("minecraft:tadpole_bucket", new Constituents(20, 0, 15, 0, 0, 0));
        addNewMobBucket("minecraft:axolotl_bucket", new Constituents(50, 5, 25, 0, 0, 0));

        addNewCopperItems("minecraft", "copper_block", "copper");
        addNewCopperItems("minecraft", "copper_door");
        addNewCopperItems("minecraft", "copper_trapdoor");

        addNewTemplate("minecraft:netherite_upgrade_smithing_template", "minecraft:diamond", "minecraft:netherrack");
        addNewTemplate("minecraft:sentry_armor_trim_smithing_template", "minecraft:diamond", "minecraft:cobblestone");
        addNewTemplate("minecraft:vex_armor_trim_smithing_template", "minecraft:diamond", "minecraft:cobblestone");
        addNewTemplate("minecraft:wild_armor_trim_smithing_template", "minecraft:diamond", "minecraft:mossy_cobblestone");
        addNewTemplate("minecraft:coast_armor_trim_smithing_template", "minecraft:diamond", "minecraft:cobblestone");
        addNewTemplate("minecraft:dune_armor_trim_smithing_template", "minecraft:diamond", "minecraft:sandstone");
        addNewTemplate("minecraft:wayfinder_armor_trim_smithing_template", "minecraft:diamond", "minecraft:terracotta");
        addNewTemplate("minecraft:raiser_armor_trim_smithing_template", "minecraft:diamond", "minecraft:terracotta");
        addNewTemplate("minecraft:shaper_armor_trim_smithing_template", "minecraft:diamond", "minecraft:terracotta");
        addNewTemplate("minecraft:host_armor_trim_smithing_template", "minecraft:diamond", "minecraft:terracotta");
        addNewTemplate("minecraft:ward_armor_trim_smithing_template", "minecraft:diamond", "minecraft:cobbled_deepslate");
        addNewTemplate("minecraft:silence_armor_trim_smithing_template", "minecraft:diamond", "minecraft:cobbled_deepslate");
        addNewTemplate("minecraft:tide_armor_trim_smithing_template", "minecraft:diamond", "minecraft:prismarine");
        addNewTemplate("minecraft:snout_armor_trim_smithing_template", "minecraft:diamond", "minecraft:blackstone");
        addNewTemplate("minecraft:rib_armor_trim_smithing_template", "minecraft:diamond", "minecraft:netherrack");
        addNewTemplate("minecraft:eye_armor_trim_smithing_template", "minecraft:diamond", "minecraft:end_stone");
        addNewTemplate("minecraft:spire_armor_trim_smithing_template", "minecraft:diamond", "minecraft:purpur_block");
        addNewTemplate("minecraft:flow_armor_trim_smithing_template", "minecraft:diamond", "minecraft:breeze_rod");
        addNewTemplate("minecraft:bolt_armor_trim_smithing_template", "minecraft:diamond", "minecraft:copper_block");

        if(ModUtils.hasServerMod("farmersdelight")){
            recipes.add(new TechnicalRecipe("farmersdelight:rope", 2).addInput("farmersdelight:straw", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:canvas", 1).addInput("farmersdelight:straw", 4));
            recipes.add(new TechnicalRecipe("farmersdelight:full_tatami_mat", 2).addInput("farmersdelight:tatami", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:half_tatami_mat", 2).addInput("farmersdelight:full_tatami_mat", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:pumpkin_slice", 4).addInput("minecraft:pumpkin", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:rich_soil", 1).addInput("farmersdelight:organic_compost", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:rich_soil_farmland", 1).addInput("farmersdelight:organic_compost", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:canvas_rug", 2).addInput("farmersdelight:canvas", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:cake_slice", 7).addInput("minecraft:cake", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:apple_pie_slice", 4).addInput("farmersdelight:apple_pie", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:chocolate_pie_slice", 4).addInput("farmersdelight:chocolate_pie", 1));
            recipes.add(new TechnicalRecipe("farmersdelight:sweet_berry_cheesecake_slice", 4).addInput("farmersdelight:sweet_berry_cheesecake", 1));

            recipes.add(new TechnicalRecipe("farmersdelight:tatami", 1)
                    .addInput("minecraft:straw", 1)
                    .addInput("minecraft:canvas", 1)
            );
            recipes.add(new TechnicalRecipe("farmersdelight:wheat_dough", 1)
                    .addInput("minecraft:wheat", 1)
                    .setAddition(Elemenix.FLUMIX, 4)
            );
            recipes.add(new TechnicalRecipe("farmersdelight:ham", 1)
                    .addInput("minecraft:bone", 1)
                    .addInput("minecraft:porkchop", 2)
            );

            addNewFoodUnit("farmersdelight:roast_chicken", "farmersdelight:roast_chicken_block");
            addNewFoodUnit("farmersdelight:shepherds_pie", "farmersdelight:shepherds_pie_block");
            addNewFoodUnit("farmersdelight:stuffed_pumpkin", 4, "minecraft:bowl", "farmersdelight:stuffed_pumpkin_block");
            addNewFoodUnit("farmersdelight:honey_glazed_ham", "farmersdelight:honey_glazed_ham_block", "minecraft:bone");
        }

        if(ModUtils.hasServerMod("twilightforest")){
            recipes.add(new TechnicalRecipe("twilightforest:giant_cobblestone", 1).addInput("minecraft:cobblestone", 64));
            recipes.add(new TechnicalRecipe("twilightforest:giant_log", 1).addInput("minecraft:oak_log", 64));
            recipes.add(new TechnicalRecipe("twilightforest:giant_leaves", 1).addInput("minecraft:oak_leaves", 64));
            recipes.add(new TechnicalRecipe("twilightforest:giant_obsidian", 1).addInput("minecraft:obsidian", 64));
            recipes.add(new TechnicalRecipe("twilightforest:knightmetal_ingot", 1).addInput("twilightforest:armor_shard_cluster", 1));
            recipes.add(new TechnicalRecipe("twilightforest:knightmetal_block", 1).addInput("twilightforest:knightmetal_ingot", 9));

            recipes.add(new TechnicalRecipe("twilightforest:cracked_deadrock", 1)
                    .addInput("twilightforest:deadrock", 1)
                    .setMultiplier(0.8)
            );
            recipes.add(new TechnicalRecipe("twilightforest:weathered_deadrock", 1)
                    .addInput("twilightforest:deadrock", 1)
                    .setMultiplier(0.5)
            );
            recipes.add(new TechnicalRecipe("twilightforest:iron_berry", 1)
                    .addInput("minecraft:iron_nugget", 1)
                    .setAddition(Elemenix.ORGANIX, 5)
            );
            recipes.add(new TechnicalRecipe("twilightforest:gold_berry", 1)
                    .addInput("minecraft:gold_nugget", 1)
                    .setAddition(Elemenix.ORGANIX, 5)
            );
            recipes.add(new TechnicalRecipe("twilightforest:copper_berry", 1)
                    .addInput("twilightforest:copper_nugget", 1)
                    .setAddition(Elemenix.ORGANIX, 5)
            );
            recipes.add(new TechnicalRecipe("twilightforest:fiery_ingot", 1)
                    .addInput("twilightforest:fiery_blood", 1)
                    .addInput("minecraft:iron_ingot", 1)
            );
        }

        if(ModUtils.hasServerMod("twilightdelight")){
            recipes.add(new TechnicalRecipe("twilightdelight:raw_tomahawk_smeak", 1).addInput("twilightforest:raw_meef", 4));
            recipes.add(new TechnicalRecipe("twilightdelight:aurora_pie_slice", 4).addInput("twilightdelight:aurora_pie", 1));
            recipes.add(new TechnicalRecipe("twilightdelight:torchberry_pie_slice", 4).addInput("twilightdelight:torchberry_pie", 1));

            addNewFoodUnit("twilightdelight:plate_of_fiery_snakes", "twilightdelight:fiery_snakes_block");
            addNewFoodUnit("twilightdelight:plate_of_meef_wellington", "twilightdelight:meef_wellington_block");
            addNewFoodUnit("twilightdelight:plate_of_lily_chicken", "twilightdelight:lily_chicken_block", "minecraft:bone_meal");
        }

        if(ModUtils.hasServerMod("sophisticatedbackpacks")){
            recipes.add(new TechnicalRecipe("sophisticatedbackpacks:netherite_backpack", 1)
                    .addInput("sophisticatedbackpacks:diamond_backpack", 1)
                    .addInput("minecraft:netherite_ingot", 1)
                    .addInput("minecraft:netherite_upgrade_smithing_template", 1)
            );
        }

        if(ModUtils.hasServerMod("mynethersdelight")){
            recipes.add(new TechnicalRecipe("mynethersdelight:stripped_powdery_block", 1).addInput("mynethersdelight:powdery_block", 1));
            recipes.add(new TechnicalRecipe("mynethersdelight:hoglin_sausage", 2).addInput("mynethersdelight:hoglin_loin", 1));
            recipes.add(new TechnicalRecipe("mynethersdelight:resurgent_soil", 1).addInput("mynethersdelight:letios_compost", 1));
            recipes.add(new TechnicalRecipe("mynethersdelight:resurgent_soil_farmland", 1).addInput("mynethersdelight:letios_compost", 1));
            recipes.add(new TechnicalRecipe("mynethersdelight:magma_cake_slice", 7).addInput("mynethersdelight:magma_cake_block", 1));
            addNewFoodUnit("mynethersdelight:plate_of_striderloaf", "mynethersdelight:striderloaf", "minecraft:string");
            addNewFoodUnit("mynethersdelight:plate_of_cold_striderloaf", "mynethersdelight:cold_striderloaf", "minecraft:string");
            addNewFoodUnit("mynethersdelight:plate_of_ghasta_with_cream", "mynethersdelight:ghasta_with_cream", "minecraft:magma_cream");

            recipes.add(new TechnicalRecipe("mynethersdelight:ghasmati", 1)
                    .addInput("mynethersdelight:ghasta", 1)
                    .setDeduction(Elemenix.ORGANIX, 2)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:minced_strider", 2)
                    .addInput("mynethersdelight:strider_slice", 1)
                    .addOffcut("minecraft:string", 1)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:cold_striderloaf", 1)
                    .addInput("mynethersdelight:striderloaf", 1)
                    .setRemovedElemenix(Elemenix.ENERGIX)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:boiled_egg", 1)
                    .addInput("mynethersdelight:strider_egg", 1)
                    .setRemovedElemenix(Elemenix.TERRIX)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:hot_wings", 1)
                    .addInput("mynethersdelight:pepper_powder", 1)
                    .addInput("farmersdelight:onion", 1)
                    .addInput("farmersdelight:chicken_cuts", 1)
                    .addInput("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:hot_cream", 1)
                    .addInput("mynethersdelight:pepper_powder", 2)
                    .addInput("minecraft:magma_cream", 2)
                    .addInput("minecraft:egg", 1)
                    .addInput("minecraft:lava_bucket", 1)
                    .setRemovedElemenix(Elemenix.FLUMIX)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:hot_cream_cone", 3)
                    .addInput("mynethersdelight:hot_cream", 1)
                    .addInput("mynethersdelight:powder_cannon", 3)
                    .addOffcut("minecraft:bucket", 1)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:ghast_dough", 1)
                    .addInput("mynethersdelight:ghasmati", 1)
                    .addInput("minecraft:egg", 1)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:skoglin_trophy", 1)
                    .addInput("mynethersdelight:hoglin_trophy", 1)
                    .addOffcut("mynethersdelight:hoglin_hide", 1)
            );
            recipes.add(new TechnicalRecipe("mynethersdelight:golden_trophy", 1)
                    .addInput("mynethersdelight:skoglin_trophy", 1)
                    .addInput("minecraft:gold_ingot", 15)
            );
        }

        if(ModUtils.hasServerMod("endersdelight")){
            addNewFoodUnit("endersdelight:stuffed_shulker_bowl", 4, "endersdelight:shulker_bowl", "endersdelight:stuffed_shulker");
        }

        if(ModUtils.hasServerMod("rusticdelight")){
            recipes.add(new TechnicalRecipe("rusticdelight:syrup_cheesecake_slice", 4).addInput("rusticdelight:syrup_cheesecake", 1));
            recipes.add(new TechnicalRecipe("rusticdelight:cherry_blossom_cheesecake_slice", 4).addInput("rusticdelight:cherry_blossom_cheesecake", 1));

            recipes.add(new TechnicalRecipe("rusticdelight:pancake", 6)
                    .addInput("rusticdelight:pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("rusticdelight:honey_pancake", 6)
                    .addInput("rusticdelight:honey_pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("rusticdelight:chocolate_pancake", 6)
                    .addInput("rusticdelight:chocolate_pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("rusticdelight:cherry_blossom_pancake", 6)
                    .addInput("rusticdelight:cherry_blossom_pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("rusticdelight:vegetable_pancake", 6)
                    .addInput("rusticdelight:vegetable_pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("rusticdelight:pumpkin_pancake", 6)
                    .addInput("rusticdelight:pumpkin_pancakes", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
        }

        if(ModUtils.hasServerMod("crittersandcompanions")){
            recipes.add(new TechnicalRecipe("crittersandcompanions:sea_bunny_slime_bottle", 1)
                    .addInput("minecraft:glass_bottle", 1)
                    .setAdditions(new Constituents(30, 0, 15, 0, 0, 0))
            );

            addNewMobBucket("crittersandcompanions:koi_fish_bucket", "crittersandcompanions:koi_fish");
            addNewMobBucket("crittersandcompanions:sea_bunny_bucket", new Constituents(30, 0, 15, 0, 0, 0));
            addNewMobBucket("crittersandcompanions:dumbo_octopus_bucket", new Constituents(50, 0, 15, 0, 0, 0));
        }

        if(ModUtils.hasServerMod("dungeonsdelight")){
            recipes.add(new TechnicalRecipe("dungeonsdelight:wormroot_stalk", 1).addInput("dungeonsdelight:wormroot_tendrils", 4));
            recipes.add(new TechnicalRecipe("dungeonsdelight:rotbulb_crop", 1).addInput("dungeonsdelight:rotbulb", 1));
            recipes.add(new TechnicalRecipe("dungeonsdelight:ghast_calamari", 2).addInput("dungeonsdelight:ghast_tentacle", 1));
            recipes.add(new TechnicalRecipe("dungeonsdelight:polterghast_pizza_slice", 4).addInput("dungeonsdelight:polterghast_pizza", 1));
            recipes.add(new TechnicalRecipe("dungeonsdelight:monster_cake_slice", 7).addInput("dungeonsdelight:monster_cake", 1));
            recipes.add(new TechnicalRecipe("dungeonsdelight:spider_pie_slice", 4).addInput("dungeonsdelight:spider_pie", 1));
            recipes.add(new TechnicalRecipe("dungeonsdelight:sculk_tart_slice", 4).addInput("dungeonsdelight:sculk_tart", 1));

            recipes.add(new TechnicalRecipe("dungeonsdelight:candied_silverfish_sucker", 1)
                    .addInput("dungeonsdelight:amethyst_rock_candy", 1)
                    .setAdditions(new Constituents(24, 0, 5, 0, 0, 0))
            );
            recipes.add(new TechnicalRecipe("dungeonsdelight:candied_vex_sucker", 1)
                    .addInput("dungeonsdelight:amethyst_rock_candy", 1)
                    .setAdditions(new Constituents(0, 0, 12, 0, 0, 10))
            );
            recipes.add(new TechnicalRecipe("dungeonsdelight:carved_rotgourd", 1)
                    .addInput("dungeonsdelight:rotgourd", 1)
                    .addOffcut("dungeonsdelight:gunk", 2)
            );

            addNewFoodUnit("dungeonsdelight:ossobuco", 4, "minecraft:bowl", "dungeonsdelight:ossobuco_block", "minecraft:skeleton_skull");
            addNewFoodUnit("dungeonsdelight:guardian_angel", "dungeonsdelight:guardian_angel_block");
            addNewFoodUnit("dungeonsdelight:silverfish_and_chips", "dungeonsdelight:silverfish_and_chips_block");
            addNewFoodUnit("dungeonsdelight:monster_mousse", "dungeonsdelight:monster_mousse_block");
        }

        if(ModUtils.hasServerMod("create")){
            recipes.add(new TechnicalRecipe("create:cardboard", 1).addInput("create:pulp", 1));
            recipes.add(new TechnicalRecipe("create:cardboard_block", 1).addInput("create:cardboard", 4));
            recipes.add(new TechnicalRecipe("create:linear_chassis", 1).addInput("create:secondary_linear_chassis", 1));
            recipes.add(new TechnicalRecipe("create:sail_frame", 1).addInput("create:white_sail", 1));
            recipes.add(new TechnicalRecipe("create:repackager", 1).addInput("create:packager", 1));

            recipes.add(new TechnicalRecipe("create:wheat_flour", 1)
                    .addInput("minecraft:wheat", 1)
                    .setMultiplier(0.5)
            );
            recipes.add(new TechnicalRecipe("create:powdered_obsidian", 1)
                    .addInput("minecraft:obsidian", 1)
                    .setMultiplier(0.25)
            );
            recipes.add(new TechnicalRecipe("create:sturdy_sheet", 1)
                    .addInput("minecraft:obsidian", 1)
                    .addInput("minecraft:powdered_obsidian", 1)
            );
            recipes.add(new TechnicalRecipe("create:sweet_roll", 1)
                    .addInput("minecraft:bread", 1)
                    .setAdditions(new Constituents(1, 0, 1, 0, 0, 0))
            );
            recipes.add(new TechnicalRecipe("create:andesite_alloy", 1)
                    .addInput("minecraft:andesite", 1)
                    .addInput("create:zinc_nugget", 1)
            );
            recipes.add(new TechnicalRecipe("create:bound_cardboard_block", 1)
                    .addInput("minecraft:string", 1)
                    .addInput("create:cardboard_block", 1)
            );
            recipes.add(new TechnicalRecipe("create:brass_ingot", 2)
                    .addInput("minecraft:copper_ingot", 1)
                    .addInput("create:zinc_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("create:andesite_casing", 1)
                    .addInput("minecraft:stripped_oak_log", 1)
                    .addInput("create:andesite_alloy", 1)
            );
            recipes.add(new TechnicalRecipe("create:brass_casing", 1)
                    .addInput("minecraft:stripped_oak_log", 1)
                    .addInput("create:brass_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("create:andesite_encased_large_cogwheel", 1)
                    .addInput("create:andesite_casing", 1)
                    .addInput("create:large_cogwheel", 1)
            );
            recipes.add(new TechnicalRecipe("create:brass_encased_large_cogwheel", 1)
                    .addInput("create:brass_casing", 1)
                    .addInput("create:large_cogwheel", 1)
            );
            recipes.add(new TechnicalRecipe("create:andesite_encased_cogwheel", 1)
                    .addInput("create:andesite_casing", 1)
                    .addInput("create:cogwheel", 1)
            );
            recipes.add(new TechnicalRecipe("create:brass_encased_cogwheel", 1)
                    .addInput("create:brass_casing", 1)
                    .addInput("create:cogwheel", 1)
            );
            recipes.add(new TechnicalRecipe("create:andesite_encased_shaft", 1)
                    .addInput("create:andesite_casing", 1)
                    .addInput("create:shaft", 1)
            );
            recipes.add(new TechnicalRecipe("create:brass_encased_shaft", 1)
                    .addInput("create:brass_casing", 1)
                    .addInput("create:shaft", 1)
            );
            recipes.add(new TechnicalRecipe("create:copper_casing", 1)
                    .addInput("minecraft:stripped_oak_log", 1)
                    .addInput("minecraft:copper_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("create:railway_casing", 1)
                    .addInput("create:brass_casing", 1)
                    .addInput("create:sturdy_sheet", 1)
            );
            recipes.add(new TechnicalRecipe("create:precision_mechanism", 1)
                    .addInput("create:golden_sheet", 1)
                    .addInput("create:cogwheel", 5)
                    .addInput("create:large_cogwheel", 5)
                    .addInput("minecraft:iron_nugget", 5)
            );
            recipes.add(new TechnicalRecipe("create:speedometer", 1)
                    .addInput("create:andesite_casing", 1)
                    .addInput("minecraft:compass", 1)
            );
            recipes.add(new TechnicalRecipe("create:blaze_burner", 1)
                    .addInput("create:empty_blaze_burner", 1)
                    .setAddition(Elemenix.ENERGIX, 216)
            );
            recipes.add(new TechnicalRecipe("create:haunted_bell", 1)
                    .addInput("create:peculiar_bell", 1)
                    .setAddition(Elemenix.ARCANIX, 5)
            );
            recipes.add(new TechnicalRecipe("create:blaze_cake", 1)
                    .addInput("create:blaze_cake_base", 1)
                    .setAddition(Elemenix.ENERGIX, 60)
                    .setAddition(Elemenix.TERRIX, 30)
            );
            recipes.add(new TechnicalRecipe("create:attribute_filter", 1)
                    .addInput("create:brass_nugget", 2)
                    .addInput("minecraft:white_wool", 1)
            );
            recipes.add(new TechnicalRecipe("create:track", 1)
                    .addInput("create:zinc_nugget", 2)
                    .addInput("minecraft:stone_slab", 1)
            );
            recipes.add(new TechnicalRecipe("create:chocolate_glazed_berries", 1)
                    .addInput("create:bar_of_chocolate", 1)
                    .addInput("minecraft:sweet_berries", 1)
            );
            recipes.add(new TechnicalRecipe("create:honeyed_apple", 1)
                    .setAddition(Elemenix.ORGANIX, 60)
                    .addInput("minecraft:apple", 1)
            );
            recipes.add(new TechnicalRecipe("create:dough", 1)
                    .setAddition(Elemenix.FLUMIX, 4)
                    .addInput("create:wheat_flour", 1)
            );
            recipes.add(new TechnicalRecipe("create:chocolate_bucket", 1)
                    .addInput("create:chocolate_bar", 1)
                    .addInput("minecraft:bucket", 1)
            );

            addNewBucket("create:honey_bucket", Elemenix.ORGANIX, 240);

            addNewCopperItems("create", "copper_tiles");
            addNewCopperItems("create", "copper_shingles");
        }
        if(ModUtils.hasServerMod("iceandfire")){
            recipes.add(new TechnicalRecipe("iceandfire:frozen_dirt", 1)
                    .addInput("minecraft:dirt", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
            recipes.add(new TechnicalRecipe("iceandfire:frozen_dirt_path", 1)
                    .addInput("minecraft:dirt_path", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
            recipes.add(new TechnicalRecipe("iceandfire:frozen_grass", 1)
                    .addInput("minecraft:grass_block", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
            recipes.add(new TechnicalRecipe("iceandfire:frozen_stone", 1)
                    .addInput("minecraft:stone", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
            recipes.add(new TechnicalRecipe("iceandfire:frozen_cobblestone", 1)
                    .addInput("minecraft:cobblestone", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
            recipes.add(new TechnicalRecipe("iceandfire:frozen_gravel", 1)
                    .addInput("minecraft:gravel", 1)
                    .setAddition(Elemenix.FLUMIX, 5)
            );
        }
        if(ModUtils.hasServerMod("eternal_starlight")){
            recipes.add(new TechnicalRecipe("eternal_starlight:ashen_snow", 1).addInput("eternal_starlight:ashen_snowball", 2));
            recipes.add(new TechnicalRecipe("eternal_starlight:aethersent_nugget", 9).addInput("eternal_starlight:aethersent_ingot", 1));
            recipes.add(new TechnicalRecipe("eternal_starlight:molten_stellagmite_slab", 2).addInput("eternal_starlight:molten_stellagmite", 1));
            recipes.add(new TechnicalRecipe("eternal_starlight:molten_stellagmite_stairs", 1).addInput("eternal_starlight:molten_stellagmite", 1));
            recipes.add(new TechnicalRecipe("eternal_starlight:molten_stellagmite_wall", 1).addInput("eternal_starlight:molten_stellagmite", 1));
            recipes.add(new TechnicalRecipe("eternal_starlight:red_starlight_crystal_block", 1).addInput("eternal_starlight:red_starlight_crystal_shard", 4));
            recipes.add(new TechnicalRecipe("eternal_starlight:blue_starlight_crystal_block", 1).addInput("eternal_starlight:blue_starlight_crystal_shard", 4));

            recipes.add(new TechnicalRecipe("eternal_starlight:carved_lunaris_cactus_fruit", 1)
                    .addInput("eternal_starlight:lunaris_cactus_fruit", 1)
                    .setDeductions(new Constituents(2, 0, 2, 0, 0, 0))
            );
            recipes.add(new TechnicalRecipe("eternal_starlight:blooming_shadegrieve", 1)
                    .addInput("eternal_starlight:shadegrieve", 1)
                    .setAddition(Elemenix.ORGANIX, 2)
            );
            recipes.add(new TechnicalRecipe("eternal_starlight:blazing_atalphaite_block", 1)
                    .addInput("eternal_starlight:atalphaite_block", 1)
                    .setAddition(Elemenix.ENERGIX, 12)
            );
            recipes.add(new TechnicalRecipe("eternal_starlight:atalphaite_light", 1).addInput("eternal_starlight:blazing_atalphaite_block", 1));

            recipes.add(new TechnicalRecipe("eternal_starlight:unrealium_ingot", 1)
                    .addInput("eternal_starlight:deepsilver_ingot", 1)
                    .addInput("eternal_starlight:golem_steel_ingot", 1)
                    .addInput("eternal_starlight:malarite", 1)
                    .addInput("eternal_starlight:soul_dew", 1)
            );

            addNewBucket("eternal_starlight:ether_bucket", new Constituents(0, 0, 20, 0, 20, 0));
            addNewMobBucket("eternal_starlight:rookfish_bucket", "eternal_starlight:rookfish");
            addNewMobBucket("eternal_starlight:luminofish_bucket", "eternal_starlight:luminofish");
            addNewMobBucket("eternal_starlight:luminaris_bucket", "eternal_starlight:luminaris");

            addNewTemplate("eternal_starlight:keeper_armor_trim_smithing_template", "eternal_starlight:deepsilver_ingot", "eternal_starlight:grimstone");
            addNewTemplate("eternal_starlight:forge_armor_trim_smithing_template", "eternal_starlight:deepsilver_ingot", "eternal_starlight:voidstone");
            addNewTemplate("eternal_starlight:blooming_armor_trim_smithing_template", "eternal_starlight:amaramber_ingot", "eternal_starlight:grimstone");
            addNewTemplate("eternal_starlight:twining_armor_trim_smithing_template", "eternal_starlight:amaramber_ingot", "eternal_starlight:grimstone");
            addNewTemplate("eternal_starlight:pungency_fruit_upgrade_smithing_template", "eternal_starlight:malarite", "eternal_starlight:nightfall_mud_bricks");
            addNewTemplate("eternal_starlight:starfire_upgrade_smithing_template", "eternal_starlight:thermal_springstone_ingot", "eternal_starlight:starcore_block");
            addNewTemplate("eternal_starlight:flowglaze_upgrade_smithing_template", "eternal_starlight:glacite_shard", "eternal_starlight:eternal_ice");
        }

        if(ModUtils.hasServerMod("delighto_flight")){
            recipes.add(new TechnicalRecipe("delighto_flight:weather_soil_farmland", 1).addInput("delighto_flight:weather_soil", 1));

            recipes.add(new TechnicalRecipe("delighto_flight:phantom_wing", 1)
                    .addInput("minecraft:bone", 1)
                    .addInput("minecraft:phantom_membrane", 2)
            );
            recipes.add(new TechnicalRecipe("delighto_flight:cloud_berry_pancake", 4)
                    .addInput("delighto_flight:cloud_berry_pancake_tower", 1)
                    .addOffcut("minecraft:bowl", 1)
            );
            recipes.add(new TechnicalRecipe("delighto_flight:mushroom_hotpot", 4)
                    .addInput("delighto_flight:mushroom_hotpot_block", 1)
                    .addOffcut("minecraft:copper_ingot", 4)
                    .setContainer("minecraft:bowl")
            );

            addNewFoodUnit("delighto_flight:thunder_fruit_stew", 4, "minecraft:bowl", "delighto_flight:thunder_fruit_stew_block", "minecraft:bucket");
            addNewFoodUnit("delighto_flight:lotus_leaf_rice", "delighto_flight:lotus_leaf_rice_block");
        }

        if(ModUtils.hasServerMod("irons_spellbooks")){
            recipes.add(new TechnicalRecipe("irons_spellbooks:archevoker_logbook_translated", 1).addInput("irons_spellbooks:archevoker_logbook_untranslated", 1));

            recipes.add(new TechnicalRecipe("irons_spellbooks:lightning_bottle", 1)
                    .addInput("minecraft:glass_bottle", 1)
                    .setAdditions(new Constituents(0, 0, 0, 0, 128, 24))
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:blood_vial", 1)
                    .addInput("minecraft:glass_bottle", 1)
                    .setAdditions(new Constituents(16, 0, 24, 0, 0, 2))
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:bloody_vellum", 1)
                    .addInput("irons_spellbooks:hogskin", 1)
                    .setAdditions(new Constituents(64, 0, 0, 0, 0, 8))
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:common_ink", 1)
                    .addInput("minecraft:glass_bottle", 1)
                    .setAdditions(new Constituents(0, 16, 10, 0, 0, 4))
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:uncommon_ink", 1)
                    .addInput("irons_spellbooks:common_ink", 4)
                    .addOffcut("minecraft:glass_bottle", 3)
                    .addInput("minecraft:copper_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:rare_ink", 1)
                    .addInput("irons_spellbooks:uncommon_ink", 4)
                    .addOffcut("minecraft:glass_bottle", 3)
                    .addInput("minecraft:iron_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:epic_ink", 1)
                    .addInput("irons_spellbooks:rare_ink", 4)
                    .addOffcut("minecraft:glass_bottle", 3)
                    .addInput("minecraft:gold_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("irons_spellbooks:legendary_ink", 1)
                    .addInput("irons_spellbooks:epic_ink", 4)
                    .addOffcut("minecraft:glass_bottle", 3)
                    .addInput("minecraft:amethyst_shard", 1)
            );
        }

        if(ModUtils.hasServerMod("anvilcraft")){
            recipes.add(new TechnicalRecipe("anvilcraft:hardend_resin", 1).addInput("anvilcraft:resin", 1));
            recipes.add(new TechnicalRecipe("anvilcraft:large_fluid_tank", 1).addInput("anvilcraft:fluid_tank", 27));
            recipes.add(new TechnicalRecipe("anvilcraft:sea_heart_shell_shard", 2).addInput("anvilcraft:sea_heart_shell", 1));
            recipes.add(new TechnicalRecipe("anvilcraft:nesting_shulker_box", 1).addInput("minecraft:shulker_box", 2));
            recipes.add(new TechnicalRecipe("anvilcraft:over_nesting_shulker_box", 1).addInput("minecraft:shulker_box", 3));
            recipes.add(new TechnicalRecipe("anvilcraft:supercritical_nesting_shulker_box", 1).addInput("minecraft:shulker_box", 4));

            addNewBucket("anvilcraft:exp_bucket", new Constituents(0, 0, 0, 0, 16, 32));
            addNewBucket("anvilcraft:oil_bucket", new Constituents(1600, 0, 0, 0, 800, 0));

            recipes.add(new TechnicalRecipe("anvilcraft:melt_gem_bucket", 1)
                    .addInput("anvilcraft:chromatic_stone", 1)
                    .addInput("minecraft:bucket", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:flour", 1)
                    .addInput("minecraft:wheat", 1)
                    .setMultiplier(0.5)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:pill", 4).addInput("anvilcraft:flour", 1));
            recipes.add(new TechnicalRecipe("anvilcraft:exp_gem", 1)
                    .addInput("anvilcraft:exp_bucket", 1)
                    .addOffcut("minecraft:bucket", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:magnet_ingot", 1)
                    .addInput("minecraft:iron_ingot", 1)
                    .setAddition(Elemenix.ENERGIX, 18)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:supercapacitor_empty", 1)
                    .addInput("anvilcraft:resin_block", 1)
                    .addInput("minecraft:heavy_weighted_pressure_plate", 2)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:transcendium_ingot", 9)
                    .addInput("anvilcraft:charged_neutronium_ingot", 1)
                    .addInput("anvilcraft:ember_metal_block", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:menger_sponge", 1)
                    .addInput("minecraft:sponge", 20)
                    .addInput("anvilcraft:void_matter_block", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:shulker_container", 1)
                    .addInput("minecraft:netherite_block", 20)
                    .addInput("minecraft:shulker_box", 6)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:space_overcompressor", 1)
                    .addInput("anvilcraft:supercritical_nesting_shulker_box", 1)
                    .addInput("anvilcraft:void_matter_block", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:confined_neutronium_ingot", 1)
                    .addInput("anvilcraft:charged_neutronium_ingot", 1)
                    .addInput("anvilcraft:confinement_chamber", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:tempering_glass", 8)
                    .addInput("anvilcraft:quartz_sand", 8)
                    .addInput("anvilcraft:royal_steel_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:ember_glass", 8)
                    .addInput("anvilcraft:quartz_sand", 8)
                    .addInput("anvilcraft:ember_metal_ingot", 1)
            );
            recipes.add(new TechnicalRecipe("anvilcraft:frost_glass", 8)
                    .addInput("anvilcraft:quartz_sand", 8)
                    .addInput("anvilcraft:frost_metal_ingot", 1)
            );

            addNewCopperItems("anvilcraft", "copper_pressure_plate");
        }

        if(ModUtils.hasServerMod("natures_spirit")){
            addNewBucket("natures_spirit:cheese_bucket", Elemenix.ORGANIX, 24);

            recipes.add(new TechnicalRecipe("natures_spirit:quarter_pizza", 4).addInput("natures_spirit:whole_pizza", 1));
            recipes.add(new TechnicalRecipe("natures_spirit:half_pizza", 2).addInput("natures_spirit:whole_pizza", 1));
            recipes.add(new TechnicalRecipe("natures_spirit:three_quarters_pizza", 1).addInput("natures_spirit:quarter_pizza", 3));
            recipes.add(new TechnicalRecipe("natures_spirit:stripped_alluaudia", 4).addInput("natures_spirit:alluaudia", 1));

            recipes.add(new TechnicalRecipe("natures_spirit:frosty_redwood_leaves", 1)
                    .addInput("natures_spirit:redwood_leaves", 1)
                    .setAddition(Elemenix.FLUMIX, 2)
            );
            recipes.add(new TechnicalRecipe("natures_spirit:frosty_fir_leaves", 1)
                    .addInput("natures_spirit:fir_leaves", 1)
                    .setAddition(Elemenix.FLUMIX, 2)
            );
        }

        if(ModUtils.hasServerMod("youkaishomecoming")){
            recipes.add(new TechnicalRecipe("youkaishomecoming:tarte_lune_slice", 4).addInput("youkaishomecoming:tarte_lune", 1));

            addNewFoodUnit("youkaishomecoming:bowl_of_heart_throbbing_surprise", 4, "minecraft:bowl", "youkaishomecoming:chest_of_heart_throbbing_surprise", "minecraft:chest");
            addNewMobBucket("youkaishomecoming:lamprey_bucket", "youkaishomecoming:raw_lamprey");

            recipes.add(new TechnicalRecipe("youkaishomecoming:soy_sauce_bottle", 1)
                    .addInput("youkaishomecoming:soy", 1)
                    .addInput("minecraft:glass_bottle", 1)
                    .setAddition(Elemenix.FLUMIX, 4)
            );
            recipes.add(new TechnicalRecipe("youkaishomecoming:daiginjo", 4)
                    .addInput("farmersdelight:rice", 3)
                    .addInput("minecraft:bowl", 4)
                    .addInput("minecraft:nether_wart", 1)
                    .addInput("minecraft:blaze_powder", 1)
            );
            recipes.add(new TechnicalRecipe("youkaishomecoming:dassai", 4)
                    .addInput("farmersdelight:rice", 3)
                    .addInput("minecraft:bowl", 4)
                    .addInput("minecraft:nether_wart", 1)
                    .addInput("minecraft:nautilus_shell", 1)
            );
        }

        if(ModUtils.hasServerMod("arsdelight")){
            recipes.add(new TechnicalRecipe("arsdelight:source_berry_pie_slice", 4).addInput("ars_nouveau:source_berry_pie", 1));
            recipes.add(new TechnicalRecipe("arsdelight:bastion_pie_slice", 4).addInput("arsdelight:bastion_pie", 1));
            recipes.add(new TechnicalRecipe("arsdelight:bombegrante_pie_slice", 4).addInput("arsdelight:bombegrante_pie", 1));
            recipes.add(new TechnicalRecipe("arsdelight:frostaya_pie_slice", 4).addInput("arsdelight:frostaya_pie", 1));
            recipes.add(new TechnicalRecipe("arsdelight:mendosteen_pie_slice", 4).addInput("arsdelight:mendosteen_pie", 1));

            addNewFoodUnit("arsdelight:bowl_of_honey_glazed_chimera", "arsdelight:honey_glazed_chimera");
            addNewFoodUnit("arsdelight:bowl_of_wilden_salad", "arsdelight:wilden_salad");
            addNewFoodUnit("arsdelight:horn_roll", 4, "arsdelight:chimera_horn", "arsdelight:wilden_salad", "minecraft:bowl");
        }

        if(ModUtils.hasServerMod("cataclysm")){
            recipes.add(new TechnicalRecipe("cataclysm:polished_sandstone", 1).addInput("minecraft:sandstone", 1));
            recipes.add(new TechnicalRecipe("cataclysm:obsidian_fence", 1).addInput("minecraft:obsidian", 1));

            recipes.add(new TechnicalRecipe("cataclysm:blessed_amethyst_crab_meat", 1)
                    .addInput("cataclysm:amethyst_crab_meat", 1)
                    .setAddition(Elemenix.ARCANIX, 10)
            );
            recipes.add(new TechnicalRecipe("cataclysm:netherite_ministrosity_bucket", 1)
                    .addInput("cataclysm:netherite_effigy", 1)
                    .addInput("minecraft:bucket", 1)
            );
            recipes.add(new TechnicalRecipe("cataclysm:modern_remnant_bucket", 1)
                    .addInput("cataclysm:remnant_skull", 1)
                    .addInput("minecraft:bucket", 1)
            );
            recipes.add(new TechnicalRecipe("cataclysm:the_baby_leviathan_bucket", 1)
                    .addInput("cataclysm:abyssal_egg", 1)
                    .addInput("minecraft:bucket", 1)
            );
        }

        if(ModUtils.hasServerMod("deep_aether")){
            recipes.add(new TechnicalRecipe("deep_aether:stratus_block", 1).addInput("deep_aether:stratus_ingot", 9));

            addNewBucket("deep_aether:poison_bucket", new Constituents(8, 0, 8, 0, 0, 0));
            addNewBucket("deep_aether:virulent_quicksand_bucket", Elemenix.TERRIX, 32);
            addNewMobBucket("deep_aether:aerglow_fish_bucket", "deep_aether:raw_aerglow_fish");

            addNewTemplate("deep_aether:stratus_smithing_template", "minecraft:diamond", "aether:holystone");
            addNewTemplate("deep_aether:stormforged_smithing_template", "minecraft:diamond", "deep_aether:nimbus_stone");

            recipes.add(new TechnicalRecipe("deep_aether:carved_blue_squash", 1)
                    .addInput("deep_aether:blue_squash", 1)
                    .addOffcut("deep_aether:squash_seeds", 4)
            );
            recipes.add(new TechnicalRecipe("deep_aether:carved_green_squash", 1)
                    .addInput("deep_aether:green_squash", 1)
                    .addOffcut("deep_aether:squash_seeds", 4)
            );
            recipes.add(new TechnicalRecipe("deep_aether:carved_purple_squash", 1)
                    .addInput("deep_aether:purple_squash", 1)
                    .addOffcut("deep_aether:squash_seeds", 4)
            );
            recipes.add(new TechnicalRecipe("deep_aether:skyroot_aerglow_fish_bucket", 1)
                    .addInput("deep_aether:raw_aerglow_fish", 1)
                    .addInput("aether:skyroot_water_bucket", 1)
            );
        }

        if(ModUtils.hasServerMod("deeperdarker")){
            addNewTemplate("deeperdarker:warden_upgrade_smithing_template", "minecraft:diamond", "minecraft:sculk");
        }
    }

    public static void initialize(){
        clear();
        handleMap();
    }

    private static void addNewBucket(String id, Elemenix elemenix, int value){
        recipes.add(new TechnicalRecipe(id, 1)
                .addInput("minecraft:bucket", 1)
                .setAddition(elemenix, value)
        );
    }

    private static void addNewBucket(String id, Constituents constituents){
        recipes.add(new TechnicalRecipe(id, 1)
                .addInput("minecraft:bucket", 1)
                .setAdditions(constituents)
        );
    }

    private static void addNewMobBucket(String id, String mobItemId){
        recipes.add(new TechnicalRecipe(id, 1)
                .addInput("minecraft:water_bucket", 1)
                .addInput(mobItemId, 1)
        );
    }

    private static void addNewMobBucket(String id, Constituents mobConstituents){
        recipes.add(new TechnicalRecipe(id, 1)
                .addInput("minecraft:water_bucket", 1)
                .setAdditions(mobConstituents)
        );
    }

    private static void addNewTemplate(String templateId, String id1, String id2){
        recipes.add(new TechnicalRecipe(templateId, 1)
                .addInput(id1, 7)
                .addInput(id2, 1)
        );
    }

    private static void addNewFoodUnit(String unitId, int count, String containerId, String sourceId, String sourceContainerId){
        recipes.add(new TechnicalRecipe(unitId, count)
                .addInput(sourceId, 1)
                .addOffcut(sourceContainerId, 1)
                .setContainer(containerId)
        );
    }

    private static void addNewFoodUnit(String unitId, String sourceId, String offcutId){
        recipes.add(new TechnicalRecipe(unitId, 4)
                .addInput(sourceId, 1)
                .addOffcut("minecraft:bowl", 1)
                .addOffcut(offcutId, 1)
                .setContainer("minecraft:bowl")
        );
    }

    private static void addNewFoodUnit(String unitId, int count, String containerId, String sourceId){
        recipes.add(new TechnicalRecipe(unitId, count)
                .addInput(sourceId, 1)
                .setContainer(containerId)
        );
    }

    private static void addNewFoodUnit(String unitId, String sourceId){
        addNewFoodUnit(unitId, 4, "minecraft:bowl", sourceId, "minecraft:bowl");
    }

    private static void addNewCopperItems(String namespace, String path){
        String inputId = namespace + ":" + path;

        for(TechnicalRecipe.CopperState copperState : TechnicalRecipe.CopperState.values()) {
            String outputId = namespace + ":" + copperState.getId() + "_" + path;
            recipes.add(new TechnicalRecipe(outputId, 1).addInput(inputId, 1).setCopperState(copperState));
        }
    }

    private static void addNewCopperItems(String namespace, String path, String pathOxidized){
        String inputId = namespace + ":" + path;

        for(TechnicalRecipe.CopperState copperState : TechnicalRecipe.CopperState.values()) {
            String outputId = namespace + ":" + copperState.getId() + "_" + pathOxidized;
            recipes.add(new TechnicalRecipe(outputId, 1).addInput(inputId, 1).setCopperState(copperState));
        }
    }

    public static void clear(){
        handled = false;
    }

    public static void handleMap(){
        if(handled) return;

        if(recipes.isEmpty()) return;

        for(TechnicalRecipe recipe : recipes){
            ItemStack output = recipe.getOutput();
            if(output == null || output.isEmpty()){
                continue;
            }

            if(ElemenixInfo.isUnanalysableStrictly(output) || ElemenixInfo.hasCache(output.getItem())){
                continue;
            }

            List<ItemStack> inputs = recipe.getInputs();
            if(inputs.isEmpty()){
                continue;
            }

            Constituents constituents = new Constituents(false);
            for(ItemStack input : inputs){
                if(input.isEmpty()){
                    continue;
                }

                Constituents inputConstituents = ElemenixInfo.getConstituents(input);

                if(input.is(ModTags.EGGS_WITH_TERRIX_SHELL) && output.is(ModTags.C_FOODS)){
                    inputConstituents.set(Elemenix.TERRIX, 0);
                }
                inputConstituents.multiply(input.getCount());
                constituents.add(inputConstituents);
            }

            TechnicalRecipe.CopperState copperState = recipe.getCopperState();
            if(copperState != null){
                double oxidizingRate = switch (copperState) {
                    case EXPOSED -> 0.2;
                    case WEATHERED -> 0.4;
                    case OXIDIZED -> 0.6;
                };
                int oxidizedPart = (int)(constituents.get(Elemenix.METALLIX) * oxidizingRate);
                constituents.add(Elemenix.TERRIX, oxidizedPart);
                constituents.deduct(Elemenix.METALLIX, oxidizedPart);

                if(!constituents.isUnanalysable()){
                    ElemenixInfo.addCache(output.getItem(), constituents);
                }
                continue;
            }

            List<ItemStack> offcuts = recipe.getOffcuts();
            if(!offcuts.isEmpty()){
                for(ItemStack offcut : offcuts){
                    if(offcut.isEmpty()){
                        continue;
                    }

                    Constituents offcutConstituents = ElemenixInfo.getConstituents(offcut);
                    offcutConstituents.multiply(offcut.getCount());
                    constituents.deduct(offcutConstituents);
                }
            }

            constituents.multiply(recipe.getMultiplier());
            constituents.multiply(1.0 / output.getCount());

            constituents.add(recipe.getAdditions());
            constituents.deduct(recipe.getDeductions());

            Elemenix removedElemenix = recipe.getRemovedElemenix();
            if(removedElemenix != null){
                constituents.set(removedElemenix, 0);
            }

            Item container = recipe.getContainer();
            if(container != null){
                constituents.add(ElemenixInfo.getConstituents(container));
            }

            if(!constituents.isUnanalysable()){
                ElemenixInfo.addCache(output.getItem(), constituents);
            }
        }

        handled = true;
    }
}
