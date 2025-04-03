package org.foxesworld.engine.gui.components;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс CompositeComponent реализует компоновку компонентов с использованием списка.
 * Все компоненты, из которых строится наш JComponent, хранятся в списке subComponents.
 * Помимо базовой функциональности, здесь реализована унифицированная настройка расположения
 * дочерних компонентов через объект LayoutConfig.
 */
public class CompositeComponent extends JComponent {
    // Список для хранения дочерних компонентов
    protected  ComponentFactory componentFactory;
    private final List<JComponent> subComponents;
    private final List<String> componentTypes = new ArrayList<>();
    private final Map<String, String> componentStyles = new HashMap<>();
    private String value;
    private ComponentAttributes.LayoutConfig layoutConfig;

    /**
     * Конструктор CompositeComponent.
     * Здесь можно задать нужный менеджер компоновки (например, BoxLayout по вертикали).
     */
    public CompositeComponent() {
        Engine.LOGGER.warn("Experimental Composite Component!");
        subComponents = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    protected void initComponents(Map<String, String> components){
        for(Map.Entry<String, String> compInst: components.entrySet()){
            componentTypes.add(compInst.getKey());
            componentStyles.put(compInst.getKey(), compInst.getValue());
        }
    }

    /**
     * Добавляет новый компонент в список и на форму.
     *
     * @param component компонент для добавления
     */
    public void addSubComponent(JComponent component) {
        subComponents.add(component);
        add(component);
        updateView();
    }

    /**
     * Удаляет компонент из списка и с формы.
     *
     * @param component компонент для удаления
     */
    public void removeSubComponent(JComponent component) {
        subComponents.remove(component);
        remove(component);
        updateView();
    }

    /**
     * Возвращает список всех дочерних компонентов.
     *
     * @return список дочерних компонентов
     */
    public List<JComponent> getSubComponents() {
        return subComponents;
    }

    /**
     * Очищает список дочерних компонентов и удаляет их с формы.
     */
    public void clearSubComponents() {
        subComponents.clear();
        removeAll();
        updateView();
    }

    /**
     * Унифицированное обновление представления.
     */
    private void updateView() {
        revalidate();
        repaint();
    }

    /**
     * Фабричный метод для создания панели (JPanel) с заданным менеджером компоновки и прозрачностью.
     *
     * @param layout менеджер компоновки для панели
     * @param opaque флаг непрозрачности (false - панель прозрачная)
     * @return настроенная панель
     */
    public JPanel createPanel(LayoutManager layout, boolean opaque) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(opaque);
        return panel;
    }

    /**
     * Устанавливает конфигурацию расположения для данного компонента.
     * Это позволит каждому композитному компоненту использовать единый механизм позиционирования.
     *
     * @param config объект конфигурации макета
     */
    public void setLayoutConfig(ComponentAttributes.LayoutConfig config) {
        this.layoutConfig = config;
    }

    /**
     * Возвращает конфигурацию расположения для данного компонента.
     *
     * @return объект конфигурации макета
     */
    public ComponentAttributes.LayoutConfig getLayoutConfig() {
        return this.layoutConfig;
    }

    /**
     * Унифицированный метод для применения конфигурации расположения к дочернему компоненту.
     * Метод устанавливает его границы на основании переданной конфигурации.
     *
     * @param component дочерний компонент
     * @param config    конфигурация расположения для этого компонента
     */
    protected void applyLayoutConfig(JComponent component, ComponentAttributes.ComponentConfig config) {
        component.setBounds(config.getX(), config.getY(), config.getWidth(), config.getHeight());
    }

    protected List<String> getComponentTypes() {
        return componentTypes;
    }

    protected Map<String, String> getComponentStyles() {
        return componentStyles;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
}
