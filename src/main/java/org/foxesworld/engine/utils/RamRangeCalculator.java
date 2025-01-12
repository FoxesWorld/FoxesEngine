package org.foxesworld.engine.utils;

import com.sun.management.OperatingSystemMXBean;
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

    public SliderRange calculateSliderRange(int numberOfSteps) {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalMemorySize();

        // Конвертируем байты в мегабайты (делим на 1024 * 1024)
        int totalMemoryMB = (int) (totalMemory / (1024 * 1024));

        // Вычисляем минимальное и максимальное значение для слайдера
        int minValue = Math.max(1024, totalMemoryMB / 10);  // минимум 1024 МБ или 10% от общей памяти
        int maxValue = Math.min(64 * 1024, totalMemoryMB * 3 / 4); // максимум 64 ГБ или 75% от общей памяти

        // Убедимся, что maxValue больше minValue
        if (maxValue <= minValue) {
            maxValue = minValue + 1; // Если maxValue <= minValue, корректируем maxValue
        }

        // Вычисляем stepSize на основе диапазона
        int stepSize = calculateStepSize(minValue, maxValue, numberOfSteps);

        // Генерируем значения для слайдера
        List<Integer> values = fillSliderValues(minValue, maxValue, stepSize);

        // Начальное значение слайдера
        int initialValue = Math.min(Math.max(roundToNearestPowerOfTwo(totalMemoryMB / 4), minValue), maxValue);

        return new SliderRange(minValue, maxValue, initialValue, values);
    }



    private int calculateStepSize(int minValue, int maxValue, int numberOfSteps) {
        return Math.max(1, (maxValue - minValue) / numberOfSteps);
    }

    private List<Integer> fillSliderValues(int minValue, int maxValue, int stepSize) {
        List<Integer> values = new ArrayList<>();
        for (int value = minValue; value <= maxValue; value += stepSize) {
            values.add(value);
        }
        return values;
    }

    private int roundToNearestPowerOfTwo(int value) {
        int lower = roundDownToPowerOfTwo(value);
        int upper = roundUpToPowerOfTwo(value);
        return (value - lower < upper - value) ? lower : upper;
    }

    private int roundUpToPowerOfTwo(int value) {
        return value <= 0 ? 1 : Integer.highestOneBit(value - 1) << 1;
    }

    private int roundDownToPowerOfTwo(int value) {
        return value <= 0 ? 1 : Integer.highestOneBit(value);
    }
}
