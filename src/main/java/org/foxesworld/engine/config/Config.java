package org.foxesworld.engine.config;

import com.foxesworld.cfgProvider.cfgProvider;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class Config {

    protected Map<String, Object> CONFIG;
    private String cfgFileExtension = "";

    protected void addCfgFiles(String[] configFiles){
        for(String cfgUnit: configFiles){
            String cfgFileName = cfgUnit + cfgFileExtension;
            new cfgProvider(cfgFileName);
        }
    }

    public abstract void addToConfig(Map<String, String> inputData, List values);
    public abstract void setConfigValue(String key, Object value);
    public abstract void clearConfigData(List<String> dataToClear, boolean write);
    public abstract void clearConfigData(String dataToClear, boolean write);

    public void assignConfigValues(){
        for(Map.Entry<String, Object> configMap : this.CONFIG.entrySet()){
            try {
                Field field = ObjectInputFilter.Config.class.getDeclaredField(configMap.getKey());
                if(field.hashCode()!= 0) {
                    field.set(this, configMap.getValue());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //throw new RuntimeException(e);
                this.writeCurrentConfig();
            }
        }
    }

    public void writeCurrentConfig() {
        //Engine.getLOGGER().debug("Writing "+ configToJSON());
        try (FileWriter fileWriter = new FileWriter(getFullPath() + File.separator + "config/config.json")) {
            fileWriter.write(configToJSON());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String configToJSON() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(CONFIG);
    }

    protected void setDirPathIndex(int index){
        cfgProvider.setBaseDirPathIndex(index);
    }
    protected  void setCfgExportDir(String dir){
        cfgProvider.setCfgExportDirName(dir);
    }
    protected void setDebug(boolean debug){ cfgProvider.setDebug(debug);}
    protected void setCfgFileExtension(String ext){
        this.cfgFileExtension = ext;
        cfgProvider.setCfgFileExtension(ext);
    }
    protected Map<String, Map> getAllCfgMaps(){
        return cfgProvider.getAllCfgMaps();
    }

    public static String getFullPath() {
        return cfgProvider.getGameFullPath();
    }

    public Map<String, Object> getCONFIG() {
        return CONFIG;
    }
}