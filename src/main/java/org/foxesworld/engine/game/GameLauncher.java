package org.foxesworld.engine.game;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.server.ServerAttributes;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GameLauncher {

    protected GameListener gameListener;
    protected ServerAttributes gameClient;
    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected Engine engine;
    protected Logger logger;
    protected Config config;
    protected int intVer;
    protected URLClassLoader classLoader;
    protected final List<String> processArgs = new ArrayList<>();
    protected boolean isStarted;
    protected abstract void collectLibraries();
    protected abstract URLClassLoader createClassLoader(List<URL> libraryURLs);
    protected abstract void loadAuthLib();
    protected abstract void addArgs(String tweakClassVal);
    protected abstract void launchGame();
    protected abstract String addTweakClass();
    public String buildGameDir() {
        return config.getFullPath();
    }
    protected abstract void setJre();

    public abstract String buildVersionDir();

    public String buildLibrariesPath() {
        return buildVersionDir() + File.separator + "libraries";
    }

    public abstract String buildMinecraftJarPath();

    public String buildNativesPath() {
        return buildVersionDir() + File.separator + "natives";
    }

    public abstract String buildClientDir();

    protected String buildAssetsPath() {
        return buildGameDir() + "assets";
    }

    protected File buildRuntimeDir() {
        File runtimeDir = new File(buildGameDir() + "runtime");
        if (!runtimeDir.isDirectory()) {
            runtimeDir.mkdirs();
        }
        return runtimeDir;
    }

    public abstract String getCurrentJre();

    public Logger getLogger() {
        return logger;
    }

    public void setStarted(boolean started) {
        isStarted = started;
        if (!isStarted) {
            executorService.shutdown();
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    protected int getIntVer() {
        return intVer;
    }

    public Engine getEngine() {
        return engine;
    }
}