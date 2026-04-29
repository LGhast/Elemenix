package net.lghast.elemenix.utils.recipe;

import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TechnicalRecipe{
    private final List<ItemStack> inputs = new ArrayList<>();
    private final List<ItemStack> offcuts = new ArrayList<>();
    private final ItemStack output;
    private final Constituents additions = new Constituents(0, 0, 0, 0, 0, 0);
    private final Constituents deductions = new Constituents(0, 0, 0, 0, 0, 0);
    private double multiplier = 1.0;
    private Item container;
    private Elemenix removedElemenix;
    private CopperState copperState;

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

    public TechnicalRecipe(String outputId, int count) {
        Item item = ModUtils.getItemFromString(outputId);
        this.output = (item != null && count > 0) ? new ItemStack(item, count) : ItemStack.EMPTY;
    }

    public TechnicalRecipe addInput(String id, int count) {
        Item item = ModUtils.getItemFromString(id);
        if (item != null && count > 0){
            inputs.add(new ItemStack(item, count));
        }
        return this;
    }

    public TechnicalRecipe addOffcut(String input, int count){
        Item item = ModUtils.getItemFromString(input);
        if(item != null && count > 0){
            offcuts.add(new ItemStack(item, count));
        }
        return this;
    }

    public TechnicalRecipe setAddition(Elemenix elemenix, int addition){
        additions.set(elemenix, addition);
        return this;
    }

    public TechnicalRecipe setAdditions(Constituents constituents){
        if(constituents != null) {
            additions.clear();
            additions.add(constituents);
        }
        return this;
    }

    public TechnicalRecipe setDeduction(Elemenix elemenix, int addition){
        deductions.set(elemenix, addition);
        return this;
    }

    public TechnicalRecipe setDeductions(Constituents constituents){
        if(constituents != null) {
            deductions.clear();
            deductions.add(constituents);
        }
        return this;
    }

    public TechnicalRecipe setMultiplier(double multiplier){
        this.multiplier = multiplier;
        return this;
    }

    public TechnicalRecipe setContainer(String id){
        Item container = ModUtils.getItemFromString(id);
        if(container != null) {
            this.container = container;
        }
        return this;
    }

    public TechnicalRecipe setRemovedElemenix(Elemenix removedElemenix) {
        this.removedElemenix = removedElemenix;
        return this;
    }

    public TechnicalRecipe setCopperState(CopperState copperState) {
        this.copperState = copperState;
        return this;
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

    public void setContainer(Item container) {
        this.container = container;
    }

    public Elemenix getRemovedElemenix() {
        return removedElemenix;
    }

    public CopperState getCopperState() {
        return copperState;
    }
}
