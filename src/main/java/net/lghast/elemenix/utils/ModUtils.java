package net.lghast.elemenix.utils;

import net.lghast.elemenix.conifig.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class ModUtils {
    public static Item getItemFromString(String itemId) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(itemId);
            return BuiltInRegistries.ITEM.get(resourceLocation);
        } catch (Exception e) {
            return null;
        }
    }

    public static void spawnParticles(ServerLevel serverLevel, ParticleOptions particle, double x, double y, double z,
                                      double dx, double dy, double dz, int count, double speed) {

        serverLevel.sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
    }

    public static String truncateToWidth(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int targetWidth = maxWidth - ellipsisWidth;

        if (targetWidth <= 0) {
            return ellipsis;
        }

        int low = 0;
        int high = text.length();
        int bestPos = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            String substring = text.substring(0, mid);
            int width = font.width(substring);

            if (width <= targetWidth) {
                bestPos = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (bestPos == 0) {
            bestPos = 1;
        }

        return text.substring(0, bestPos).trim() + ellipsis;
    }

    public static long safeAdd(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    public static String formatNumber(long value) {
        if(ClientConfig.DIGIT_GROUPING_BY_FOURS.get()){
            return formatByFourGroups(value);
        }else{
            return formatByThreeGroups(value);
        }
    }

    public static String formatNumber(int value) {
        if(ClientConfig.DIGIT_GROUPING_BY_FOURS.get()){
            return formatByFourGroups(value);
        }else{
            return formatByThreeGroups(value);
        }
    }

    private static final String[] THREE_UNITS = {"", "K", "M", "B", "T", "Q"};
    private static final long[] THREE_VALUES = {
            1L,
            1_000L,           // K - 千
            1_000_000L,       // M - 百万
            1_000_000_000L,   // B - 十亿
            1_000_000_000_000L, // T - 万亿
            1_000_000_000_000_000L // Q - 千兆
    };

    private static final String[] FOUR_UNITS = {"", "W", "Y", "Z", "J"};
    private static final long[] FOUR_VALUES = {
            1L,
            10_000L,              // W - 万
            100_000_000L,         // Y - 亿
            1_000_000_000_000L,   // Z - 兆
            10_000_000_000_000_000L // J - 京
    };

    public static String formatByThreeGroups(long value) {
        if (value <= 0) return "0";

        int unitIndex = 0;
        for (int i = THREE_VALUES.length - 1; i >= 0; i--) {
            if (value >= THREE_VALUES[i]) {
                unitIndex = i;
                break;
            }
        }

        return formatNumber(value, THREE_VALUES[unitIndex], THREE_UNITS[unitIndex], false);
    }

    public static String formatByFourGroups(long value) {
        if (value <= 0) return "0";

        int unitIndex = 0;
        for (int i = FOUR_VALUES.length - 1; i >= 0; i--) {
            if (value >= FOUR_VALUES[i]) {
                unitIndex = i;
                break;
            }
        }

        return formatNumber(value, FOUR_VALUES[unitIndex], FOUR_UNITS[unitIndex], true);
    }

    private static String formatNumber(long number, long unitValue, String unit, boolean isFourGroup) {
        if (unitValue == 1) {
            return String.valueOf(number);
        }

        double value = (double) number / unitValue;

        int integerPart = (int) value;
        boolean hasFourDigits = integerPart >= 1000;

        if (hasFourDigits && isFourGroup) {
            return integerPart + unit;
        }

        double rounded = Math.round(value * 10) / 10.0;

        if (rounded == (int) rounded) {
            return (int) rounded + ".0" + unit;
        } else {
            return rounded + unit;
        }
    }

    public static String formatNumber(long value, String extraFormat) {
        return String.format(extraFormat, formatNumber(value));
    }

    public static String formatNumber(int value, String extraFormat) {
        return String.format(extraFormat, formatNumber(value));
    }

    public static ItemStack findItemInPlayerInventory(Player player, Item item) {
        Inventory playerInventory = player.getInventory();

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return stack;
            }
        }

        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);
        if (optional.isPresent()) {
            ICuriosItemHandler handler = optional.get();
            Optional<SlotResult> result = handler.findFirstCurio(item);
            if (result.isPresent()) {
                return result.get().stack();
            }
        }

        return ItemStack.EMPTY;
    }
}
