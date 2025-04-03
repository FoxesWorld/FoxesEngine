package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.FrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.FrameLoaderAdapters;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.notification.Notification;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Расширенный класс для построения графических интерфейсов.
 *
 * Предоставляет синхронное и асинхронное построение GUI с возможностью регистрации дополнительных загрузчиков,
 * множественных слушателей событий и отмены асинхронных задач.
 */
public class GuiBuilder {
    private final FrameLoaderAdapters frameLoaderAdapters;
    private final FrameConstructor frameConstructor;
    private final ComponentFactory componentFactory;
    private final Notification notification;
    private final Engine engine;

    private final Map<String, JPanel> panelsMap = new ConcurrentHashMap<>();
    private final Map<String, List<JComponent>> componentsMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> childParentMap = new ConcurrentHashMap<>();
    private final Map<String, JPanel> loadPanels = new ConcurrentHashMap<>();
    private GuiBuilderListener guiBuilderListener;

    // Поддержка нескольких слушателей построения GUI
    private final List<GuiBuilderListener> guiBuilderListeners = new CopyOnWriteArrayList<>();

    // Слушатель, который выполнится по завершению построения всех панелей (однократно)
    private Runnable onPanelsBuildTask;

    // Флаг для контроля одноразовой сборки дополнительных панелей
    private final AtomicBoolean additionalPanelsBuilt = new AtomicBoolean(false);

    // Хранит текущий CompletableFuture для асинхронного построения, что позволяет его отменить
    private volatile CompletableFuture<?> currentBuildFuture;

    /**
     * Конструктор сборщика GUI.
     *
     * @param engine Экземпляр движка. Если engine равен null, выбрасывается IllegalArgumentException.
     */
    public GuiBuilder(Engine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine не может быть null");
        }
        this.engine = engine;
        this.frameLoaderAdapters = new FrameLoaderAdapters(engine);
        this.frameConstructor = engine.getFrame();
        this.componentFactory = new ComponentFactory(engine);
        this.notification = new Notification();
        this.notification.setJFrame(this.frameConstructor);
        Engine.getLOGGER().debug("=== GUI BUILDER INITIATED ===");
    }

    /**
     * Синхронное построение GUI.
     *
     * @param framePath путь к файлу описания фрейма; не должен быть пустым.
     * @param parent    родительская панель, в которую добавляются компоненты.
     */
    public void buildGui(String framePath, JPanel parent) {
        if (framePath == null || framePath.isEmpty()) {
            Engine.getLOGGER().error("Frame path is empty");
            return;
        }
        if (parent == null) {
            Engine.getLOGGER().error("Parent panel is null");
            return;
        }
        Attributes frameAttributes = loadFrameAttributes(framePath);
        if (frameAttributes == null) {
            Engine.getLOGGER().error("Failed to load frame attributes from: {}", framePath);
            return;
        }
        buildPanels(frameAttributes.getGroups(), parent);
        notifyPanelsBuilt();  // Вызывается один раз после завершения построения всех панелей
    }

    /**
     * Асинхронное построение GUI.
     *
     * Загрузка атрибутов фрейма производится в фоновом потоке, а построение панелей – на EDT.
     *
     * @param framePath путь к файлу описания фрейма; не должен быть пустым.
     * @param parent    родительская панель, в которую добавляются компоненты.
     */
    public void buildGuiAsync(String framePath, JPanel parent) {
        if (framePath == null || framePath.isEmpty()) {
            Engine.getLOGGER().error("Frame path is empty");
            return;
        }
        if (parent == null) {
            Engine.getLOGGER().error("Parent panel is null");
            return;
        }
        // Сохраняем future для возможности отмены
        currentBuildFuture = CompletableFuture.supplyAsync(() -> loadFrameAttributes(framePath))
                .thenAccept(attributes -> {
                    if (attributes == null) {
                        Engine.getLOGGER().error("Frame attributes are null for path: {}", framePath);
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        buildPanels(attributes.getGroups(), parent);
                        notifyPanelsBuilt(); // Вызывается один раз после завершения построения всех панелей
                    });
                })
                .exceptionally(ex -> {
                    Engine.getLOGGER().error("Error building GUI asynchronously", ex);
                    return null;
                });
    }

    /**
     * Отменяет асинхронное построение GUI, если оно еще выполняется.
     */
    public void cancelBuild() {
        if (currentBuildFuture != null && !currentBuildFuture.isDone()) {
            currentBuildFuture.cancel(true);
            Engine.getLOGGER().info("Asynchronous GUI build canceled.");
        }
    }

    /**
     * Регистрирует слушателя для событий сборки GUI.
     *
     * @param listener слушатель.
     */
    public void addGuiBuilderListener(GuiBuilderListener listener) {
        guiBuilderListeners.add(listener);
    }

    /**
     * Удаляет ранее зарегистрированного слушателя.
     *
     * @param listener слушатель.
     */
    public void removeGuiBuilderListener(GuiBuilderListener listener) {
        guiBuilderListeners.remove(listener);
    }

    /**
     * Устанавливает слушатель, который будет вызван один раз после завершения построения всех панелей.
     *
     * @param task экземпляр Runnable, который будет выполнен по завершению построения.
     */
    public void setOnPanelsBuild(Runnable task) {
        this.onPanelsBuildTask = task;
    }

    /**
     * Позволяет зарегистрировать загрузчик атрибутов фрейма для нового типа файла.
     *
     * @param fileExtension расширение файла (например, "xml", "ini"); не должно быть пустым.
     * @param loader        загрузчик атрибутов.
     */
    public void registerFrameAttributesLoader(String fileExtension, FrameAttributesLoader loader) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            Engine.getLOGGER().error("File extension is empty.");
            return;
        }
        frameLoaderAdapters.registerAdapter(fileExtension, loader);
        Engine.getLOGGER().debug("Registered new FrameAttributesLoader for extension: {}", fileExtension);
    }

    /**
     * Очищает внутреннее состояние сборщика: карты панелей, компонентов и связи родитель-потомок.
     */
    public void resetState() {
        panelsMap.clear();
        componentsMap.clear();
        childParentMap.clear();
        loadPanels.clear();
        additionalPanelsBuilt.set(false);
        Engine.getLOGGER().info("GuiBuilder state has been reset.");
    }

    /**
     * Загрузка атрибутов фрейма в зависимости от типа файла.
     *
     * @param framePath путь к файлу конфигурации.
     * @return объект Attributes фрейма или null при ошибке загрузки.
     */
    private Attributes loadFrameAttributes(String framePath) {
        String fileType = getFileType(framePath);
        FrameAttributesLoader loader = frameLoaderAdapters.getLoader(fileType);
        if (loader == null) {
            Engine.getLOGGER().error("No loader found for file type: {}", fileType);
            return null;
        }
        return loader.getAttributes(framePath);
    }

    /**
     * Определяет тип файла по его расширению.
     *
     * @param framePath путь к файлу.
     * @return строка, обозначающая тип файла.
     */
    private String getFileType(String framePath) {
        String lowerCasePath = framePath.toLowerCase(Locale.ROOT);
        if (lowerCasePath.endsWith(".json")) {
            return "json";
        } else if (lowerCasePath.endsWith(".json5")) {
            return "json5";
        } else if (lowerCasePath.endsWith(".yaml") || lowerCasePath.endsWith(".yml")) {
            return "yaml";
        } else if (lowerCasePath.endsWith(".fxml")) {
            return "fxml";
        }
        // Расширение по умолчанию
        return "json";
    }

    /**
     * Возвращает все дочерние компоненты для указанной родительской панели.
     *
     * @param parentPanelName имя родительской панели.
     * @return список дочерних компонентов.
     */
    public List<Component> getAllChildComponents(String parentPanelName) {
        List<Component> components = new ArrayList<>();
        childParentMap.getOrDefault(parentPanelName, Collections.emptyList())
                .forEach(child -> components.addAll(componentsMap.getOrDefault(child, Collections.emptyList())));
        return components;
    }

    /**
     * Рекурсивное построение панелей.
     *
     * @param panels      карта групп панелей; может быть null.
     * @param parentPanel родительская панель.
     */
    private void buildPanels(Map<String, OptionGroups> panels, JPanel parentPanel) {
        if (panels != null) {
            panels.forEach((componentGroup, optionGroups) -> {
                if (optionGroups == null) {
                    Engine.getLOGGER().warn("OptionGroups is null for component group: {}", componentGroup);
                    return;
                }
                JPanel thisPanel = createPanel(optionGroups, componentGroup);
                processChildComponents(optionGroups.getChildComponents(), thisPanel);
                addChildPanelIfNeeded(panels, parentPanel, thisPanel, componentGroup);
                buildPanels(optionGroups.getGroups(), thisPanel);
                updateChildParentMap(parentPanel, thisPanel);
            });
        }
    }

    /**
     * Создаёт панель на основе заданных опций.
     *
     * @param optionGroups   параметры для создания панели.
     * @param componentGroup имя группы компонентов.
     * @return созданная панель.
     */
    private JPanel createPanel(OptionGroups optionGroups, String componentGroup) {
        JPanel panel = frameConstructor.getPanel().createGroupPanel(optionGroups.getPanelOptions(), componentGroup, frameConstructor);
        panel.setName(componentGroup);
        panel.setVisible(optionGroups.getPanelOptions().isVisible());
        Container panelParent = panel.getParent();
        if (panelParent != null) {
            panelParent.setComponentZOrder(panel, optionGroups.getPanelOptions().getzIndex());
        } else {
            //Engine.getLOGGER().warn("Parent for panel {} is null!", panel.getName());
        }
        return panel;
    }

    /**
     * Обрабатывает дочерние компоненты и группы, определённые в атрибутах.
     *
     * @param childComponents список дочерних атрибутов компонентов; может быть null.
     * @param parentPanel     родительская панель.
     */
    private void processChildComponents(List<ComponentAttributes> childComponents, JPanel parentPanel) {
        if (childComponents == null) {
            return;
        }
        for (ComponentAttributes componentAttributes : childComponents) {
            if (componentAttributes == null) {
                Engine.getLOGGER().warn("Found null ComponentAttributes in parent panel: {}", parentPanel.getName());
                continue;
            }
            if (componentAttributes.getComponentType() != null) {
                addComponentToParent(componentAttributes, parentPanel);
            } else if (componentAttributes.getGroups() != null) {
                buildPanels(componentAttributes.getGroups(), parentPanel);
            } else if (componentAttributes.getReadFrom() != null) {
                processReadFromAttribute(componentAttributes, parentPanel);
            } else if (componentAttributes.getLoadPanel() != null && !componentAttributes.getLoadPanel().isEmpty()) {
                loadPanels.put(componentAttributes.getLoadPanel(), parentPanel);
            }
        }
    }

    /**
     * Создаёт и добавляет компонент в родительскую панель.
     *
     * @param componentAttributes атрибуты компонента.
     * @param parentPanel         родительская панель.
     */
    private void addComponentToParent(ComponentAttributes componentAttributes, JPanel parentPanel) {
        JComponent component = componentFactory.createComponent(componentAttributes);
        if (component == null) {
            Engine.getLOGGER().warn("Component creation returned null for attributes: {}", componentAttributes);
            return;
        }
        if (component instanceof JPanel) {
            addPanelGroup(parentPanel, (JPanel) component);
            panelsMap.put(component.getName(), (JPanel) component);
        } else {
            parentPanel.add(component);
        }
        componentsMap.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(component);
    }

    /**
     * Обрабатывает атрибут readFrom для загрузки дополнительных компонентов или групп.
     *
     * @param componentAttributes атрибуты компонента.
     * @param parentPanel         родительская панель.
     */
    private void processReadFromAttribute(ComponentAttributes componentAttributes, JPanel parentPanel) {
        Attributes frameAttributes = loadFrameAttributes(componentAttributes.getReadFrom());
        if (frameAttributes == null) {
            Engine.getLOGGER().error("Failed to load attributes from: {}", componentAttributes.getReadFrom());
            return;
        }
        if (frameAttributes.getGroups() == null && frameAttributes.getChildComponents() != null) {
            processChildComponents(frameAttributes.getChildComponents(), parentPanel);
        } else {
            buildGui(componentAttributes.getReadFrom(), parentPanel);
        }
    }

    /**
     * Добавляет дочернюю панель к родительской, если такой панели еще нет в карте.
     *
     * @param panels         карта групп панелей.
     * @param parentPanel    родительская панель.
     * @param childPanel     дочерняя панель.
     * @param componentGroup имя группы компонентов.
     */
    private void addChildPanelIfNeeded(Map<String, OptionGroups> panels, JPanel parentPanel, JPanel childPanel, String componentGroup) {
        if (!panelsMap.containsKey(componentGroup)) {
            addPanelGroup(parentPanel, childPanel);
            notifyPanelBuild(panels, componentGroup, parentPanel);
        }
    }

    /**
     * Обновляет карту связи родитель-потомок.
     *
     * @param parentPanel родительская панель.
     * @param childPanel  дочерняя панель.
     */
    private void updateChildParentMap(JPanel parentPanel, JPanel childPanel) {
        String parentName = parentPanel.getName();
        if (parentName == null || parentName.isEmpty()) {
            parentName = UUID.randomUUID().toString();
            parentPanel.setName(parentName);
        }
        childParentMap.computeIfAbsent(parentName, k -> new ArrayList<>()).add(childPanel.getName());
    }

    /**
     * Построение дополнительных панелей, которые загружались отдельно.
     * Выполняется только один раз.
     */
    public void buildAdditionalPanels() {
        if (additionalPanelsBuilt.compareAndSet(false, true)) {
            Engine.getLOGGER().debug("== BUILDING ADDITIONAL PANELS ==");
            loadPanels.forEach((key, value) -> {
                notifyAdditionalPanelBuild(value);
                Engine.getLOGGER().debug("Processing additional panel key: {}", key);
                JPanel loadingPanel = panelsMap.get(key);
                if (loadingPanel != null) {
                    addPanelGroup(value, loadingPanel);
                } else {
                    Engine.getLOGGER().warn("No panel found in panelsMap for key: {}", key);
                }
            });
        } else {
            Engine.getLOGGER().error("Additional panels are already built!");
        }
    }

    /**
     * Добавляет дочернюю панель к родительской.
     *
     * @param parent родительская панель.
     * @param child  дочерняя панель.
     */
    private void addPanelGroup(JPanel parent, JPanel child) {
        if (parent == null || child == null) {
            Engine.getLOGGER().warn("Cannot add panel group because parent or child is null");
            return;
        }
        parent.add(child);
        panelsMap.put(child.getName(), child);
        updateChildParentMap(parent, child);
    }

    /**
     * Уведомляет слушателей о завершении построения всех панелей.
     */
    private void notifyPanelsBuilt() {
        for (GuiBuilderListener listener : guiBuilderListeners) {
            listener.onPanelsBuilt();
        }
        if (onPanelsBuildTask != null) {
            onPanelsBuildTask.run();
        }
    }

    /**
     * Уведомляет слушателей о построении конкретной панели.
     *
     * @param panels         карта групп панелей.
     * @param componentGroup имя группы компонентов.
     * @param parentPanel    родительская панель.
     */
    private void notifyPanelBuild(Map<String, OptionGroups> panels, String componentGroup, JPanel parentPanel) {
        for (GuiBuilderListener listener : guiBuilderListeners) {
            listener.onPanelBuild(panels, componentGroup, parentPanel);
        }
    }

    /**
     * Уведомляет слушателей о построении дополнительной панели.
     *
     * @param panel панель.
     */
    private void notifyAdditionalPanelBuild(JPanel panel) {
        for (GuiBuilderListener listener : guiBuilderListeners) {
            listener.onAdditionalPanelBuild(panel);
        }
    }

    /**
     * Возвращает карту компонентов, сгруппированных по имени панели.
     *
     * @return карта компонентов.
     */
    public Map<String, List<JComponent>> getComponentsMap() {
        return componentsMap;
    }

    /**
     * Добавляет панель в карту панелей.
     *
     * @param panel панель; должна иметь ненулевое имя.
     */
    public void addPanelToMap(JPanel panel) {
        if (panel != null && panel.getName() != null) {
            this.panelsMap.put(panel.getName(), panel);
        } else {
            Engine.getLOGGER().warn("Panel or its name is null, cannot add to panelsMap");
        }
    }

    /**
     * Возвращает карту панелей.
     *
     * @return карта панелей.
     */
    public Map<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    /**
     * Возвращает карту связи родитель-потомок для панелей.
     *
     * @return карта связи родитель-потомок.
     */
    public Map<String, List<String>> getChildParentMap() {
        return childParentMap;
    }

    /**
     * Возвращает экземпляр Engine.
     *
     * @return engine.
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Возвращает адаптеры для загрузки фреймов.
     *
     * @return frameLoaderAdapters.
     */
    public FrameLoaderAdapters getFrameLoaderAdapters() {
        return frameLoaderAdapters;
    }

    /**
     * Возвращает экземпляр Notification.
     *
     * @return notification.
     */
    public Notification getNotification() {
        return notification;
    }


    public void setGuiBuilderListener(GuiBuilderListener guiBuilderListener) {
        this.guiBuilderListener = guiBuilderListener;
    }

    /**
     * Возвращает фабрику компонентов.
     *
     * @return componentFactory.
     */
    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}
