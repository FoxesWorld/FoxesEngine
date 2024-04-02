package org.foxesworld.engine.user;

import org.foxesworld.engine.Engine;

import java.util.HashMap;
import java.util.Map;

public abstract class User {

    protected Engine engine;

    protected abstract void setUserSpace();

    protected String getUserHead(String login) {
        Map<String, String> skinData = new HashMap<>();
        skinData.put("sysRequest", "skin");
        skinData.put("show", "head");
        skinData.put("login", login);
        return this.engine.getPOSTrequest().send(this.engine.getEngineData().getBindUrl(), skinData);
    }
}
