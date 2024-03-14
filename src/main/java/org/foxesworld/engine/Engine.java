package org.foxesworld.engine;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.discord.Discord;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.GuiBuilderListener;
import org.foxesworld.engine.gui.GuiProperties;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.engine.gui.components.panel.PanelVisibility;
import org.foxesworld.engine.gui.styles.StyleProvider;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.news.News;
import org.foxesworld.engine.sound.Sound;
import org.foxesworld.engine.utils.Crypt.CryptUtils;
import org.foxesworld.engine.utils.FontUtils;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;
import org.foxesworld.engine.utils.LoadingManager;
import org.foxesworld.engine.utils.ServerInfo;
import org.foxesworld.engine.gui.ActionHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public abstract class Engine extends JFrame implements ActionListener, GuiBuilderListener {
    private final GuiProperties guiProperties;
    protected LoadingManager loadingManager;
    private final String configFiles;
    private final String appTitle;
    protected Sound SOUND;
    protected Config config;
    protected LanguageProvider LANG;
    protected ServerInfo serverInfo;
    private News news;
    public static Logger LOGGER;
    protected Discord discord;
    private final FontUtils FONTUTILS;
    protected CryptUtils CRYPTO;
    protected FrameConstructor frameConstructor;
    private final PanelVisibility panelVisibility;
    private GuiBuilder guiBuilder;
    private StyleProvider styleProvider;
    private EngineData engineData;
    private final HTTPrequest GETrequest, POSTrequest;
    public ActionHandler actionHandler;
    private boolean init = false;
    public Engine(String configFiles) {
        this.engineData = new EngineData();
        this.configFiles = configFiles;
        setEngineData(engineData.initEngineValues("engine.json"));
        guiProperties = new GuiProperties(this);
        System.setProperty("log.dir", System.getProperty("user.dir"));
        LOGGER = LogManager.getLogger(Engine.class);
        appTitle = engineData.getLauncherBrand() + '-' + engineData.getLauncherVersion();
        this.panelVisibility = new PanelVisibility(this);
        LOGGER.info(appTitle + " started...");

        this.FONTUTILS = new FontUtils(this);
        //this.discord = new Discord(this);
        Configurator.setLevel(getLOGGER().getName(), Level.valueOf(engineData.getLogLevel()));

        this.GETrequest = new HTTPrequest(this, "GET");
        this.POSTrequest = new HTTPrequest(this, "POST");
    }

    public abstract void init(Engine engine);
    protected abstract void preInit(Engine engine);
    @Override
    public abstract void onPanelsBuilt();
    @Override
    public abstract void onPanelBuild(Map<String, OptionGroups> groups, String componentGroup, JPanel parentPanel);
    @Override
    public abstract void actionPerformed(ActionEvent e);
    protected void loadMainPanel(String path) {
        this.guiBuilder.buildGui(path, this.getFrame().getRootPanel());
    }
    public String appPath() {
        try {
            return URLDecoder.decode(HTTPrequest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(),StandardCharsets.UTF_8);
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public void restartApplication(int xmx) {
        String path = Config.getFullPath();
        ArrayList params = new ArrayList();
        params.add(path + "/runtime/"+ this.getEngineData().getProgramRuntime() + "/bin/java");
        params.add("-Xmx"+xmx+"M");
        params.add("-jar");
        params.add(appPath().substring(1));

        ProcessBuilder builder = new ProcessBuilder(params);
        builder.redirectErrorStream(true);
        builder.directory(new File(path + File.separator));
        try {
            builder.start();
            System.exit(-1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Restart Error occured \n PLease try again" + e, "Restart Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public String[] getConfigFiles() {
        return configFiles.split(",");
    }
    protected boolean isInit() {
        return init;
    }
    public FrameConstructor getFrame() {
        return this.frameConstructor;
    }
    public GuiBuilder getGuiBuilder() {
        return guiBuilder;
    }
    public HTTPrequest getGETrequest() {
        return GETrequest;
    }
    public HTTPrequest getPOSTrequest() {
        return POSTrequest;
    }
    public static Logger getLOGGER() {
        return LOGGER;
    }
    public LanguageProvider getLANG() {
        return LANG;
    }
    public FontUtils getFONTUTILS() {
        return FONTUTILS;
    }
    public StyleProvider getStyleProvider() {
        return styleProvider;
    }
    public Sound getSOUND() {
        return SOUND;
    }
    public EngineData getEngineData() {
        return engineData;
    }
    public void setStyleProvider(StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
    }
    public void setEngineData(EngineData engineData) {
        this.engineData = engineData;
    }
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    public Discord getDiscord() {
        return discord;
    }
    public String getAppTitle() {
        return appTitle;
    }
    public PanelVisibility getPanelVisibility() {
        return panelVisibility;
    }
    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }
    public void setGuiBuilder(GuiBuilder guiBuilder) {
        this.guiBuilder = guiBuilder;
    }
    public void setInit(boolean init) {
        this.init = init;
    }
    public GuiProperties getGuiProperties() {
        return guiProperties;
    }
    public LoadingManager getLoadingManager() {
        return loadingManager;
    }
    public void setNews(News news) {
        this.news = news;
    }
    public News getNews() {
        return news;
    }

    public CryptUtils getCRYPTO() {
        return CRYPTO;
    }
    public Config getConfig() {
        return config;
    }
}