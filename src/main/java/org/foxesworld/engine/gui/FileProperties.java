package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings("unused")
public class FileProperties {
    private String frameTpl, mainFrame,localeFile,soundsFile;

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
    @SuppressWarnings("unused")
    public String getFrameTpl() {
        return frameTpl;
    }
    @SuppressWarnings("unused")
    public String getMainFrame() {
        return mainFrame;
    }
    @SuppressWarnings("unused")
    public String getLocaleFile() {
        return localeFile;
    }
    @SuppressWarnings("unused")
    public String getSoundsFile() {
        return soundsFile;
    }
}
