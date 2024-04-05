package org.foxesworld.engine.user;

import org.foxesworld.engine.Engine;

import java.util.HashMap;
import java.util.Map;

public abstract class User {

    protected Engine engine;

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
