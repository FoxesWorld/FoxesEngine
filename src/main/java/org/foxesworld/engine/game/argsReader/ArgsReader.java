package org.foxesworld.engine.game.argsReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.utils.helper.JVMHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsReader {
    private JsonArray jvmArguments, gameArguments;
    private final String path;

    public ArgsReader(String path){
        this.path = path;
        if(new File(path).exists()) {
            this.readArgs();
        }
    }

    private void readArgs() {
        try {
            FileReader fileReader = new FileReader(path);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);

            jvmArguments = jsonObject.getAsJsonObject("arguments").getAsJsonArray("jvm");
            gameArguments = jsonObject.getAsJsonObject("arguments").getAsJsonArray("game");

            jvmArguments = applyRules(jvmArguments);
            gameArguments = applyRules(gameArguments);

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonArray applyRules(JsonArray argumentsArray) {
        JsonArray resultArray = new JsonArray();
        for (JsonElement argumentElement : argumentsArray) {
            JsonObject argumentObject = argumentElement.getAsJsonObject();
            // Проверяем наличие правил для аргумента
            if (argumentObject.has("rules")) {
                JsonArray rulesArray = argumentObject.getAsJsonArray("rules");
                // Проверяем каждое правило
                for (JsonElement ruleElement : rulesArray) {
                    JsonObject ruleObject = ruleElement.getAsJsonObject();
                    // Проверяем, допустимо ли применить правило для текущей платформы
                    if (isRuleApplicable(ruleObject)) {
                        // Добавляем аргумент в массив результата
                        resultArray.add(argumentElement);
                        // Прекращаем проверку правил для этого аргумента, если одно правило уже применимо
                        break;
                    }
                }
            } else {
                // Если для аргумента нет правил, добавляем его в массив результата без проверки
                resultArray.add(argumentElement);
            }
        }
        return resultArray;
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

    public StringBuilder replaceMask(JsonArray arguments, Map<String, String> variables) {
        StringBuilder stringBuilder = new StringBuilder();
        for (JsonElement argumentElement : arguments) {
            JsonObject argumentObject = argumentElement.getAsJsonObject();
            JsonArray valuesArray = argumentObject.getAsJsonArray("values");
            for (JsonElement valueElement : valuesArray) {
                String value = valueElement.getAsString();
                value = replaceVariables(value, variables);
                stringBuilder.append(value).append(" ");
            }
        }
        return stringBuilder;
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
}
