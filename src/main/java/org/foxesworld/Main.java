package org.foxesworld;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.engine.gui.styles.StyleProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

public class Main  extends Engine {

    public static void main(String[] args){
        SwingUtilities.invokeLater(Main::new);
    }

    public Main(){
        super("config");
        init();
    }

    @Override
    public void init() {
        setStyleProvider(new StyleProvider(new String[]{"button"}));
        setGuiBuilder(new GuiBuilder(this));
        //this.getGuiBuilder().getComponentFactory().setComponentFactoryListener(new Components(this));
        getGuiBuilder().setGuiBuilderListener(this);
        //getGuiBuilder().getComponentFactory().setComponentFactoryListener(getGuiBuilder());
        this.getGuiBuilder().buildGui(this.getFileProperties().getFrameTpl(), this.getFrame().getRootPanel());
        loadMainPanel(this.getFileProperties().getMainFrame());

        //ALL PANELS ARE BUILT
        this.getGuiBuilder().buildAdditionalPanels();
        setInit(true);
    }

    @Override
    protected void preInit() {

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
