package org.foxesworld.engine.game.argsReader.libraries;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LibraryReader {

    private String path;
    private List<URL> libraryURLs = new LinkedList<>();
    public LibraryReader(String path) {
        this.path = path;
    }
    public List<Library> readLibraries() {
        List<Library> libraries = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(path);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);

            JsonArray librariesArray = jsonObject.getAsJsonArray("libraries");
            for (JsonElement libraryElement : librariesArray) {
                JsonObject libraryObject = libraryElement.getAsJsonObject();
                Library library = convertToLibrary(libraryObject);
                // Check rules, natives, and classifiers before adding the library
                if (checkRules(libraryObject) && checkNatives(libraryObject) && checkClassifiers(libraryObject)) {
                    libraries.add(library);
                }
            }

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libraries;
    }

    private Library convertToLibrary(JsonObject libraryObject) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(libraryObject, Library.class);
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
                }
                else if ("disallow".equals(action)) {
                    if (isRuleApplicable(ruleObject)) {
                        disallow = true;
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

    private boolean checkNatives(JsonObject libraryObject) {
        JsonObject nativesObject = libraryObject.getAsJsonObject("natives");
        if (nativesObject != null) {
            String currentOS = determineCurrentOS();
            return nativesObject.has(currentOS);
        }
        return true;
    }

    private boolean checkClassifiers(JsonObject libraryObject) {
        JsonObject classifiersObject = libraryObject.getAsJsonObject("classifiers");
        if (classifiersObject != null) {
            String currentOS = determineCurrentOS();
            return classifiersObject.has(currentOS);
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

    public String getLibrariesAsString(String libDir) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Library> libraries = readLibraries();
        URL libraryURL = null;
        for (Library library : libraries) {
            String fullPath = libDir + File.separator + library.getArtifact().getPath();
            if (new File(fullPath).exists()) {
                stringBuilder.append(fullPath).append(File.pathSeparator);

                try {
                    libraryURL = Paths.get(fullPath).toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                libraryURLs.add(libraryURL);
            }
        }
        return stringBuilder.toString();
    }

    public List<URL> getLibraryURLs() {
        return libraryURLs;
    }
}


/*
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibraryReader {

    private String path;
    public LibraryReader(String path){
        this.path = path;
    }

    public List<Library> readLibraries() {
        List<Library> libraries = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(path);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);

            JsonArray librariesArray = jsonObject.getAsJsonArray("libraries");
            for (JsonElement libraryElement : librariesArray) {
                JsonObject libraryObject = libraryElement.getAsJsonObject();
                Gson gson = new GsonBuilder().create();
                Library library = gson.fromJson(libraryObject, Library.class);
                if (checkRules(libraryObject) && checkNatives(libraryObject)) {
                    libraries.add(library);
                }
            }

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libraries;
    }

    private boolean checkRules(JsonObject libraryObject) {
        JsonArray rulesArray = libraryObject.getAsJsonArray("rules");
        if (rulesArray != null) {
            for (JsonElement ruleElement : rulesArray) {
                JsonObject ruleObject = ruleElement.getAsJsonObject();
                if (!isRuleApplicable(ruleObject)) {
                    return false;
                }
            }
        }
        return true;
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

    private boolean checkNatives(JsonObject libraryObject) {
        JsonObject nativesObject = libraryObject.getAsJsonObject("natives");
        if (nativesObject != null) {
            String currentOS = determineCurrentOS();
            if (nativesObject.has(currentOS)) {
                return true;
            } else {
                return false;
            }
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

}
*/