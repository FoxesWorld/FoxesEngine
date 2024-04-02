package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;

import java.lang.reflect.Field;
import java.util.Map;

public class FileProperties {
    private String frameTpl;
    private String mainFrame;
    private String localeFile;
    private String soundsFile;

    public FileProperties(Engine engine){
        Map<String, Object> guiList = engine.getEngineData().getFiles();
        for (Map.Entry<String, Object> guiEl : guiList.entrySet()) {
            try {
                Field field = FileProperties.class.getDeclaredField(guiEl.getKey());
                if(field.hashCode()!= 0) {
                    field.set(this, guiEl.getValue());
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    public String getFrameTpl() {
        return frameTpl;
    }

    public String getMainFrame() {
        return mainFrame;
    }

    public String getLocaleFile() {
        return localeFile;
    }

    public String getSoundsFile() {
        return soundsFile;
    }
}
