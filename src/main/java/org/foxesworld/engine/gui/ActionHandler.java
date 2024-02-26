package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;

import java.awt.event.ActionEvent;

public  abstract class ActionHandler {
    protected Engine engine;

    public abstract void handleAction(ActionEvent e);
    public Engine getEngine() {
        return engine;
    }
}
