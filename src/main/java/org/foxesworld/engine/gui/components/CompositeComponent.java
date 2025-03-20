package org.foxesworld.engine.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс CompositeComponent реализует компоновку компонентов с использованием списка.
 * Все компоненты, из которых строится наш JComponent, хранятся в списке subComponents.
 */
public class CompositeComponent extends JComponent {
    // Список для хранения дочерних компонентов
    private final List<JComponent> subComponents;

    /**
     * Конструктор CompositeComponent.
     * Здесь можно задать нужный менеджер компоновки (например, BoxLayout по вертикали).
     */
    public CompositeComponent() {
        subComponents = new ArrayList<>();
        // Пример: располагаем дочерние компоненты вертикально
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
}
