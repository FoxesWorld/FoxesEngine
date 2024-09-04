package org.foxesworld.engine.config;

import org.foxesworld.cfgProvider.CfgProvider;
import com.google.gson.GsonBuilder;
import org.foxesworld.engine.Engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Config extends ConfigAbstract {
    private Engine engine;
    private Map<String, Object> CONFIG;

    public Config(Engine engine) {
        this.engine = engine;
        setCfgExportDir("config");
        setDirPathIndex(0);
        setCfgFileExtension(".json");
        CfgProvider.setLOGGER(engine.getLOGGER());
        addCfgFiles(engine.getConfigFiles());
        this.CONFIG = getCfgMaps().get("internal/config");
    }

    public void addToConfig(Map<String, String> inputData, List values) {
        for (Map.Entry<String, String> configEntry : inputData.entrySet()) {
            if (values.contains(configEntry.getKey())) {
                this.engine.getCONFIG().put(configEntry.getKey(), configEntry.getValue());
            }
        }
    }

    public void setConfigValue(String key, Object value){
        if(CONFIG.get(key) != null) {
            clearConfigData(Arrays.asList(key), false);
        }
        CONFIG.put(key, value);
    }

    public void clearConfigData(List<String> dataToClear, boolean write) {
        this.engine.getLOGGER().debug("Wiping "+dataToClear);
        for (String keyToWipe : dataToClear) {
            this.CONFIG.remove(keyToWipe);
        }
        if (write) {
            this.writeCurrentConfig();
        }
    }

    public void writeCurrentConfig() {
        this.engine.getLOGGER().debug("Writing "+ configToJSON());
        try (FileWriter fileWriter = new FileWriter(this.getFullPath() + File.separator + "config.json")) {
            fileWriter.write(configToJSON());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, Object>> getCfgMaps() {
        return getAllCfgMaps();
    }

    public String configToJSON() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(CONFIG);
    }

    public Map<String, Object> getCONFIG() {
        return CONFIG;
    }

    public void setCONFIG(Map<String, Object> CONFIG) {
        this.CONFIG = CONFIG;
    }

    public Engine getAppFrame() {
        return engine;
    }

    @Override
    public String getFullPath() {
        return CfgProvider.getGameFullPath();
    }


}
