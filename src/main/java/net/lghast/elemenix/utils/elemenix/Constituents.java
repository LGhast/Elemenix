package net.lghast.elemenix.utils.elemenix;

import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Fixed-size container of the six elemenix values, saturated to [0, Long.MAX_VALUE];
 * carries an unanalysable flag that freezes its contents.
 */
public class Constituents {
    public static volatile double DISCOUNT = 1.0;
    public static volatile double PREMIUM = 0;

    final long[] constituents = new long[6];
    boolean unanalysable = false;

    public Constituents(long organix, long terrix, long flumix, long metallix, long energix, long arcanix){
        setAll(organix, terrix, flumix, metallix, energix, arcanix);
    }

    public Constituents(){
        clear();
    }

    public Constituents(Elemenix type, long value){
        clear();
        set(type, value);
    }

    /**
     * Creates an instance whose unanalysable flag is set directly.
     *
     * @param unanalysable when true, all mutation methods become no-ops
     */
    public Constituents(boolean unanalysable){
        clear();
        this.unanalysable = unanalysable;
    }

    public Constituents copy() {
        Constituents copy = new Constituents();
        System.arraycopy(this.constituents, 0, copy.constituents, 0, this.constituents.length);
        copy.unanalysable = this.unanalysable;
        return copy;
    }

    public void clear(){
        Arrays.fill(constituents, 0L);
    }

    public long get(Elemenix type){
        int index = type.getIndex();
        return isValidIndex(index) ? constituents[index] : 0L;
    }

    public void set(Elemenix type, long value) {
        if (unanalysable) return;
        int index = type.getIndex();
        if (!isValidIndex(index)) return;
        constituents[index] = clampValue(value);
    }

    public void setAll(long organix, long terrix, long flumix, long metallix, long energix, long arcanix) {
        if (unanalysable) return;

        constituents[0] = clampValue(organix);
        constituents[1] = clampValue(terrix);
        constituents[2] = clampValue(flumix);
        constituents[3] = clampValue(metallix);
        constituents[4] = clampValue(energix);
        constituents[5] = clampValue(arcanix);
    }

    public void add(Elemenix type, long addition) {
        if (unanalysable || addition <= 0) return;
        int index = type.getIndex();
        if (!isValidIndex(index)) return;
        constituents[index] = saturatedAdd(constituents[index], addition);
    }

    public void deduct(Elemenix type, long deduction) {
        if (unanalysable || deduction <= 0) return;
        int index = type.getIndex();
        if (!isValidIndex(index)) return;
        constituents[index] = Math.max(constituents[index] - deduction, 0L);
    }

    public void multiply(int factor) {
        if(factor < 0) return;
        for(int i = 0; i < constituents.length; i++){
            constituents[i] = saturatedMultiply(constituents[i], factor);
        }
    }

    /**
     * Scales all values by a double factor, rounding down; negative factors are ignored.
     */
    public void multiply(double multiplier) {
        multiplyInternal(multiplier, false);
    }

    /**
     * Same as {@link #multiply(double)} but rounds up instead of down.
     */
    public void multiplyCeil(double multiplier) {
        multiplyInternal(multiplier, true);
    }

    public void add(Constituents other) {
        if (unanalysable || other == null || other.unanalysable) return;
        for (int i = 0; i < constituents.length; i++) {
            constituents[i] = saturatedAdd(constituents[i], other.constituents[i]);
        }
    }

    public void deduct(Constituents other) {
        if (unanalysable || other == null || other.unanalysable) return;
        for (int i = 0; i < constituents.length; i++) {
            constituents[i] = Math.max(constituents[i] - other.constituents[i], 0L);
        }
    }

    /**
     * Total of all six values, saturated to Long.MAX_VALUE; Long.MAX_VALUE when unanalysable.
     */
    public long getSum(){
        if(unanalysable) return Long.MAX_VALUE;
        long sum = 0;
        for (long constituent : constituents) {
            sum = saturatedAdd(sum, constituent);
        }
        return sum;
    }

    public boolean isUnanalysable() {
        return unanalysable;
    }

    /**
     * True when {@code pureElemenix} is the only type with a positive value
     * and that value is non-zero.
     *
     * @param pureElemenix the single type allowed to be present
     */
    public boolean isPure(Elemenix pureElemenix){
        if(unanalysable) return false;
        if(pureElemenix == null) return false;

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

    /**
     * Sums the constituents of a stack list, multiplying each by its count.
     *
     * @return a fresh Constituents; a frozen one when any stack is unanalysable
     */
    public static Constituents sumConstituents(List<ItemStack> stacks){
        if(stacks == null || stacks.isEmpty()) return new Constituents();

        for(ItemStack stack : stacks){
            Constituents c = ElemenixInfo.getConstituents(stack);
            if(c != null && c.unanalysable){
                return new Constituents(true);
            }
        }

        Constituents result = new Constituents();
        for(ItemStack stack : stacks){
            if(stack == null || stack.isEmpty()) continue;

            Constituents constituents = ElemenixInfo.getConstituents(stack);
            if(constituents == null) continue;

            constituents.multiply(stack.getCount());
            result.add(constituents);
        }
        return result;
    }

    /**
     * Applies the global deconstruction discount, rounded up (ceiling).
     */
    public static Constituents getDiscountApplied(Constituents original){
        Constituents discounted = original.copy();
        discounted.multiplyCeil(DISCOUNT);
        return discounted;
    }

    /**
     * Applies the global reconstruction premium, rounded down (floor).
     */
    public static Constituents getPremiumApplied(Constituents original){
        Constituents premiumApplied = original.copy();
        premiumApplied.multiply(1 + PREMIUM);
        return premiumApplied;
    }

    /**
     * Reloads DISCOUNT / PREMIUM from the common config.
     */
    public static void loadFromConfig(){
        DISCOUNT = CommonConfig.DC_DISCOUNT.get();
        PREMIUM = CommonConfig.RC_PREMIUM.get();
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

    public Component toComponentFormerWhole() {
        return unanalysable ? Component.translatable("tooltip.elemenix.unanalysable").withStyle(ChatFormatting.DARK_GRAY) :
                Component.literal(String.format("O:%s  ", constituents[0])).withColor(Elemenix.ORGANIX.getColor())
                        .append(Component.literal(String.format("T:%s  ", constituents[1])).withColor(Elemenix.TERRIX.getColor()))
                        .append(Component.literal(String.format("F:%s", constituents[2])).withColor(Elemenix.FLUMIX.getColor()));
    }

    public Component toComponentLatterWhole() {
        return Component.literal(String.format("M:%s  ", constituents[3])).withColor(Elemenix.METALLIX.getColor())
                .append(Component.literal(String.format("E:%s  ", constituents[4])).withColor(Elemenix.ENERGIX.getColor()))
                .append(Component.literal(String.format("A:%s", constituents[5])).withColor(Elemenix.ARCANIX.getColor()));
    }

    public static Constituents rawMeat(int hunger){
        return new Constituents(5L * hunger, 0, hunger, 0, 0, 0);
    }

    public static Constituents rawMeat(int hunger, int flumixAddition){
        return new Constituents(5L * hunger, 0, hunger + flumixAddition, 0, 0, 0);
    }

    public static Constituents rawFish(int hunger){
        return new Constituents(5L * hunger, 2, Math.max(hunger, 8), 0, 0, 0);
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

    private boolean isValidIndex(int index){
        return index>=0 && index<constituents.length;
    }

    private long clampValue(long value) {
        return Math.max(0L, value);
    }

    private void multiplyInternal(double multiplier, boolean useCeil) {
        if (multiplier < 0) return;
        for (int i = 0; i < constituents.length; i++) {
            double raw = constituents[i] * multiplier;
            long newVal = useCeil ? (long) Math.ceil(raw) : (long) Math.floor(raw);
            constituents[i] = clampValue(newVal);
        }
    }

    /**
     * Saturating addition: caps positive overflow at Long.MAX_VALUE instead of wrapping.
     */
    private static long saturatedAdd(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Saturating multiplication: caps positive overflow at Long.MAX_VALUE instead of wrapping.
     */
    private static long saturatedMultiply(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }
}
