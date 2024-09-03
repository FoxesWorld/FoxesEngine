package org.foxesworld.engine;

import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.foxesworld.cfgProvider.CfgProvider;
import org.foxesworld.engine.action.ActionHandler;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.SystemComponents;
import org.foxesworld.engine.gui.components.frame.Frame;
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.gui.styles.StyleProvider;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.FontUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Engine extends JFrame implements ActionListener {

    protected final String app;
    private final Logger LOGGER;
    private EngineData engineData;
    private GuiBuilder guiBuilder;
    private StyleProvider styleProvider;
    private final String LOCALE;
    private final LanguageProvider LANG;
    private final Config config;
    private final Map<String, Object> CONFIG;
    private final FontUtils fontUtils;
    private SystemComponents systemComponents;
    private ActionHandler actionHandler;
    private Map<String, Map<String, StyleAttributes>> elementStyles = new HashMap<>();
    private final Frame frame;
    //private DownloadUtils download;
    //private Updater updater;
    private final String[] configFiles = new String[]{ "internal/config"};
    private boolean init = false;

    public Engine(String app) {
        this.app = app;
        this.engineData = new EngineData();
        initEngineValues("engine.json");
        System.setProperty("log.dir", CfgProvider.getGameFullPath());
        LOGGER = LogManager.getLogger(Engine.class);
        LOGGER.info("Started "+engineData.getUpdaterBrand()+'-'+engineData.getUpdaterVersion());
        Configurator.setLevel(LOGGER.getName(), Level.valueOf("DEBUG"));
        this.config = new Config(this);
        CONFIG = config.getCONFIG();
        LOCALE = String.valueOf(CONFIG.get("lang"));
        this.LANG = new LanguageProvider(this, "/assets/lang/locale.json");
        this.fontUtils = new FontUtils(this);

        this.frame = new Frame(this);
        initialize();
    }

    private void initialize() {
        styleProvider = new StyleProvider(this);
        this.elementStyles = styleProvider.getElementStyles();
        this.guiBuilder = new GuiBuilder(this);
        getGuiBuilder().buildGui("assets/frames/frame.json", true, this.getFrame().getRootPanel());
        this.loadMainPanel(this.app);
        this.actionHandler = new ActionHandler(this);
        //this.download = new DownloadUtils(this);
        //this.updater = new Updater(this);
        init = true;
    }

    void initEngineValues(String propertyPath){
        InputStream inputStream = Engine.class.getClassLoader().getResourceAsStream(propertyPath);
        if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            setEngineData(new Gson().fromJson(reader, EngineData.class));
        }
    }

    public void displayPanel(String displayString) {
        String[] panelElements = displayString.split("\\|");
        if (panelElements.length <= 1) {
            this.processSinglePanelDisplay(displayString);
        } else {
            for (String panelElement : panelElements) {
                this.processSinglePanelDisplay(panelElement);
            }
        }
    }

    private void processSinglePanelDisplay(String panelElement){
        String[] parts = panelElement.split("->");
        if (parts.length == 2) {
            String panelName = parts[0];
            boolean displayValue = Boolean.parseBoolean(parts[1]);

            JPanel groupPanel = guiBuilder.getPanelsMap().get(panelName);
            groupPanel.setVisible(displayValue);
            getLOGGER().debug("Setting " + panelName + " visible to " + displayValue);
        }
    }

    private void loadMainPanel(String path) {
        this.guiBuilder.buildGui(path, true, this.getFrame().getRootPanel());
        this.processComponents();
    }

    private void processComponents(){
        List<String> systemIds = Arrays.asList("PBar", "progressLabel"); //ComponentFactory we define as system
        this.systemComponents = new SystemComponents();
        for(Map.Entry<String, List<Component>> panels: guiBuilder.getComponentsMap().entrySet()){
            String panelName = panels.getKey();
            for(Component component: panels.getValue()){
                if(systemIds.contains(component.getName())){
                    this.systemComponents.addComponent(component.getName(), component);
                    getLOGGER().debug("Adding system component '" + component.getName()+"'");
                }
                this.setComponentValues(component);
            }
        }
    }

    private void setComponentValues(Component component){
        if(component instanceof  JLabel){
            String text = ((JLabel) component).getText();
        } else {
            if(component instanceof  JCheckBox) {
                if(component.isEnabled()){
                    ((JCheckBox) component).setSelected((Boolean) CONFIG.get(component.getName()));
                }
            }
        }
    }

    public Map<String, Map<String, StyleAttributes>> getElementStyles() {
        return elementStyles;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.actionHandler.handleAction(e);
    }
    public Frame getFrame() {
        return this.frame;
    }
    public SystemComponents getSystemComponents() {
        return systemComponents;
    }
    public GuiBuilder getGuiBuilder() {
        return guiBuilder;
    }

    public EngineData getEngineData() {
        return engineData;
    }
    public Logger getLOGGER() {
        return LOGGER;
    }
    public LanguageProvider getLANG() {
        return LANG;
    }
    public Map<String, Object> getCONFIG() {
        return CONFIG;
    }
    public String getLOCALE() {
        return LOCALE;
    }
    public FontUtils getFontUtils() {
        return fontUtils;
    }
    public String[] getConfigFiles() {
        return configFiles;
    }
    public Config getConfig() {
        return config;
    }
    public void setEngineData(EngineData engineData) {
        this.engineData = engineData;
    }
    public StyleProvider getStyleProvider() {
        return styleProvider;
    }
}