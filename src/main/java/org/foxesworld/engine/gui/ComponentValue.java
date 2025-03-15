package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactoryListener;

public abstract class ComponentValue implements ComponentFactoryListener {

    protected Engine engine;
    public ComponentValue(Engine engine){
        this.engine = engine;
    }
    protected abstract void setInitialData(ComponentAttributes componentAttributes);
}
