package org.foxesworld.engine.config;

import org.foxesworld.cfgProvider.CfgProvider;

import java.util.Map;

public abstract class ConfigAbstract {

    private String cfgFileExtension = "";

    protected void addCfgFiles(String[] configFiles){
        for(String cfgUnit: configFiles){
            String cfgFileName = cfgUnit + cfgFileExtension;
            new CfgProvider(cfgFileName);
        }
    }

    protected void setDirPathIndex(int index){
        CfgProvider.setBaseDirPathIndex(index);
    }
    protected  void setCfgExportDir(String dir){
        CfgProvider.setCfgExportDirName(dir);
    }
    protected void setCfgFileExtension(String ext){
        this.cfgFileExtension = ext;
        CfgProvider.setCfgFileExtension(ext);
    }
    protected Map<String, Map<String, Object>> getAllCfgMaps(){
        return CfgProvider.getAllCfgMaps();

    }

    protected String getFullPath() {
        return CfgProvider.getGameFullPath();
    }
}
