package org.foxesworld.engine;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.discord.Discord;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.GuiBuilderListener;
import org.foxesworld.engine.gui.FileProperties;
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
import org.foxesworld.engine.utils.ImageUtils;
import org.foxesworld.engine.gui.loadingManager.LoadingManager;
import org.foxesworld.engine.utils.OS;
import org.foxesworld.engine.utils.ServerInfo;
import org.foxesworld.engine.gui.ActionHandler;
import org.fusesource.jansi.AnsiConsole;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class Engine extends JFrame implements ActionListener, GuiBuilderListener {
    private final FileProperties fileProperties;
    private final OperatingSystemMXBean osBean;

    public static String currentOS = "";
    protected LoadingManager loadingManager;
    private final List<String> configFiles;
    private final String appTitle;
    protected Sound SOUND;
    protected Config config;
    protected LanguageProvider LANG;
    protected ServerInfo serverInfo;
    protected ImageUtils imageUtils;
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
    private final EngineInfo engineInfo;

    public Engine(List<String> configFiles) {
        currentOS = OS.determineCurrentOS();
        osBean = ManagementFactory.getOperatingSystemMXBean();
        this.engineData = new EngineData();
        this.configFiles = configFiles;
        InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("buildInfo.json"), StandardCharsets.UTF_8);
        this.engineInfo = new Gson().fromJson(reader, EngineInfo.class);
        setEngineData(engineData.initEngineValues("engine.json"));
        fileProperties = new FileProperties(this);
        System.setProperty("log.dir", System.getProperty("user.dir"));
        LOGGER = LogManager.getLogger(this.getClass());
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
        appTitle = engineData.getLauncherBrand() + '-' + engineData.getLauncherVersion();
        this.panelVisibility = new PanelVisibility(this);
        LOGGER.info(appTitle + " started...");

        this.FONTUTILS = new FontUtils(this);
        Configurator.setLevel(getLOGGER().getName(), Level.valueOf(engineData.getLogLevel()));
        LOGGER.info("Engine version " + this.getEngineInfo().engineVersion + this.getEngineInfo().engineBrand);
        this.GETrequest = new HTTPrequest(this, "GET");
        this.POSTrequest = new HTTPrequest(this, "POST");
        this.imageUtils = new ImageUtils(this);
        FlatIntelliJLaf.setup();
    }

    public abstract void init();
    protected abstract void preInit();
    protected abstract void postInit();
    @Override
    public abstract void onPanelsBuilt();
    @Override
    public abstract void onPanelBuild(Map<String, OptionGroups> panels, String componentGroup, JPanel parentPanel);
    @Override
    public abstract void actionPerformed(ActionEvent e);
    protected void loadMainPanel(String path) {
        this.guiBuilder.buildGui(path, this.getFrame().getRootPanel());
        if (!init) {
            this.postInit();
        }
    }
    public String appPath() {
        try {
            return URLDecoder.decode(this.POSTrequest.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath(),StandardCharsets.UTF_8);
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }

    public void restartApplication(int xmx, String jvmDir) {
        System.gc();
        Runtime.getRuntime().addShutdownHook(new Thread(this.guiBuilder.getComponentFactory().getCustomTooltip()::clearAllTooltips));
        String path = Config.getFullPath();
        List<String> params = new LinkedList<>();
        params.add(path + "/runtime/"+ jvmDir + "/bin/java");
        params.add("-Xmx"+xmx+"M");
        params.add("-jar");
        params.add(appPath().substring(1));

        ProcessBuilder builder = new ProcessBuilder(params);
        builder.redirectErrorStream(true);
        builder.directory(new File(path + File.separator));
        try {
            builder.start();
            terminateAllThreads();
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Restart Error occurred \n PLease try again" + e, "Restart Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void terminateAllThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);

        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo != null && threadInfo.getThreadId() != Thread.currentThread().getId()) {
                Thread thread = findThread(threadInfo.getThreadId());
                if (thread != null && thread != Thread.currentThread()) {
                    thread.interrupt();
                }
            }
        }
    }

    private Thread findThread(long threadId) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getId() == threadId) {
                return thread;
            }
        }
        return null;
    }


    public void showDialog(String messageKey, String errorTitle, int warningMessage, boolean terminate) {
        SwingUtilities.invokeLater(() -> {
            String errorMessage = this.getLANG().getString(messageKey);
            this.getSOUND().playSound("other", messageKey);
            UIManager.put("OptionPane.messageFont", this.getFONTUTILS().getFont("mcfont", 12.0F));
            JOptionPane.showMessageDialog(this.getFrame().getRootPane(), errorMessage, errorTitle, warningMessage);
            if (terminate) {
                System.exit(0);
            }
        });
    }


    private static class EngineInfo {
        private String engineVersion, engineBrand;

        public String getEngineVersion() {
            return engineVersion;
        }

        public String getEngineBrand() {
            return engineBrand;
        }
    }

    public List<String> getConfigFiles() {
        return configFiles;
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
    public FileProperties getFileProperties() {
        return fileProperties;
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
    public ImageUtils getImageUtils() {
        return imageUtils;
    }
    public CryptUtils getCRYPTO() {
        return CRYPTO;
    }
    public Config getConfig() {
        return config;
    }
    public EngineInfo getEngineInfo(){
        return this.engineInfo;
    }

    public OperatingSystemMXBean getOsBean() {
        return osBean;
    }
}