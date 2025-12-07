package net.lghast.elemenix.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class Constituents {
    private static final int MAX = 999_999_999;
    final int[] constituents = new int[6];
    boolean unanalysable = false;

    public Constituents(int organix, int terrix, int flumix, int metallix, int energix, int arcanix){
        setAll(organix, terrix, flumix, metallix, energix, arcanix);
    }

    public Constituents(){
        clear();
    }

    public Constituents(Elemenix type, int value){
        clear();
        set(type, value);
    }

    public Constituents(boolean unanalysable){
        clear();
        this.unanalysable = unanalysable;
    }

    public void clear(){
        Arrays.fill(constituents, 0);
    }

    public void fill(){
        Arrays.fill(constituents, MAX);
    }

    private boolean isValidIndex(int index){
        return index>=0 && index<constituents.length;
    }

    private int clampValue(int value) {
        return Math.max(0, Math.min(value, MAX));
    }

    public int get(Elemenix type){
        int index = type.getIndex();
        return isValidIndex(index) ? constituents[index] : 0;
    }

    public void set(Elemenix type, int value){
        int index = type.getIndex();
        if(unanalysable || !isValidIndex(index)){
            return;
        }
        constituents[index] = clampValue(value);
    }

    public void setAll(int organix, int terrix, int flumix, int metallix, int energix, int arcanix) {
        if (unanalysable) return;

        constituents[0] = clampValue(organix);
        constituents[1] = clampValue(terrix);
        constituents[2] = clampValue(flumix);
        constituents[3] = clampValue(metallix);
        constituents[4] = clampValue(energix);
        constituents[5] = clampValue(arcanix);
    }

    public void add(Elemenix type, int addend) {
        int index = type.getIndex();
        if (unanalysable || addend <= 0 || !isValidIndex(index)) return;

        long newValue = (long) constituents[index] + addend;
        constituents[index] = (int) Math.min(newValue, MAX);
    }

    public void consume(Elemenix type, int consumption) {
        int index = type.getIndex();
        if (unanalysable || consumption <= 0 || !isValidIndex(index)) return;

        constituents[index] = Math.max(constituents[index] - consumption, 0);
    }

    public void multiply(double multiple) {
        if(multiple < 0) return;;
       for(int i = 0; i < constituents.length; i++){
           constituents[i] = clampValue((int)Math.floor(constituents[i] * multiple));
       }
    }

    public void minus(Constituents minus) {
        if(minus == null || minus.isUnanalysable()) return;
        for(int i = 0; i < constituents.length; i++){
            constituents[i] = clampValue(constituents[i] - minus.constituents[i]);
        }
    }

    public void add(Constituents addition) {
        if(addition == null || addition.isUnanalysable()) return;
        for(int i = 0; i < constituents.length; i++){
            constituents[i] = clampValue(constituents[i] + addition.constituents[i]);
        }
    }

    public long getSum(){
        if(unanalysable) return Long.MAX_VALUE;
        long sum = 0;
        for (int constituent : constituents) {
            sum += constituent;
        }
        return sum;
    }

    public boolean isPure(Elemenix pureElemenix){
        if(isUnanalysable()) return false;
        for(Elemenix elemenix : Elemenix.values()){
            if(elemenix == pureElemenix && get(elemenix) <= 0){
                return false;
            }
            if(elemenix != pureElemenix && get(elemenix) > 0){
                return false;
            }
        }
        return true;
    }

    public static Constituents sumConstituents(List<ItemStack> stacks){
        if(stacks == null || stacks.isEmpty()) return new Constituents();
        Constituents[] constituentArray = new Constituents[stacks.size()];
        for(int i = 0; i<stacks.size(); i++){
            constituentArray[i] = ElemenixInfo.getConstituents(stacks.get(i));
        }
        return sumConstituents(constituentArray);
    }

    public static Constituents sumConstituents(Constituents... constituentArray){
        if(constituentArray == null || constituentArray.length == 0){
            return new Constituents();
        }

        for(Constituents c : constituentArray){
            if(c != null && c.unanalysable){
                return new Constituents(true);
            }
        }

        Constituents result = new Constituents();
        for(int i = 0; i < result.constituents.length; i++){
            long sum = 0;

            for(Constituents c : constituentArray){
                if(c != null){
                    sum += c.constituents[i];
                }
            }
            if(sum > MAX){
                result.constituents[i] = MAX;
            } else {
                result.constituents[i] = (int)sum;
            }
        }

        return result;
    }

    public boolean isUnanalysable() {
        return unanalysable;
    }

    public Constituents copy() {
        Constituents copy = new Constituents();
        System.arraycopy(this.constituents, 0, copy.constituents, 0, this.constituents.length);
        copy.unanalysable = this.unanalysable;
        return copy;
    }

    @Override
    public String toString() {
        if(unanalysable){
            return "=UNANALYSABLE=";
        }
        return String.format("O:%d, T:%d, F:%d, M:%d, E:%d, A:%d",
                constituents[0], constituents[1], constituents[2], constituents[3], constituents[4], constituents[5]);
    }

    public Component toComponentFormer() {
        return unanalysable ? Component.translatable("tooltip.elemenix.unanalysable").withStyle(ChatFormatting.DARK_GRAY) :
                Component.literal(ModUtils.formatNumber(constituents[0], "O:%s  ")).withColor(Elemenix.ORGANIX.getColor())
                        .append(Component.literal(ModUtils.formatNumber(constituents[1], "T:%s  ")).withColor(Elemenix.TERRIX.getColor()))
                        .append(Component.literal(ModUtils.formatNumber(constituents[2], "F:%s")).withColor(Elemenix.FLUMIX.getColor()));
    }

    public Component toComponentLatter() {
        return Component.literal(ModUtils.formatNumber(constituents[3], "M:%s  ")).withColor(Elemenix.METALLIX.getColor())
                .append(Component.literal(ModUtils.formatNumber(constituents[4], "E:%s  ")).withColor(Elemenix.ENERGIX.getColor()))
                .append(Component.literal(ModUtils.formatNumber(constituents[5], "A:%s")).withColor(Elemenix.ARCANIX.getColor()));
    }

    public static Constituents rawMeat(int hunger){
        return new Constituents(5 * hunger, 0, hunger, 0, 0, 0);
    }

    public static Constituents rawMeat(int hunger, int flumixAddition){
        return new Constituents(5 * hunger, 0, hunger + flumixAddition, 0, 0, 0);
    }

    public static Constituents rawFish(int hunger){
        return new Constituents(5 * hunger, 2, Math.max(hunger, 8), 0, 0, 0);
    }

    public static Constituents stone(){
        return new Constituents(Elemenix.TERRIX, 24);
    }

    public static Constituents grassVineLeaves(){
        return new Constituents(6, 0, 2, 0, 0, 0);
    }

    public static Constituents seagrass(){
        return new Constituents(6, 0, 4, 0, 0, 0);
    }

    public static Constituents flower(){
        return new Constituents(18, 0, 8, 0, 0, 0);
    }

    public static Constituents flowerLarge(){
        return new Constituents(36, 0, 16, 0, 0, 0);
    }
}
