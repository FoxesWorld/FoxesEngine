package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.command.DynamicCommandRegistry;
import org.foxesworld.engine.gui.componentAccessor.ComponentsAccessor;
import org.foxesworld.engine.server.ServerAttributes;

import java.awt.event.ActionEvent;
import java.util.List;

public abstract class ActionHandler extends ComponentsAccessor implements DynamicCommandRegistry {
    protected Engine engine;
    protected ServerAttributes currentServer;

    public ActionHandler(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        super(guiBuilder, panelId, componentTypes);
    }

    @SuppressWarnings("unused")
    public abstract void handleAction(ActionEvent e);
    public Engine getEngine() {
        return engine;
    }
    public ServerAttributes getCurrentServer() {
        return currentServer;
    }
}
