package org.foxesworld.engine.game;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.server.ServerAttributes;
import org.foxesworld.engine.utils.LibraryScanner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameLauncher {

    protected GameListener gameListener;
    protected ServerAttributes gameClient;
    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected Engine engine;
    protected Logger logger;
    protected Config config;
    protected int intVer;
    private final String[] toTest = {"_JAVA_OPTIONS", "_JAVA_OPTS", "JAVA_OPTS", "JAVA_OPTIONS"};
    protected URLClassLoader classLoader;
    protected final List<String> processArgs = new ArrayList<>();
    protected boolean isStarted;

    protected abstract void addArgs(String tweakClassVal);
    protected abstract void launchGame();
    protected abstract void setJre();

    protected URLClassLoader collectLibraries() {
        AtomicInteger num = new AtomicInteger();
        processArgs.add("-cp");

        StringBuilder sb = new StringBuilder();
        List<URL> libraryURLs = new LinkedList<>();

        new LibraryScanner(this.engine).findLibraryPaths(buildLibrariesPath()).forEach(libraryPathString -> {
            Path libraryPath = Paths.get(libraryPathString);
            sb.append(libraryPath.toAbsolutePath()).append(File.pathSeparator);

            if (libraryPath.toFile().isFile()) {
                try {
                    URL libraryURL = libraryPath.toUri().toURL();
                    libraryURLs.add(libraryURL);
                } catch (MalformedURLException e) {
                    logger.error("Error creating URL for library: " + libraryPath, e);
                }
            }
            num.getAndIncrement();
        });
        sb.append(buildMinecraftJarPath()).append(File.pathSeparator);
        processArgs.add(sb.toString());

        logger.debug(num.get() + " libraries found");
        return createClassLoader(libraryURLs);
    }

    protected String tweakClass() {
        String tweakClassVal;
        List<TweakClasses> tweakClasses = this.engine.getEngineData().getTweakClasses();
        for (TweakClasses aClass : tweakClasses) {
            String className = aClass.classPath;
            Engine.getLOGGER().debug("Searching " + className);
            try {
                classLoader.loadClass(className);
                tweakClassVal = "--tweakClass=" + className;
                logger.debug("TweakClass " + className + " was found!");
                System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
                System.setProperty("fml.ignorePatchDiscrepancies", "true");
                return tweakClassVal;
            } catch (ClassNotFoundException classNotFoundException) {
                Engine.getLOGGER().debug("TweakClass " + className + " not found");
            }
        }
        return "";
    }

    private URLClassLoader createClassLoader(List<URL> libraryURLs) {
        URL[] urls = libraryURLs.toArray(new URL[0]);
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

    public String buildGameDir() {
        return Config.getFullPath();
    }

    public String buildLibrariesPath() {
        return buildVersionDir() + File.separator + "libraries";
    }

    public String buildVersionDir() {
        return buildGameDir() + "versions" + File.separator + gameClient.getServerVersion();
    }

    public String buildMinecraftJarPath() {
        return buildVersionDir() + File.separator + gameClient.getServerVersion() + ".jar";
    }

    public String buildNativesPath() {
        return buildVersionDir() + File.separator + "natives";
    }

    public String buildClientDir() {
        File clientDir = new File(buildGameDir() + "clients" + File.separator + gameClient.getServerName());
        if (!clientDir.isDirectory()) {
            Engine.getLOGGER().debug("Creating " + gameClient.getServerName() + " directory");
            clientDir.mkdirs();
        }
        return clientDir.toString();
    }

    protected String buildAssetsPath() {
        return buildGameDir() + "assets";
    }

    public File buildRuntimeDir() {
        File runtimeDir = new File(buildGameDir() + "runtime");
        if (!runtimeDir.isDirectory()) {
            runtimeDir.mkdirs();
        }
        return runtimeDir;
    }

    public String getCurrentJre() {
        return this.gameClient.getJreVersion();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setStarted(boolean started) {
        isStarted = started;
        if (!isStarted) {
            executorService.shutdown();
        }
    }

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

    public boolean isStarted() {
        return isStarted;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    protected int getIntVer() {
        return intVer;
    }

    protected String getArgsFile(){
        return this.buildVersionDir() + File.separator + this.gameClient.getServerVersion() +".json";
    }

    public Engine getEngine() {
        return engine;
    }

    public URLClassLoader getClassLoader(){
        return  classLoader;
    }

    public List<String> getProcessArgs(){
        return  processArgs;
    }

    public ServerAttributes getGameClient() {
        return gameClient;
    }
}
