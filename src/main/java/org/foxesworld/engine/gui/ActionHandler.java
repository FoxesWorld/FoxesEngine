package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.server.ServerAttributes;

import java.awt.event.ActionEvent;

public  abstract class ActionHandler {
    protected Engine engine;
    protected ServerAttributes currentServer;
    @SuppressWarnings("unused")
    public abstract void handleAction(ActionEvent e);
    public Engine getEngine() {
        return engine;
    }
    public ServerAttributes getCurrentServer() {
        return currentServer;
    }
}
