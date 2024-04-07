package org.foxesworld.engine.game.argsReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.game.GameLauncher;
import org.foxesworld.engine.game.argsReader.libraries.Library;
import org.foxesworld.engine.game.argsReader.libraries.LibraryReader;
import org.foxesworld.engine.utils.helper.JVMHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsReader {
    private JsonArray jvmArguments, gameArguments;
    private LibraryReader libraryReader;
    private final String path;
    private GameLauncher gameLauncher;
    private String mainClass, assets;
    private boolean authLib;

    public ArgsReader(GameLauncher gameLauncher){
        this.gameLauncher = gameLauncher;
        this.path = gameLauncher.getPathBuilders().getArgsFile();
        if(new File(path).exists()) {
            this.libraryReader = new LibraryReader(this);
            this.readArgs();
        }
    }

    private void readArgs() {
        try {
            FileReader fileReader = new FileReader(path);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);

            mainClass = jsonObject.get("mainClass").getAsString();
            authLib = jsonObject.get("authLib").getAsBoolean();
            assets = jsonObject.get("assets").getAsString();

            jvmArguments = jsonObject.getAsJsonObject("arguments").getAsJsonArray("jvm");
            gameArguments = jsonObject.getAsJsonObject("arguments").getAsJsonArray("game");

            jvmArguments = applyRules(jvmArguments);
            //gameArguments = applyRules(gameArguments);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonArray applyRules(JsonArray argumentsArray) {
        JsonArray resultArray = new JsonArray();

        for (JsonElement argumentElement : argumentsArray) {
            JsonObject argumentObject = argumentElement.getAsJsonObject();

            // Get the rules for the current argument
            JsonArray rulesArray = argumentObject.getAsJsonArray("rules");

            // If rules are absent or null, or the array is empty, add the argument directly
            if (rulesArray == null || rulesArray.size() == 0) {
                resultArray.add(argumentElement);
                continue; // Skip the rest of the loop for this argument
            }

            // Check if any rule is applicable
            if (hasApplicableRule(rulesArray)) {
                resultArray.add(argumentElement);
            }
        }

        return resultArray;
    }

    // Check if any rule in the array is applicable
    private boolean hasApplicableRule(JsonArray rulesArray) {
        for (JsonElement ruleElement : rulesArray) {
            JsonObject ruleObject = ruleElement.getAsJsonObject();
            if (isRuleApplicable(ruleObject) || ruleObject == null) {
                return true;
            }
        }
        return false;
    }



    private boolean isRuleApplicable(JsonObject rule) {
        if (rule != null && rule.has("os")) {
            String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            JVMHelper.OS currentOS = JVMHelper.OS.byName(osName);

            JsonObject osRule = rule.getAsJsonObject("os");

            if (osRule.entrySet().isEmpty()) {
                return true;
            }
            String ruleOSName = osRule.get("name").getAsString().toLowerCase(Locale.ENGLISH);
            JVMHelper.OS ruleOS = JVMHelper.OS.byName(ruleOSName);

            return currentOS == ruleOS;
        }  else {
            return false;
        }
    }
    public List<String> replaceMask(JsonArray arguments, Map<String, String> variables) {
        List<String> argsAndValues = new ArrayList<>();
        for (JsonElement argumentElement : arguments) {
            JsonObject argumentObject = argumentElement.getAsJsonObject();
            JsonArray valuesArray = argumentObject.getAsJsonArray("values");
            for (JsonElement valueElement : valuesArray) {
                String value = valueElement.getAsString();
                value = replaceVariables(value, variables);
                argsAndValues.add(value);
            }
        }
        return argsAndValues;
    }
    private String replaceVariables(String value, Map<String, String> variables) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (variables.containsKey(variableName)) {
                String variableValue = variables.get(variableName);
                value = value.replace("${" + variableName + "}", variableValue);
            }
        }
        return value;
    }

    public JsonArray getJvmArguments() {
        return jvmArguments;
    }

    public JsonArray getGameArguments() {
        return gameArguments;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getAssets() {
        return assets;
    }

    public boolean isAuthLib() {
        return authLib;
    }

    public GameLauncher getGameLauncher() {
        return gameLauncher;
    }

    public LibraryReader getLibraryReader() {
        return libraryReader;
    }
}