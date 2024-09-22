package org.foxesworld.engine.user;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.ComponentsAccessor;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class User extends ComponentsAccessor {

    protected Engine engine;

    public User(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        super(guiBuilder, panelId, componentTypes);
    }

    @SuppressWarnings("unused")
    protected abstract void setUserSpace();

    @SuppressWarnings("unused")
    protected String getUserHead(String login) {
        Map<String, String> skinData = new HashMap<>();
        skinData.put("sysRequest", "skin");
        skinData.put("show", "head");
        skinData.put("login", login);
        return this.engine.getPOSTrequest().send(skinData);
    }

}
