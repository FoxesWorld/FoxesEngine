package org.foxesworld.engine.gui;

import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactoryListener;

public abstract class ComponentValue implements ComponentFactoryListener {
    public abstract void setInitialData(ComponentAttributes componentAttributes);
}
