package org.foxesworld;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.frame.OptionGroups;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

public class Main  extends Engine {

    public static void main(String[] args){
        SwingUtilities.invokeLater(Main::new);
    }

    public Main(){
        super("config");
        initialize(this);
    }

    @Override
    public void initialize(Engine engine) {
        //setStyleProvider(new StyleProvider(this));
        setGuiBuilder(new GuiBuilder(this));
        //this.getGuiBuilder().getComponentFactory().setComponentFactoryListener(new Components(this));
        getGuiBuilder().setGuiBuilderListener(this);
        //getGuiBuilder().getComponentFactory().setComponentFactoryListener(getGuiBuilder());
        this.getGuiBuilder().buildGui(this.getGuiProperties().getFrameTpl(), this.getFrame().getRootPanel());
        loadMainPanel(this.getGuiProperties().getMainFrame());

        //ALL PANELS ARE BUILT
        this.getGuiBuilder().buildAdditionalPanels();
        setInit(true);
    }

    @Override
    public void onPanelsBuilt() {

    }

    @Override
    public void onPanelBuild(Map<String, OptionGroups> groups, String componentGroup, JPanel parentPanel) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
