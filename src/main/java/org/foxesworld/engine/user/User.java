package org.foxesworld.engine.user;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.componentAccessor.ComponentsAccessor;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;
import org.foxesworld.engine.utils.HTTP.OnFailure;
import org.foxesworld.engine.utils.HTTP.OnSuccess;

import java.io.IOException;
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

    protected void getUserHeadAsync(String login, OnSuccess<String> onSuccess, OnFailure onFailure) {
        if (login == null || login.isEmpty()) {
            Engine.getLOGGER().warn("Login is null or empty in getUserHead");
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Login cannot be null or empty"));
            }
            return;
        }

        Map<String, Object> skinData = new HashMap<>();
        skinData.put("sysRequest", "skin");
        skinData.put("show", "head");
        skinData.put("login", login);

        HTTPrequest httpRequest = new HTTPrequest(engine, "POST");
        httpRequest.sendAsync(skinData, response -> {
            if (response == null || response.toString().isEmpty()) {
                Engine.getLOGGER().warn("Received empty or null response for user head request for login: {}", login);
                if (onFailure != null) {
                    onFailure.onFailure(new Exception("Received empty or null response"));
                }
            } else {
                onSuccess.onSuccess((String) response);
            }
        }, e -> {
            Engine.getLOGGER().error("Error while sending user head request for login: {}", login, e);
            if (onFailure != null) {
                onFailure.onFailure(e);
            }
        });
    }
}
