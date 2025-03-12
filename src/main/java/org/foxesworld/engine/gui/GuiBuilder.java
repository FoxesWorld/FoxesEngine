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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
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

    // Слушатель, который выполнится по завершению построения всех панелей (однократно)
    private Runnable onPanelsBuildTask;

    // Флаг, защищённый от гонок, для контроля одноразовой сборки дополнительных панелей
    private final AtomicBoolean additionalPanelsBuilt = new AtomicBoolean(false);

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
     * @param framePath путь к файлу описания фрейма
     * @param parent    родительская панель, в которую добавляются компоненты
     */
    public void buildGui(String framePath, JPanel parent) {
        if (framePath == null || framePath.isEmpty()) {
            Engine.getLOGGER().error("Frame path is null or empty");
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
     * <p>
     * Загрузка атрибутов фрейма производится в фоновом потоке, а построение панелей – на EDT.
     *
     * @param framePath путь к файлу описания фрейма
     * @param parent    родительская панель, в которую добавляются компоненты
     */
    public void buildGuiAsync(String framePath, JPanel parent) {
        if (framePath == null || framePath.isEmpty()) {
            Engine.getLOGGER().error("Frame path is null or empty");
            return;
        }
        if (parent == null) {
            Engine.getLOGGER().error("Parent panel is null");
            return;
        }
        CompletableFuture.supplyAsync(() -> loadFrameAttributes(framePath))
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
     * Устанавливает слушатель, который будет вызван один раз после завершения построения всех панелей.
     *
     * @param task экземпляр Runnable, который будет выполнен по завершению построения
     */
    public void setOnPanelsBuild(Runnable task) {
        this.onPanelsBuildTask = task;
    }

    /**
     * Загрузка атрибутов фрейма в зависимости от типа файла.
     *
     * @param framePath путь к файлу конфигурации
     * @return Attributes объекта фрейма или null при ошибке
     */
    private Attributes loadFrameAttributes(String framePath) {
        String fileType = getFileType(framePath);
        FrameAttributesLoader loader = this.frameLoaderAdapters.getLoader(fileType);
        if (loader == null) {
            Engine.getLOGGER().error("No loader found for file type: {}", fileType);
            return null;
        }
        return loader.getAttributes(framePath);
    }

    private String getFileType(String framePath) {
        if (framePath.endsWith(".json")) {
            return "json";
        } else if (framePath.endsWith(".json5")) {
            return "json5";
        } else if (framePath.endsWith(".yaml") || framePath.endsWith(".yml")) {
            return "yaml";
        } else if (framePath.endsWith(".fxml")) {
            return "fxml";
        }
        return "json";
    }

    public List<Component> getAllChildComponents(String parentPanel) {
        List<Component> components = new ArrayList<>();
        childParentMap.getOrDefault(parentPanel, Collections.emptyList())
                .forEach(child -> components.addAll(componentsMap.getOrDefault(child, Collections.emptyList())));
        return components;
    }

    /**
     * Рекурсивное построение панелей.
     *
     * @param panels      карта групп панелей
     * @param parentPanel родительская панель для добавления созданных панелей
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

    private JPanel createPanel(OptionGroups optionGroups, String componentGroup) {
        JPanel panel = frameConstructor.getPanel().createGroupPanel(optionGroups.getPanelOptions(), componentGroup, frameConstructor);
        panel.setName(componentGroup);
        panel.setVisible(optionGroups.getPanelOptions().isVisible());
        Container panelParent = panel.getParent();
        if (panelParent != null) {
            panelParent.setComponentZOrder(panel, optionGroups.getPanelOptions().getzIndex());
        } else {
            Engine.getLOGGER().warn("Parent for panel {} is null!", panel.getName());
        }
        return panel;
    }

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

    private void addChildPanelIfNeeded(Map<String, OptionGroups> panels, JPanel parentPanel, JPanel childPanel, String componentGroup) {
        if (!panelsMap.containsKey(componentGroup)) {
            addPanelGroup(parentPanel, childPanel);
            notifyPanelBuild(panels, componentGroup, parentPanel);
        }
    }

    private void updateChildParentMap(JPanel parentPanel, JPanel childPanel) {
        // Если у родительской панели не задано имя, генерируем его
        String parentName = parentPanel.getName();
        if (parentName == null || parentName.isEmpty()) {
            parentName = UUID.randomUUID().toString();
            parentPanel.setName(parentName);
        }
        childParentMap.computeIfAbsent(parentName, k -> new ArrayList<>()).add(childPanel.getName());
    }

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
     * Вызывается один раз после завершения построения всех панелей.
     * Выполняет вызов методов слушателей, если они установлены.
     */
    private void notifyPanelsBuilt() {
        if (guiBuilderListener != null) {
            guiBuilderListener.onPanelsBuilt();
        }
        if (onPanelsBuildTask != null) {
            onPanelsBuildTask.run();
        }
    }

    private void notifyPanelBuild(Map<String, OptionGroups> panels, String componentGroup, JPanel parentPanel) {
        if (guiBuilderListener != null) {
            guiBuilderListener.onPanelBuild(panels, componentGroup, parentPanel);
        }
    }

    private void notifyAdditionalPanelBuild(JPanel panel) {
        if (guiBuilderListener != null) {
            guiBuilderListener.onAdditionalPanelBuild(panel);
        }
    }

    public Map<String, List<JComponent>> getComponentsMap() {
        return componentsMap;
    }

    public void addPanelToMap(JPanel panel) {
        if (panel != null && panel.getName() != null) {
            this.panelsMap.put(panel.getName(), panel);
        } else {
            Engine.getLOGGER().warn("Panel or its name is null, cannot add to panelsMap");
        }
    }

    public Map<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    public Map<String, List<String>> getChildParentMap() {
        return childParentMap;
    }

    public void setGuiBuilderListener(GuiBuilderListener guiBuilderListener) {
        this.guiBuilderListener = guiBuilderListener;
    }

    public Engine getEngine() {
        return engine;
    }

    public FrameLoaderAdapters getFrameLoaderAdapters() {
        return frameLoaderAdapters;
    }

    public Notification getNotification() {
        return notification;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}
