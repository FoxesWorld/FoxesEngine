package org.foxesworld.engine.gui.components.slider;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;

public class Slider extends JSlider {
    @SuppressWarnings("unused")
    private SliderListener sliderListener;
    @SuppressWarnings("unused")
    private ComponentFactory componentFactory;

    public Slider(ComponentFactory componentFactory){
        super(componentFactory.getComponentAttribute().getMinValue(), componentFactory.getComponentAttribute().getMaxValue());
        this.componentFactory = componentFactory;
    }

    @SuppressWarnings("unused")
    public void setSliderListener(SliderListener sliderListener) {
        this.sliderListener = sliderListener;
        this.addChangeListener(e -> sliderListener.onSliderChange(this.getValue()));
    }
}
