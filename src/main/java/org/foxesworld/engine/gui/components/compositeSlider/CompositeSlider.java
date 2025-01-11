package org.foxesworld.engine.gui.components.compositeSlider;

import com.sun.management.OperatingSystemMXBean;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.label.LabelStyle;
import org.foxesworld.engine.gui.components.slider.TexturedSliderUI;
import org.foxesworld.engine.gui.components.spinner.Spinner;
import org.foxesworld.engine.utils.RamRangeCalculator;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CompositeSlider extends JComponent {
    private LabelStyle labelStyle;
    private final ComponentFactory componentFactory;
    private final ComponentAttributes componentAttribute;
    private Label label;
    private JSlider slider;
    private JSpinner spinner;
    private SliderListener sliderListener;

    public CompositeSlider(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        this.componentAttribute = componentFactory.getComponentAttribute();
        initializeComponents();
        configureLayout();
        addListeners();
    }


    private void initializeComponents() {
        int minValue, maxValue, initialValue;
        List<Integer> values;

        if (componentAttribute.getComponentId().contains("ram")) {
            RamRangeCalculator calculator = new RamRangeCalculator();
            RamRangeCalculator.SliderRange sliderRange = calculator.calculateSliderRange();
            minValue = sliderRange.getMinValue();
            maxValue = sliderRange.getMaxValue();
            initialValue = getInitialValue(sliderRange.getInitialValue());
            values = sliderRange.getValues();
        } else {
            minValue = componentAttribute.getMinValue();
            maxValue = componentAttribute.getMaxValue();
            initialValue = getInitialValue(minValue);
            values = getValues(minValue, maxValue, componentAttribute.getStepSize());
        }

        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid range: minValue (" + minValue + ") must be less than maxValue (" + maxValue + ")");
        }
        if (initialValue < minValue || initialValue > maxValue) {
            throw new IllegalArgumentException("Initial value (" + initialValue + ") must be within range: [" + minValue + ", " + maxValue + "]");
        }

        label = new Label(componentFactory);
        configureLabel();

        slider = new JSlider(minValue, maxValue, initialValue);
        configureSlider(values);

        spinner = new Spinner(initialValue, minValue, maxValue, componentAttribute.getMinorSpacing());
    }
    private int getInitialValue(int defaultValue) {
        try {
            int initialValue = componentAttribute.getInitialValue() != null
                    ? (int) Math.round(Double.parseDouble((String) componentAttribute.getInitialValue()))
                    : defaultValue;

            if (initialValue < componentAttribute.getMinValue() || initialValue > componentAttribute.getMaxValue()) {
                Engine.LOGGER.warn("Initial value (" + initialValue + ") is out of range [" +
                        componentAttribute.getMinValue() + ", " + componentAttribute.getMaxValue() +
                        "]. Using default: " + defaultValue);
                return defaultValue;
            }
            return initialValue;
        } catch (NumberFormatException e) {
            Engine.LOGGER.error("Invalid initial value format, using default: " + e.getMessage());
            return defaultValue;
        }
    }



    private void configureLabel() {
        labelStyle = new LabelStyle(componentFactory.getEngine().getStyleProvider().getElementStyles().get("label").get(componentAttribute.getStyles().get("label")));
        labelStyle.apply(label);
        label.setFont(componentFactory.getEngine().getFONTUTILS().getFont(labelStyle.getFontName(), componentAttribute.getFontSize()));
    }

    private void configureSlider(List<Integer> values) {
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(componentAttribute.getBounds());
        slider.setMajorTickSpacing((values.get(values.size() - 1) - values.get(0)) / 9);
        slider.setMinorTickSpacing((values.get(1) - values.get(0)) / 2);
        slider.setOpaque(false);
        slider.setUI(new TexturedSliderUI(componentFactory, slider, componentFactory.getEngine().getStyleProvider()
                .getElementStyles().get("slider").get(componentAttribute.getStyles().get("slider"))));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int value : values) {
            JLabel tableLabel = new JLabel(String.valueOf(value));
            tableLabel.setFont(this.componentFactory.getEngine().getFONTUTILS().getFont(labelStyle.getFontName(), this.componentAttribute.getFontSize() - 3f));
            tableLabel.setForeground(labelStyle.getActiveColor());
            labelTable.put(value, tableLabel);
        }
        slider.setLabelTable(labelTable);
    }


    private void configureLayout() {
        setLayout(null);
        ComponentAttributes.LayoutConfig config = this.componentAttribute.getLayoutConfig();
            ComponentAttributes.ComponentConfig labelConfig = config.getLabel();
            label.setBounds(labelConfig.getX(), labelConfig.getY(), labelConfig.getWidth(), labelConfig.getHeight());
            add(label);

            ComponentAttributes.ComponentConfig sliderConfig = config.getSlider();
            slider.setBounds(sliderConfig.getX(), sliderConfig.getY(), sliderConfig.getWidth(), sliderConfig.getHeight());
            add(slider);

            ComponentAttributes.ComponentConfig spinnerConfig = config.getSpinner();
            spinner.setBounds(spinnerConfig.getX(), spinnerConfig.getY(), spinnerConfig.getWidth(), spinnerConfig.getHeight());
            add(spinner);

    }

    private void addListeners() {
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                spinner.setValue(slider.getValue());
                notifyListeners();
            }
        });

        spinner.addChangeListener(e -> {
            int newValue = (Integer) spinner.getValue();
            if (newValue >= slider.getMinimum() && newValue <= slider.getMaximum()) {
                slider.setValue(newValue);
                notifyListeners();
            }
        });
    }

    public void setSliderListener(SliderListener sliderListener) {
        this.sliderListener = sliderListener;
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void notifyListeners() {
        if (sliderListener != null) {
            sliderListener.onSliderChange(this);
        }
    }

    public int getValue() {
        return slider.getValue();
    }

    public JSlider getSlider() {
        return slider;
    }

    public Label getLabel() {
        return label;
    }

    public JSpinner getSpinner() {
        return spinner;
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

    private List<Integer> getPowerOfTwoValues(int minValue, int maxValue) {
        List<Integer> values = new ArrayList<>();
        for (int value = minValue; value <= maxValue; value <<= 1) {
            values.add(value);
        }
        return values;
    }

    private SliderRange getSliderRangeBasedOnRam() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalMemorySize();

        int minValue = Math.max(1024, (int) (totalMemory * 0.10 / (1024 * 1024)));
        int maxValue = Math.min(64 * 1024, (int) (totalMemory * 0.75 / (1024 * 1024)));

        minValue = roundUpToPowerOfTwo(minValue);
        maxValue = roundDownToPowerOfTwo(maxValue);

        Engine.LOGGER.info("Calculated RAM range: minValue=" + minValue + ", maxValue=" + maxValue);

        if (maxValue <= minValue) {
            throw new IllegalArgumentException("Invalid range properties: maxValue (" + maxValue + ") must be greater than minValue (" + minValue + ")");
        }

        List<Integer> values = getPowerOfTwoValues(minValue, maxValue);

        int initialValue = Math.min(Math.max(roundToNearestPowerOfTwo((int) (totalMemory * 0.25 / (1024 * 1024))), minValue), maxValue);

        Engine.LOGGER.info("RAM-based initial value: " + initialValue);

        return new SliderRange(minValue, maxValue, initialValue, values);
    }



    public List<Integer> getValues(int minValue, int maxValue, int steps) {
        if (steps < 2) {
            throw new IllegalArgumentException("Steps must be at least 2 to create a range.");
        }

        List<Integer> values = new ArrayList<>();
        double step = (double) (maxValue - minValue) / (steps - 1);

        for (int i = 0; i < steps; i++) {
            int value = minValue + (int) Math.round(i * step);
            values.add(value);
        }

        return values;
    }

    public void setValue(int value){
        this.slider.setValue(value);
    }
}
