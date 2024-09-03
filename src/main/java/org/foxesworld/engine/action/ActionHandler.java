package org.foxesworld.engine.action;

import org.foxesworld.engine.Engine;

import java.awt.event.ActionEvent;

public class ActionHandler {
    private Engine engine;

    public ActionHandler(Engine engine) {
        this.engine = engine;
    }

    public void handleAction(ActionEvent e) {
        String key = e.getActionCommand();
        String parent = "";
        if (e.getActionCommand().contains(">")) {
            String[] command = e.getActionCommand().split(">");
            key = command[1];
            parent = command[0];
        }
        switch (key) {


            case "closeButton": System.exit(0);
            case "hideButton":  engine.getFrame().getFrame().setExtendedState(1);
        }
    }
}
