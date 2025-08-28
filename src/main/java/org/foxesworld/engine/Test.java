package org.foxesworld.engine;

import org.foxesworld.engine.gui.ComponentValue;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactoryListener;
import org.foxesworld.engine.gui.components.button.Button;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.engine.gui.components.multiButton.MultiButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Test extends Engine {

    public Test(int poolSize, String worker, Map<String, Class<?>> configFiles) {
        super(poolSize, worker, configFiles);
    }

    public static void main(String[] args){
        new Test(10, "forge", null);
    }

    @Override
    public void init() {
        safeSubmitTask(() -> {
            buildGui(new InitialValue(this));
            loadMainPanel(fileProperties.getMainFrame());
        }, "init");
    }

    @Override
    protected void preInit() {
        System.setProperty("AppDir", System.getenv("APPDATA"));
        System.setProperty("RamAmount", String.valueOf(Runtime.getRuntime().maxMemory() / 45));

        this.frameConstructor.setFocusStatusListener(this);
    }

    @Override
    protected void postInit() {
        setActionHandler(new ActionHandler(this.getGuiBuilder(), "mainFrame", List.of(MultiButton.class, Button.class)));
    }

    @Override
    public void onPanelsBuilt() {
        this.getFrame().repaint();
    }

    @Override
    public void onAdditionalPanelBuild(JPanel panel) {

    }

    @Override
    public void onGuiBuilt() {
    }

    @Override
    public void onPanelBuild(Map<String, OptionGroups> panels, String componentGroup, Container parentPanel) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionHandler.handleAction(e);
    }

    @Override
    public void updateFocus(boolean hasFocus) {

    }

    // INTERNAL CLASSES
    static class InitialValue extends ComponentValue implements ComponentFactoryListener {

        private int count;
        private final Test test;
        public InitialValue(Test test) {
            super(test);
            this.test = test;
        }

        @Override
        public void onComponentCreation(ComponentAttributes componentAttributes) {
            if (componentAttributes.getInitialValue() != null) {
                this.setInitialData(componentAttributes);
            }
            count+=1;
        }

        @Override
        public void setInitialData(ComponentAttributes componentAttributes) {
            String[] splitValue = String.valueOf(componentAttributes.getInitialValue()).split("#");
            switch (splitValue[0]) {
                case "version" -> componentAttributes.setInitialValue(this.test.getEngineData().getLauncherVersion());
                case "build" -> componentAttributes.setInitialValue(this.test.getEngineData().getLauncherBuild());
            }
        }
    }

    static class ActionHandler extends org.foxesworld.engine.gui.ActionHandler {

        public ActionHandler(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
            super(guiBuilder, panelId, componentTypes);

        }

        @Override
        public void handleAction(ActionEvent e) {
            switch (e.getActionCommand()){
                case "closeButton":
                    System.exit(0);
                    break;

                case "hideButton":
                    engine.getFrame().setExtendedState(Frame.ICONIFIED);
                    break;
            }
        }

        @Override
        public void registerCommand(String key, Consumer<ActionEvent> command) {

        }

        @Override
        public void unregisterCommand(String key) {

        }

        @Override
        public void executeCommand(String key, ActionEvent event) {
        }
    }
}
