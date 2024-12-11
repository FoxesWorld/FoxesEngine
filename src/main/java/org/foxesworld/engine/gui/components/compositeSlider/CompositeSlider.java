package org.foxesworld.engine.gui.components.compositeSlider;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.management.OperatingSystemMXBean;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.label.LabelStyle;
import org.foxesworld.engine.gui.components.slider.TexturedSliderUI;
import org.foxesworld.engine.gui.components.spinner.Spinner;

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
            SliderRange sliderRange = getSliderRangeBasedOnRam();
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

        label = new Label(componentFactory);
        configureLabel();

        slider = new JSlider(minValue, maxValue, initialValue);
        configureSlider(values);

        spinner = new Spinner(initialValue, minValue, maxValue, componentAttribute.getMinorSpacing());
    }

    private int getInitialValue(int defaultValue) {
        return componentAttribute.getInitialValue() != null
                ? Integer.parseInt((String) componentAttribute.getInitialValue())
                : defaultValue;
    }

    private void configureLabel() {
        labelStyle = new LabelStyle(componentFactory.engine.getStyleProvider()
                .getElementStyles().get("label").get(componentAttribute.getStyles().get("label")));
        labelStyle.apply(label);
        label.setFont(componentFactory.engine.getFONTUTILS().getFont(labelStyle.fontName, componentAttribute.getFontSize()));
    }

    private void configureSlider(List<Integer> values) {
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(componentAttribute.getBounds());
        slider.setMajorTickSpacing((values.get(values.size() - 1) - values.get(0)) / 9);
        slider.setMinorTickSpacing((values.get(1) - values.get(0)) / 2);
        slider.setOpaque(false);
        slider.setUI(new TexturedSliderUI(componentFactory, slider, componentFactory.engine.getStyleProvider()
                .getElementStyles().get("slider").get(componentAttribute.getStyles().get("slider"))));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int value : values) {
            JLabel tableLabel = new JLabel(String.valueOf(value));
            tableLabel.setFont(this.componentFactory.engine.getFONTUTILS().getFont(labelStyle.fontName, this.componentAttribute.getFontSize()));
            tableLabel.setForeground(labelStyle.activeColor);
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

    private SliderRange getSliderRangeBasedOnRam() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalMemorySize();

        int minValue = Math.max(1024, (int) (totalMemory * 0.10 / (1024 * 1024)));
        int maxValue = Math.min(64 * 1024, (int) (totalMemory * 0.75 / (1024 * 1024)));

        if (maxValue <= minValue) {
            throw new IllegalArgumentException("Invalid range properties: maxValue must be greater than minValue");
        }

        List<Integer> values = getValues(minValue, maxValue, componentAttribute.getStepSize());
        int initialValue = Math.min(Math.max((int) (totalMemory * 0.25 / (1024 * 1024)), minValue), maxValue);

        return new SliderRange(minValue, maxValue, initialValue, values);
    }

    private List<Integer> getValues(int minValue, int maxValue, int steps) {
        int step = (maxValue - minValue) / steps;
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            values.add(minValue + i * step);
        }
        values.set(values.size() - 1, maxValue);
        return values;
    }
}
