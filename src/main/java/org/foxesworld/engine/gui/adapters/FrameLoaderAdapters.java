package org.foxesworld.engine.gui.adapters;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.json.JsonFrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.json5.Json5FrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.xml.XmlFrameAttributesLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс-адаптер для загрузчиков атрибутов фрейма.
 *
 * Отвечает за регистрацию, получение и управление загрузчиками для различных типов файлов.
 */
@SuppressWarnings("unused")
public class FrameLoaderAdapters {

    // Карта зарегистрированных адаптеров (потокобезопасная)
    private final Map<String, FrameAttributesLoader> adapters = new ConcurrentHashMap<>();
    // Карта адаптеров по умолчанию (потокобезопасная)
    private final Map<String, FrameAttributesLoader> defaultAdapters = new ConcurrentHashMap<>();
    private final Engine engine;

    /**
     * Конструктор, инициализирующий адаптеры по умолчанию и загруженные из конфигурации движка.
     *
     * @param engine экземпляр движка
     */
    public FrameLoaderAdapters(Engine engine) {
        this.engine = engine;
        registerDefaultAdapters();
        registerEngineConfiguredAdapters();
    }

    /**
     * Регистрирует адаптеры по умолчанию.
     */
    private void registerDefaultAdapters() {
        defaultAdapters.put("json", new JsonFrameAttributesLoader());
        defaultAdapters.put("json5", new Json5FrameAttributesLoader());
        defaultAdapters.put("xml", new XmlFrameAttributesLoader());
        // defaultAdapters.put("fxml", new XmlFrameAttributesLoader());
        // defaultAdapters.put("yaml", new YamlFrameAttributesLoader());
    }

    /**
     * Регистрирует адаптеры, заданные в конфигурации движка.
     * <p>
     * Если список адаптеров пустой или не задан, регистрируются все адаптеры по умолчанию.
     */
    private void registerEngineConfiguredAdapters() {
        String[] loadAdapters = engine.getEngineData().getLoadAdapters();
        if (loadAdapters == null) {
            // Если в конфигурации движка не указаны конкретные адаптеры, регистрируем все по умолчанию.
            adapters.putAll(defaultAdapters);
            Engine.LOGGER.info("No specific adapters configured. Registered all default adapters: {}", adapters.keySet());
        } else {
            for (String type : loadAdapters) {
                FrameAttributesLoader adapter = defaultAdapters.get(type);
                if (adapter != null) {
                    adapters.put(type, adapter);
                    Engine.LOGGER.info("Registering {} adapter...", type);
                } else {
                    Engine.LOGGER.warn("No default adapter found for type: {}", type);
                }
            }
            Engine.LOGGER.info("Registered adapters: {}", adapters.keySet());
        }
    }

    /**
     * Возвращает загрузчик для указанного типа файла.
     *
     * @param fileType тип файла
     * @return соответствующий загрузчик
     * @throws IllegalArgumentException если адаптер для указанного типа не найден
     */
    public FrameAttributesLoader getLoader(String fileType) {
        FrameAttributesLoader loader = adapters.get(fileType);
        if (loader == null) {
            throw new IllegalArgumentException("No adapter found for: " + fileType);
        }
        return loader;
    }

    /**
     * Регистрирует адаптер для указанного типа файла.
     *
     * @param type    тип файла
     * @param adapter экземпляр загрузчика атрибутов
     */
    public void registerAdapter(String type, FrameAttributesLoader adapter) {
        if (adapters.containsKey(type)) {
            Engine.LOGGER.warn("Adapter for type {} is already registered and will be overwritten.", type);
        }
        adapters.put(type, adapter);
        Engine.LOGGER.info("Registered {} adapter", type);
    }

    /**
     * Пакетно регистрирует адаптеры.
     *
     * @param newAdapters карта, содержащая тип файла и соответствующий загрузчик атрибутов
     */
    public void registerAdapters(Map<String, FrameAttributesLoader> newAdapters) {
        if (newAdapters == null) {
            Engine.LOGGER.warn("Provided newAdapters map is null");
            return;
        }
        for (Map.Entry<String, FrameAttributesLoader> entry : newAdapters.entrySet()) {
            registerAdapter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Удаляет адаптер для указанного типа файла.
     *
     * @param type тип файла
     */
    public void unregisterAdapter(String type) {
        if (adapters.remove(type) != null) {
            Engine.LOGGER.info("Unregistered {} adapter", type);
        } else {
            Engine.LOGGER.warn("No adapter found to unregister for type: {}", type);
        }
    }

    /**
     * Пакетно удаляет адаптеры для указанных типов файлов.
     *
     * @param types коллекция типов файлов
     */
    public void unregisterAdapters(Collection<String> types) {
        if (types == null) {
            Engine.LOGGER.warn("Provided types collection is null");
            return;
        }
        for (String type : types) {
            unregisterAdapter(type);
        }
    }

    /**
     * Проверяет, зарегистрирован ли адаптер для указанного типа файла.
     *
     * @param type тип файла
     * @return true, если адаптер зарегистрирован, иначе false
     */
    public boolean isAdapterRegistered(String type) {
        return adapters.containsKey(type);
    }

    /**
     * Возвращает копию карты зарегистрированных адаптеров.
     *
     * @return карта зарегистрированных адаптеров
     */
    public Map<String, FrameAttributesLoader> getRegisteredAdapters() {
        return new ConcurrentHashMap<>(adapters);
    }

    /**
     * Возвращает копию карты адаптеров по умолчанию.
     *
     * @return карта адаптеров по умолчанию
     */
    public Map<String, FrameAttributesLoader> getDefaultAdapters() {
        return new ConcurrentHashMap<>(defaultAdapters);
    }

    /**
     * Сбрасывает зарегистрированные адаптеры, возвращая их к настройкам по умолчанию.
     */
    public void resetAdapters() {
        adapters.clear();
        adapters.putAll(defaultAdapters);
        Engine.LOGGER.info("Adapters have been reset to default: {}", adapters.keySet());
    }
}
