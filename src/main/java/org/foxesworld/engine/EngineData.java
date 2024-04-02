package org.foxesworld.engine;

import com.google.gson.Gson;
import org.foxesworld.engine.game.TweakClasses;
import org.foxesworld.engine.utils.HTTP.RequestProperty;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EngineData {
    private String logLevel,bindUrl,launcherBrand,launcherVersion,appId,accessToken,programRuntime,groupDomain,vkAPIversion;
    private int downloadThreads;
    private List<RequestProperty> requestProperties;
    private List<TweakClasses> tweakClasses;
    private Map<String, Object> files;
    public String getBindUrl() {
        return bindUrl;
    }
    public String getLauncherBrand() {
        return launcherBrand;
    }
    public String getLauncherVersion() {
        return launcherVersion;
    }
    public String getAppId() {
        return appId;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public String getGroupDomain() {
        return groupDomain;
    }
    public String getVkAPIversion() {
        return vkAPIversion;
    }
    public int getDownloadThreads() {
        return downloadThreads;
    }
    public List<RequestProperty> getRequestProperties() {
        return requestProperties;
    }
    public List<TweakClasses> getTweakClasses() {
        return tweakClasses;
    }
    public String getLogLevel() {
        return logLevel;
    }
    public String getProgramRuntime() {
        return programRuntime;
    }
    public Map<String, Object> getFiles() {
        return files;
    }
    public EngineData initEngineValues(String propertyPath) {
        InputStream inputStream = Engine.class.getClassLoader().getResourceAsStream(propertyPath);
        if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return new Gson().fromJson(reader, EngineData.class);
        }
        return null;
    }
}
