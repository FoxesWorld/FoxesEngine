package org.foxesworld.engine.fileLoader;

/**
 * Интерфейс для отображения информации о процессе загрузки.
 */
public interface ILoadingManager {
    void toggleVisibility();
    void setLoadingText(String descriptionKey, String titleKey);
}
