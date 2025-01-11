package org.foxesworld.engine.utils;

import com.sun.management.OperatingSystemMXBean;
import org.foxesworld.engine.Engine;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class RamRangeCalculator {

    public static class SliderRange {
        private final int minValue;
        private final int maxValue;
        private final int initialValue;
        private final List<Integer> values;

        public SliderRange(int minValue, int maxValue, int initialValue, List<Integer> values) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.initialValue = initialValue;
            this.values = values;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }

        public int getInitialValue() {
            return initialValue;
        }

        public List<Integer> getValues() {
            return values;
        }
    }

    /**
     * Рассчитывает диапазон для RAM на основе общей памяти системы.
     *
     * @return SliderRange объект, содержащий значения для слайдера.
     */
    public SliderRange calculateSliderRange() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalMemorySize();

        int minValue = Math.max(1024, toMB(totalMemory * 0.10));
        int maxValue = Math.min(64 * 1024, toMB(totalMemory * 0.75));

        minValue = roundUpToPowerOfTwo(minValue);
        maxValue = roundDownToPowerOfTwo(maxValue);

        if (maxValue <= minValue) {
            throw new IllegalArgumentException("Invalid range properties: maxValue (" + maxValue + ") must be greater than minValue (" + minValue + ")");
        }
        List<Integer> values = fillSliderValues(minValue, maxValue);
        int initialValue = Math.min(Math.max(roundToNearestPowerOfTwo(toMB(totalMemory * 0.25)), minValue), maxValue);
        return new SliderRange(minValue, maxValue, initialValue, values);
    }

    private int toMB(double bytes) {
        return (int) (bytes / (1024 * 1024));
    }

    private int roundUpToPowerOfTwo(int value) {
        return value <= 0 ? 1 : Integer.highestOneBit(value - 1) << 1;
    }

    private int roundDownToPowerOfTwo(int value) {
        return value <= 0 ? 1 : Integer.highestOneBit(value);
    }

    private int roundToNearestPowerOfTwo(int value) {
        int lower = roundDownToPowerOfTwo(value);
        int upper = roundUpToPowerOfTwo(value);
        return (value - lower < upper - value) ? lower : upper;
    }

    private List<Integer> fillSliderValues(int minValue, int maxValue) {
        List<Integer> values = new ArrayList<>();
        int step = (maxValue - minValue) / 9;
        for (int value = minValue; value <= maxValue; value += step) {
            values.add(value);
        }

        int power = minValue;
        while (power <= maxValue) {
            if (!values.contains(power)) {
                values.add(power);
            }
            power <<= 1;
        }

        values.sort(Integer::compareTo);
        return values;
    }
}
