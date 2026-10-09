package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.FluidElemenixInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A technical recipe for constituent calculation where no actual recipe exists
 * but the items are materially related, e.g. raw cod + water bucket -> cod bucket.
 * Also used to manually fill relations where automatic recipe inference fails.
 */
public class TechnicalRecipe{
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<ItemStack> inputs = new ArrayList<>();
    private final List<ItemStack> offcuts = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();
    private final List<FluidStack> fluidOffcuts = new ArrayList<>();

    private final ItemStack output;
    private FluidStack fluidOutput = FluidStack.EMPTY;
    private int fluidOutputCount = 0;
    private final Constituents additions = new Constituents(0, 0, 0, 0, 0, 0);
    private final Constituents deductions = new Constituents(0, 0, 0, 0, 0, 0);
    private double multiplier = 1.0;
    private Item container;
    private Elemenix removedElemenix;
    private CopperState copperState;

    /**
     * Oxidation stages with increasing metallix-to-terrix conversion rates.
     */
    public enum CopperState {
        EXPOSED("exposed", 0.2),
        WEATHERED("weathered", 0.4),
        OXIDIZED("oxidized", 0.6);

        private final String id;
        private final double oxidizingRate;

        CopperState(String id, double rate) {
            this.id = id;
            this.oxidizingRate = rate;
        }

        public String getId() { return id; }
        public double getOxidizingRate() { return oxidizingRate; }
    }

    /**
     * Builds a recipe from an output id.
     *
     * @param outputId item id; with '&' prefix it is a fluid output instead
     * @param count    output amount; for fluids: units of 250 mB
     */
    public TechnicalRecipe(String outputId, int count) {
        if (outputId.startsWith("&")) {
            Fluid fluid = getFluid(outputId.substring(1));
            if (fluid != null && count > 0) {
                this.fluidOutput = new FluidStack(fluid, count * FluidElemenixInfo.FLUID_UNIT);
                this.fluidOutputCount = count;
            }
            this.output = ItemStack.EMPTY;
        } else {
            Item item = ModUtils.getItemFromString(outputId);
            this.output = (item != null && count > 0) ? new ItemStack(item, count) : ItemStack.EMPTY;
        }
    }

    /**
     * Adds an input; '&' prefix means fluid.
     */
    public void addInput(String id, int count) {
        if (id.startsWith("&")) {
            Fluid fluid = getFluid(id.substring(1));
            if (fluid != null && count > 0) {
                fluidInputs.add(new FluidStack(fluid, count * FluidElemenixInfo.FLUID_UNIT));
            }
        } else {
            Item item = ModUtils.getItemFromString(id);
            if (item != null && count > 0) {
                inputs.add(new ItemStack(item, count));
            }
        }
    }

    /**
     * Adds an offcut whose constituents are deducted from the result; '&' prefix means fluid.
     */
    public void addOffcut(String id, int count) {
        if (id.startsWith("&")) {
            Fluid fluid = getFluid(id.substring(1));
            if (fluid != null && count > 0) {
                fluidOffcuts.add(new FluidStack(fluid, count * FluidElemenixInfo.FLUID_UNIT));
            }
        } else {
            Item item = ModUtils.getItemFromString(id);
            if (item != null && count > 0) {
                offcuts.add(new ItemStack(item, count));
            }
        }
    }

    /**
     * Multiplier applied to the raw result before additions/deductions.
     */
    public void setMultiplier(double multiplier){
        this.multiplier = multiplier;
    }

    /**
     * Sets the container that composes the output for some presets;
     * its constituents count toward the result.
     */
    public void setContainer(String id){
        Item container = ModUtils.getItemFromString(id);
        if(container != null) {
            this.container = container;
        }
    }
    public void setContainer(Item container) {
        this.container = container;
    }

    /**
     * Force-zeroes this Elemenix in the result.
     */
    public void setRemovedElemenix(Elemenix removedElemenix) {
        this.removedElemenix = removedElemenix;
    }

    public void setAddition(Elemenix elemenix, int addition){
        additions.set(elemenix, addition);
    }

    public void setAdditions(Constituents constituents){
        if(constituents != null) {
            additions.clear();
            additions.add(constituents);
        }
    }

    public void setDeductions(Constituents constituents){
        if(constituents != null) {
            deductions.clear();
            deductions.add(constituents);
        }
    }

    public void setCopperState(CopperState copperState) {
        this.copperState = copperState;
    }

    public boolean isFluidOutput() {
        return !fluidOutput.isEmpty();
    }

    public FluidStack getFluidOutput() {
        return fluidOutput;
    }

    public int getFluidOutputCount() {
        return fluidOutputCount;
    }

    public List<FluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public List<FluidStack> getFluidOffcuts() {
        return fluidOffcuts;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output;
    }

    public Constituents getAdditions() {
        return additions;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Constituents getDeductions() {
        return deductions;
    }

    public List<ItemStack> getOffcuts() {
        return offcuts;
    }

    public Item getContainer() {
        return container;
    }

    public Elemenix getRemovedElemenix() {
        return removedElemenix;
    }

    public CopperState getCopperState() {
        return copperState;
    }

    private Fluid getFluid(String id) {
        try {
            return BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
        } catch (Exception e) {
            LOGGER.warn("Invalid fluid ID: {}", id);
            return null;
        }
    }
}
