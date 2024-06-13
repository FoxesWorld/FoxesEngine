package org.foxesworld.engine.gui.components.slider;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;

@SuppressWarnings("unused")
public class Slider extends JSlider {

    private SliderListener sliderListener;
    private ComponentFactory componentFactory;

    public Slider(ComponentFactory componentFactory){
        super(componentFactory.getComponentAttribute().getMinValue(), componentFactory.getComponentAttribute().getMaxValue());
        this.componentFactory = componentFactory;
    }

    public void setSliderListener(SliderListener sliderListener) {
        this.sliderListener = sliderListener;
        this.addChangeListener(e -> sliderListener.onSliderChange(this));
    }
}
