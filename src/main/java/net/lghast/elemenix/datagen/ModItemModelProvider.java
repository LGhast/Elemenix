package net.lghast.elemenix.datagen;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Elemenics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.ELEMENIC_ANALYZER.get());
        basicItem(ModItems.FONDANT_CAKE.get());
        basicItem(ModItems.THROTTLE_VALVE.get());
        basicItem(ModItems.FLOW_STRAIGHTENER.get());
        basicItem(ModItems.ORGANIX_ESSENCE.get());
        basicItem(ModItems.TERRIX_ESSENCE.get());
        basicItem(ModItems.FLUMIX_ESSENCE.get());
        basicItem(ModItems.METALLIX_ESSENCE.get());
        basicItem(ModItems.ENERGIX_ESSENCE.get());
        basicItem(ModItems.ARCANIX_ESSENCE.get());
        basicItem(ModItems.ELEMENIC_EQUILIBRIUM.get());
        basicItem(ModItems.ORGANIX_ESSENPLEX.get());
        basicItem(ModItems.TERRIX_ESSENPLEX.get());
        basicItem(ModItems.FLUMIX_ESSENPLEX.get());
        basicItem(ModItems.METALLIX_ESSENPLEX.get());
        basicItem(ModItems.ENERGIX_ESSENPLEX.get());
        basicItem(ModItems.ARCANIX_ESSENPLEX.get());
        basicItem(ModItems.ELEMENIC_EQUILIPLEX.get());
        basicItem(ModItems.MEMORIZER_BOX.get());
        basicItem(ModItems.MEMORY_BURNER.get());
        basicItem(ModItems.NULLVOID.get());
    }
}