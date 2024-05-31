package org.foxesworld.engine.game;

import org.foxesworld.engine.server.ServerAttributes;

@SuppressWarnings("unused")
public interface GameListener {
    void onGameStart(ServerAttributes serverAttributes);
    void onGameExit(ServerAttributes serverAttributes);
}
