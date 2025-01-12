package org.foxesworld.engine.gui.styles;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class StyleApplier {
    private String[] components;
    public StyleApplier(Engine engine){
        this.components = engine.getEngineData().getStyles();
    }
    private final Map<Class<? extends JComponent>, BiConsumer<JComponent, StyleAttributes>> styleHandlers = new HashMap<>();

    /**
     * Регистрирует обработчик стиля для указанного типа компонента.
     *
     * @param componentClass  Класс компонента (например, JButton.class).
     * @param handler         Обработчик стиля (лямбда или метод).
     */
    public <T extends JComponent> void registerStyleHandler(Class<T> componentClass, BiConsumer<T, StyleAttributes> handler) {
        styleHandlers.put(componentClass, (BiConsumer<JComponent, StyleAttributes>) handler);
    }

    /**
     * Применяет стиль к компоненту, используя зарегистрированный обработчик.
     *
     * @param component       Компонент, к которому нужно применить стиль.
     * @param styleAttributes Стиль для применения.
     */
    public void applyStyle(JComponent component, StyleAttributes styleAttributes) {
        if (component == null || styleAttributes == null) {
            throw new IllegalArgumentException("Component and StyleAttributes cannot be null.");
        }

        BiConsumer<JComponent, StyleAttributes> handler = styleHandlers.get(component.getClass());
        if (handler != null) {
            handler.accept(component, styleAttributes);
        } else {
            throw new UnsupportedOperationException("No style handler registered for component type: " + component.getClass());
        }
    }
}
