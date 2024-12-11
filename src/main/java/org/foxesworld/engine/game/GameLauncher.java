package org.foxesworld.engine.game;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.game.argsReader.ArgsReader;
import org.foxesworld.engine.server.ServerAttributes;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class GameLauncher {
    protected GameListener gameListener;
    protected ServerAttributes gameClient;
    protected Engine engine;
    protected Logger logger;
    protected ArgsReader argsReader;
    protected Config config;
    protected pathBuilders pathBuilders;
    protected int intVer;
    private final String[] toTest = {"_JAVA_OPTIONS", "_JAVA_OPTS", "JAVA_OPTS", "JAVA_OPTIONS"};
    protected URLClassLoader classLoader;
    protected final List<String> processArgs = new ArrayList<>();
    public void createClassLoader(List<URL> libraryURLs) {
        URL[] urls = libraryURLs.toArray(new URL[0]);
        this.classLoader = new URLClassLoader(urls, getClass().getClassLoader());
    }
    protected abstract void setJreArgs();
    protected abstract void setGameArgs();
    protected abstract String addTweakClass();
    protected abstract void launchGame();
    public Logger getLogger() {
        return logger;
    }

    protected  void printDebug(){
        this.getLogger().debug("#############################");
        this.logger.debug("GameDir " + getPathBuilders().buildGameDir());
        this.logger.debug("ClientDir " + getPathBuilders().buildClientDir());
        this.logger.debug("VersionsDir " + getPathBuilders().buildVersionDir());
        this.logger.debug("JarFile " + getPathBuilders().buildMinecraftJarPath());
        this.logger.debug("Natives " + getPathBuilders().buildNativesPath());
        this.logger.debug("Libraries " + getPathBuilders().buildLibrariesPath());
        this.logger.debug("Assets " + getPathBuilders().buildAssetsPath());
        this.logger.debug("#############################");
    }
    protected void checkDangerousParams() {
        for (String t : toTest) {
            String env = System.getenv(t);
            if (env != null) {
                env = env.toLowerCase(Locale.US);
                if (env.contains("-cp") || env.contains("-classpath") || env.contains("-javaagent")
                        || env.contains("-agentpath") || env.contains("-agentlib")) {
                    throw new SecurityException("JavaAgent in global options not allowed!");
                }
            }
        }
    }
    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }
    protected int getIntVer() {
        return intVer;
    }
    public String getCurrentJre() {
        return this.gameClient.getJreVersion();
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public List<String> getProcessArgs() {
        return processArgs;
    }

    public Engine getEngine() {
        return engine;
    }

    protected void addArgsToProcess(List<String> args){
        processArgs.addAll(args);
    }

    protected String getVersion(){
        String version = gameClient.getServerVersion();
        if (gameClient.getServerVersion().contains("-")) {
            version = gameClient.getServerVersion().split("-")[0];
        }
        return  version;
    }

    public static class pathBuilders {
        private  GameLauncher gameLauncher;
        public pathBuilders(GameLauncher gameLauncher){
            this.gameLauncher = gameLauncher;
        }
        public String buildGameDir() {
            return Config.getFullPath();
        }

        public String buildVersionDir() {
            return buildGameDir() + "versions" + File.separator + this.gameLauncher.gameClient.getServerVersion();
        }
        public String getArgsFile() {
            return this.buildVersionDir() + File.separator + this.gameLauncher.gameClient.getServerVersion() + ".json";
        }
        public String buildLibrariesPath() {
            return buildGameDir() + "libraries";
        }
        public String buildNativesPath() {
            return buildVersionDir() + File.separator + "natives";
        }

        public String buildAssetsPath() {
            return buildGameDir() + "assets";
        }
        public String buildMinecraftJarPath() {
            return buildVersionDir() + File.separator + this.gameLauncher.gameClient.getServerVersion() + ".jar";
        }
        public String buildClientDir() {
            File clientDir = new File(buildGameDir() + "clients" + File.separator + this.gameLauncher.gameClient.getServerName());
            if (!clientDir.isDirectory()) {
                Engine.getLOGGER().debug("Creating " + this.gameLauncher.gameClient.getServerName() + " directory");
                clientDir.mkdirs();
            }
            return clientDir.toString();
        }

        public File buildRuntimeDir() {
            File runtimeDir = new File(buildGameDir() + "runtime");
            if (!runtimeDir.isDirectory()) {
                runtimeDir.mkdirs();
            }
            return runtimeDir;
        }
    }
    public void setArgsReader(ArgsReader argsReader) {
        this.argsReader = argsReader;
    }

    public ArgsReader getArgsReader() {
        return argsReader;
    }
    public GameLauncher.pathBuilders getPathBuilders() {
        return pathBuilders;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
