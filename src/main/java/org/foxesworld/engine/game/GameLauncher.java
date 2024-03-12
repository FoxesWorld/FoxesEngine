package org.foxesworld.engine.game;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.ConfigAbstract;
import org.foxesworld.engine.server.ServerAttributes;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GameLauncher {

    protected GameListener gameListener;
    protected ServerAttributes gameClient;
    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected Engine engine;
    protected Logger logger;
    protected ConfigAbstract config;
    protected int intVer;
    private final String[] toTest = {"_JAVA_OPTIONS", "_JAVA_OPTS", "JAVA_OPTS", "JAVA_OPTIONS"};
    protected URLClassLoader classLoader;
    protected final List<String> processArgs = new ArrayList<>();
    protected boolean isStarted;
    protected abstract void collectLibraries();
    protected abstract URLClassLoader createClassLoader(List<URL> libraryURLs);
    protected abstract void loadAuthLib(String accessToken, String UUID, String userProperties);
    protected abstract void addArgs(String tweakClassVal);
    protected abstract void launchGame();
    protected abstract String addTweakClass();
    public String buildGameDir() {
        return ConfigAbstract.getFullPath();
    }
    protected abstract void setJre();

    public abstract String buildVersionDir();

    public String buildLibrariesPath() {
        return buildVersionDir() + File.separator + "libraries";
    }

    @SuppressWarnings("unused")
    public abstract String buildMinecraftJarPath();

    public String buildNativesPath() {
        return buildVersionDir() + File.separator + "natives";
    }

    public abstract String buildClientDir();

    @SuppressWarnings("unused")
    protected String buildAssetsPath() {
        return buildGameDir() + "assets";
    }

    @SuppressWarnings("unused")
    protected File buildRuntimeDir() {
        File runtimeDir = new File(buildGameDir() + "runtime");
        if (!runtimeDir.isDirectory()) {
            runtimeDir.mkdirs();
        }
        return runtimeDir;
    }

    @SuppressWarnings("unused")
    public abstract String getCurrentJre();

    public Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("unused")
    public void setStarted(boolean started) {
        isStarted = started;
        if (!isStarted) {
            executorService.shutdown();
        }
    }

    @SuppressWarnings("unused")
    protected void checkDangerousParams() {
        for (String t : toTest) {
            String env = System.getenv(t);
            if (env != null) {
                env = env.toLowerCase(Locale.US);
                if (env.contains("-cp") || env.contains("-classpath") || env.contains("-javaagent")
                        || env.contains("-agentpath") || env.contains("-agentlib"))
                    throw new SecurityException("JavaAgent in global options not allow");
            }
        }
    }

    @SuppressWarnings("unused")
    public boolean isStarted() {
        return isStarted;
    }

    @SuppressWarnings("unused")
    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    @SuppressWarnings("unused")
    protected int getIntVer() {
        return intVer;
    }

    public Engine getEngine() {
        return engine;
    }
}