package org.foxesworld.engine.server;

import org.foxesworld.engine.Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ServerParser {
    protected Engine engine;
    protected   int serversNum = 0;
    protected List<ServerAttributes> serverList = new ArrayList<>();
    protected Map<String, String> request = new HashMap<>();

    public  abstract List<ServerAttributes> parseServers(String login);

    public int getServersNum() {
        return serversNum;
    }
}
