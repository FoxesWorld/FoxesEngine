package org.foxesworld.engine.config;

import org.foxesworld.cfgProvider.CfgProvider;
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
            new CfgProvider(cfgFileName);
        }
    }

    @SuppressWarnings("unused")
    public abstract void addToConfig(Map<String, String> inputData, List values);
    @SuppressWarnings("unused")
    public abstract void setConfigValue(String key, Object value);
    @SuppressWarnings("unused")
    public abstract void clearConfigData(List<String> dataToClear, boolean write);
    @SuppressWarnings("unused")
    public abstract void clearConfigData(String dataToClear, boolean write);

    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    protected void setDirPathIndex(int index){
        CfgProvider.setBaseDirPathIndex(index);
    }
    @SuppressWarnings("unused")
    protected  void setCfgExportDir(String dir){
        CfgProvider.setCfgExportDirName(dir);
    }
    @SuppressWarnings("unused")
    protected void setDebug(boolean debug){ CfgProvider.setDebug(debug);}
    protected void setCfgFileExtension(String ext){
        this.cfgFileExtension = ext;
        CfgProvider.setCfgFileExtension(ext);
    }
    @SuppressWarnings("unused")
    protected Map<String, Map<String, Object>> getAllCfgMaps(){
        return CfgProvider.getAllCfgMaps();
    }

    public static String getFullPath() {
        return CfgProvider.getGameFullPath();
    }

    public Map<String, Object> getCONFIG() {
        return CONFIG;
    }
}