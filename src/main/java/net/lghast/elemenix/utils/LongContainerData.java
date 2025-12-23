package net.lghast.elemenix.utils;

import net.minecraft.world.inventory.ContainerData;

public class LongContainerData implements ContainerData {
    private final long[] data;

    public LongContainerData(int size) {
        this.data = new long[size];
    }

    @Override
    public int get(int index) {
        int longIndex = index / 2;
        if (longIndex >= data.length) {
            return 0;
        }

        if (index % 2 == 0) {
            return (int) (data[longIndex] & 0xFFFFFFFFL);
        } else {
            return (int) (data[longIndex] >>> 32);
        }
    }

    @Override
    public void set(int index, int value) {
        int longIndex = index / 2;
        if (longIndex >= data.length) {
            return;
        }

        if (index % 2 == 0) {
            long high = data[longIndex] & 0xFFFFFFFF00000000L;
            data[longIndex] = high | (value & 0xFFFFFFFFL);
        } else {
            long low = data[longIndex] & 0xFFFFFFFFL;
            data[longIndex] = ((long) value << 32) | low;
        }
    }

    public long getLong(int index) {
        return data[index];
    }

    public void setLong(int index, long value) {
        data[index] = value;
    }

    @Override
    public int getCount() {
        return data.length * 2;
    }

    public long[] getData() {
        return data.clone();
    }
}
