package org.foxesworld.engine.game;

import org.foxesworld.engine.Engine;

public enum ClientType {
    fmlclient,
    forgeclient,
    modified,
    fabricclient;

    @SuppressWarnings("unused")
    public static ClientType getType(String str) {
        if (str != null) {
            for (ClientType clientType : ClientType.values()) {
                if (str.equalsIgnoreCase(clientType.name())) {
                    return clientType;
                }
            }
        }
        Engine.LOGGER.warn("No constant with name " + str + " found for enum ClientType");
        return null;
    }
}
