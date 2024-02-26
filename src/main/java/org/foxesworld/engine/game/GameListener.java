package org.foxesworld.engine.game;


import org.foxesworld.engine.server.ServerAttributes;

public interface GameListener {
    void onGameStart(ServerAttributes server);
    void onGameExit(int exitCode);
}
