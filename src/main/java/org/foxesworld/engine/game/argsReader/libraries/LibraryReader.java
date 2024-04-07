package org.foxesworld.engine.game.argsReader.libraries;


import com.google.gson.*;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.game.GameLauncher;
import org.foxesworld.engine.game.argsReader.ArgsReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LibraryReader {

    private final String path, currentOS;
    private final GameLauncher gameLauncher;

    public LibraryReader(ArgsReader argsReader) {
        this.gameLauncher = argsReader.getGameLauncher();
        this.path = this.gameLauncher.getPathBuilders().getArgsFile();
        currentOS = determineCurrentOS();
    }

    public List<Library> readLibraries() {
        List<Library> libraries = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            JsonArray librariesArray = jsonObject.getAsJsonArray("libraries");
            Engine.LOGGER.debug("Total libraries to process: {}", librariesArray.size());
            for (JsonElement libraryElement : librariesArray) {
                JsonObject libraryObject = libraryElement.getAsJsonObject();
                if (isLibraryAllowed(libraryObject)) {
                    Library library = convertToLibrary(libraryObject);
                    Engine.LOGGER.debug("Adding {} for {} ENV", library.getName(), determineCurrentOS());
                    libraries.add(library);
                }
            }
        } catch (IOException e) {
            Engine.LOGGER.error("Error reading libraries file {}: {}", path, e.getMessage());
        }
        return libraries;
    }

    private boolean isLibraryAllowed(JsonObject libraryObject) {
        return checkRules(libraryObject) && checkPlatform(libraryObject, "natives") && checkPlatform(libraryObject, "classifiers");
    }

    private boolean checkRules(JsonObject libraryObject) {
        JsonArray rulesArray = libraryObject.getAsJsonArray("rules");
        if (rulesArray == null || rulesArray.size() == 0) {
            return true;
        }
        boolean allow = false;
        boolean disallow = false;

        for (JsonElement ruleElement : rulesArray) {
            JsonObject ruleObject = ruleElement.getAsJsonObject();
            String action = ruleObject.get("action").getAsString();

            if ("allow".equals(action)) {
                allow = true;
            } else if ("disallow".equals(action)) {
                if (isRuleApplicable(ruleObject)) {
                    disallow = true;
                    Engine.LOGGER.debug("Rule disallowed: {}", ruleObject);
                }
            }
        }

        return allow && !disallow;
    }

    private boolean isRuleApplicable(JsonObject rule) {
        if (rule != null && rule.has("os")) {
            String osName = System.getProperty("os.name").toLowerCase();
            JsonObject osObject = rule.getAsJsonObject("os");
            if (osObject.entrySet().isEmpty()) {
                return true;
            }
            String ruleOS = osObject.get("name").getAsString().toLowerCase();
            return osName.contains(ruleOS);
        }
        return true;
    }

    private boolean checkPlatform(JsonObject libraryObject, String platform) {
        JsonObject platformObject = libraryObject.getAsJsonObject(platform);
        if (platformObject != null) {
            return platformObject.has(currentOS);
        }
        return true;
    }

    private String determineCurrentOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("mac")) {
            return "osx";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "linux";
        } else {
            return osName;
        }
    }

    @SuppressWarnings("unused")
    public String getLibrariesAsString(String libDir) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Library> libraries = readLibraries();
        List<URL> libraryURLs = new LinkedList<>();
        for (Library library : libraries) {
            System.out.println(library.getName()); // Debugging
            String fullPath = libDir + File.separator + library.getArtifact().getPath();
            if (new File(fullPath).exists()) {
                stringBuilder.append(fullPath).append(File.pathSeparator);
                try {
                    libraryURLs.add(Paths.get(fullPath).toUri().toURL());
                } catch (MalformedURLException e) {
                    // Log the error message
                    Engine.LOGGER.error("Error creating URL for library {}: {}", fullPath, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Library file " + fullPath + " not found"); // Debugging
            }
        }
        gameLauncher.createClassLoader(libraryURLs);
        return stringBuilder.toString();
    }

    private Library convertToLibrary(JsonObject libraryObject) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(libraryObject, Library.class);
    }
}