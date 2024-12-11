package org.foxesworld.engine.gui.components.serverBox;

import org.foxesworld.engine.gui.components.ComponentFactory;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class ServerBoxStyle {
    private  ComponentFactory componentFactory;

    public ServerBoxStyle(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    public void apply(ServerBox serverBox) {
        serverBox.setFont(componentFactory.getEngine().getFONTUTILS().getFont(componentFactory.getStyle().getFont(), componentFactory.getStyle().getFontSize()));
        serverBox.setBackground(hexToColor(componentFactory.getStyle().getColor()));
        serverBox.setForeground(hexToColor(componentFactory.getStyle().getColor()));
        serverBox.sb = this;
    }
}