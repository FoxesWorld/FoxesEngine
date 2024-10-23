package org.foxesworld.engine.user;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.componentAccessor.ComponentsAccessor;
import org.foxesworld.engine.gui.GuiBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class User extends ComponentsAccessor {

    protected Engine engine;

    public User(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        super(guiBuilder, panelId, componentTypes);
    }

    protected abstract void setUserSpace();

    protected String getUserHead(String login) {
        if (login == null || login.isEmpty()) {
            Engine.getLOGGER().warn("Login is null or empty in getUserHead");
            return null;
        }

        Map<String, Object> skinData = new HashMap<>();
        skinData.put("sysRequest", "skin");
        skinData.put("show", "head");
        skinData.put("login", login);

        String response = null;
        try {
            response = this.engine.getPOSTrequest().send(skinData);
            if (response == null || response.isEmpty()) {
                Engine.getLOGGER().warn("Received empty or null response for user head request for login: {}", login);
            }
        } catch (Exception e) {
            Engine.getLOGGER().error("Error while sending user head request for login: {}", login, e);
        }

        return response;
    }


}
