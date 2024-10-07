package org.foxesworld.engine.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.foxesworld.cfgProvider.CfgProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class Config {

    protected Map<String, Object> config;
    private String cfgFileExtension = "";

    /**
     * Adds configuration files to the provider.
     *
     * @param configFiles List of configuration file names without extension
     */
    protected void addCfgFiles(List<String> configFiles) {
        configFiles.forEach(cfgUnit -> {
            String cfgFileName = cfgUnit + cfgFileExtension;
            new CfgProvider(cfgFileName);
        });
    }

    /**
     * Abstract methods for configuration operations.
     */
    public abstract void addToConfig(Map<String, Object> inputData, List<?> values);
    public abstract void setConfigValue(String key, Object value);
    public abstract void clearConfigData(List<String> dataToClear, boolean write);
    public abstract void clearConfigData(String dataToClear, boolean write);

    /**
     * Assigns configuration values to the fields of the implementing class.
     */
    public void assignConfigValues() {
        config.forEach((key, value) -> {
            try {
                Field field = this.getClass().getDeclaredField(key);
                field.setAccessible(true); // Ensure the field is accessible
                field.set(this, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                writeCurrentConfig();
            }
        });
    }

    /**
     * Writes the current configuration to a JSON file.
     */
    public void writeCurrentConfig() {
        try (FileWriter fileWriter = new FileWriter(getFullPath() + "config/config.json")) {
            fileWriter.write(configToJSON());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the configuration to a JSON string.
     *
     * @return JSON representation of the configuration
     */
    public String configToJSON() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(this.getClass(), new ConfigSerializer())
                .create();
        return gson.toJson(this);
    }

    /**
     * Sets the base directory path index.
     *
     * @param index The index of the base directory path
     */
    protected void setDirPathIndex(int index) {
        CfgProvider.setBaseDirPathIndex(index);
    }

    /**
     * Sets the configuration export directory.
     *
     * @param dir The directory to export configuration files to
     */
    protected void setCfgExportDir(String dir) {
        CfgProvider.setCfgExportDirName(dir);
    }

    /**
     * Sets the configuration file extension.
     *
     * @param ext The file extension for configuration files
     */
    protected void setCfgFileExtension(String ext) {
        this.cfgFileExtension = ext;
        CfgProvider.setCfgFileExtension(ext);
    }

    /**
     * Retrieves all configuration maps from the provider.
     *
     * @return A map of all configuration maps
     */
    protected Map<String, Map<String, Object>> getAllCfgMaps() {
        return CfgProvider.getAllCfgMaps();
    }

    /**
     * Retrieves the full path for configuration files.
     *
     * @return The full path as a string
     */
    public static String getFullPath() {
        return CfgProvider.getGameFullPath() + File.separator;
    }

    /**
     * Gets the configuration map.
     *
     * @return The configuration map
     */
    public Map<String, Object> getConfig() {
        return config;
    }
}