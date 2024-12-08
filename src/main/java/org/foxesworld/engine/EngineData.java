package org.foxesworld.engine;

import com.google.gson.Gson;
import org.foxesworld.engine.utils.HTTP.HTTPconf;
import org.foxesworld.engine.gui.loadingManager.LoadManagerAttributes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class EngineData {
    private String logLevel,bindUrl,launcherBrand,launcherVersion,appId,accessToken,programRuntime,groupDomain,vkAPIversion;
    private String[] styles,loadAdapters;
    private DownloadManager downloadManager;
    private LoadManagerAttributes loadManager[];
    private HTTPconf httpConf;
    private String[] tweakClasses;
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
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }
    public LoadManagerAttributes[] getLoadManager() { return  loadManager;};
    public HTTPconf getHttPconf() {
        return httpConf;
    }
    public String[] getTweakClasses() {
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

    public String[] getStyles() {
        return styles;
    }

    public String[] getLoadAdapters() {
        return loadAdapters;
    }

    public static class DownloadManager {
        private int downloadThreads;
        private List<ReplaceMask> replaceMasks;

        public int getDownloadThreads() {
            return downloadThreads;
        }

        public List<ReplaceMask> getReplaceMasks() {
            return replaceMasks;
        }
    }

    public static class ReplaceMask {
        private String mask, suffix, prefix,replace;

        public String getMask() {
            return mask;
        }

        public String getReplace() {
            return replace;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public EngineData initEngineValues(String propertyPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propertyPath);
        if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return new Gson().fromJson(reader, EngineData.class);
        }
        return null;
    }
}
