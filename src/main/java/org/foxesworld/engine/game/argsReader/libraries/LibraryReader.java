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
    private final List<Library> libraries;

    public LibraryReader(ArgsReader argsReader) {
        this.gameLauncher = argsReader.getGameLauncher();
        this.path = this.gameLauncher.getPathBuilders().getArgsFile();
        currentOS = determineCurrentOS();
        this.libraries = readLibraries();
    }

    private List<Library> readLibraries() {
        List<Library> libraries = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            JsonArray librariesArray = jsonObject.getAsJsonArray("libraries");
            Engine.LOGGER.debug("Total libraries to process: {}", librariesArray.size());
            for (JsonElement libraryElement : librariesArray) {
                JsonObject libraryObject = libraryElement.getAsJsonObject();
                if (isLibraryAllowed(libraryObject)) {
                    Library library = convertToLibrary(libraryObject);
                    Engine.LOGGER.debug("Adding {} for {} ENV", library.getName(), currentOS);
                    libraries.add(library);
                }
            }
            Engine.LOGGER.debug("{} lib num {}", currentOS, libraries.size());
        } catch (IOException e) {
            Engine.LOGGER.error("Error reading libraries file {}: {}", path, e.getMessage());
        }
        return libraries;
    }

    private boolean isLibraryAllowed(JsonObject libraryObject) {
        return checkRules(libraryObject) && checkPlatform(libraryObject, "natives") && checkPlatform(libraryObject, "classifies");
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
            JsonObject osObject = rule.getAsJsonObject("os");
            if (osObject.entrySet().isEmpty()) {
                return true;
            }
            String ruleOS = osObject.get("name").getAsString().toLowerCase();
            return currentOS.contains(ruleOS);
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
                Engine.LOGGER.debug("Library {} doesn't exist!", fullPath);
            }
        }
        gameLauncher.createClassLoader(libraryURLs);
        return stringBuilder.toString();
    }

    private Library convertToLibrary(JsonObject libraryObject) {
        Gson gson = new GsonBuilder().create();
        Library library = gson.fromJson(libraryObject, Library.class);
        if (library.getArtifact() == null) {
            String name = library.getName();
            String packed = libraryObject.has("packed") ? libraryObject.get("packed").getAsString() : null;
            String[] nameParts = name.split(":");
            String filePath = "";
            if (packed != null) {
                filePath = String.format("%s/%s/%s/%s-%s.jar", nameParts[0].replace(".", File.separator), nameParts[1], nameParts[2], nameParts[1], nameParts[2]);
            } /*else {
                filePath = String.format("/%s/%s/%s-%s.jar", nameParts[0], nameParts[1], nameParts[1], nameParts[2]);
            }
            */
            Artifact artifact = new Artifact("", 0, filePath, "");
            library.setArtifact(artifact);
        }

        JsonObject classifiesObject = libraryObject.getAsJsonObject("classifies");
        if (classifiesObject != null) {
            JsonObject platformObject = classifiesObject.getAsJsonObject(currentOS);
            if (platformObject != null) {
                String sha1 = platformObject.get("sha1").getAsString();
                int size = platformObject.get("size").getAsInt();
                String path = platformObject.get("path").getAsString();
                String url = platformObject.get("url").getAsString();

                Artifact artifact = new Artifact(sha1, size, path, url);
                library.setArtifact(artifact);
            }
        }

        return library;
    }
}