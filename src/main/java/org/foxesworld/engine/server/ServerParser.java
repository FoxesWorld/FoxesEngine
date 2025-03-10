package org.foxesworld.engine.server;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class ServerParser extends HTTPrequest {
    protected Engine engine;
    protected   int serversNum = 0;
    protected List<ServerAttributes> serverList = new ArrayList<>();

    public ServerParser(Engine engine, String requestMethod) {
        super(engine, requestMethod);
    }
    public  abstract List<ServerAttributes> parseServers(String login);
    public int getServersNum() {
        return serversNum;
    }
}
