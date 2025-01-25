package org.foxesworld.engine.gui.adapters.xml;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.FrameAttributesLoader;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.Bounds;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class XmlFrameAttributesLoader implements FrameAttributesLoader {
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Override
    public Attributes getAttributes(String framePath) {
        Engine.LOGGER.warn("USING EXPERIMENTAL Xml ADAPTER");
        try (InputStream inputStream = XmlFrameAttributesLoader.class.getClassLoader().getResourceAsStream(framePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + framePath);
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            // Парсим XML-документ и возвращаем компонент
            return parseDocument(document, ComponentAttributes.class);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Failed to load frame attributes from path: " + framePath, e);
        }
    }

    private <T> T parseDocument(Document document, Class<T> classOfT) {
        try {
            T instance = classOfT.getDeclaredConstructor().newInstance();
            Element root = document.getDocumentElement();

            if (root != null) {
                // Парсим атрибуты компонента
                populateAttributes(root, instance);

                // Если есть вложенные компоненты, парсим их тоже
                NodeList components = root.getElementsByTagName("component");
                for (int i = 0; i < components.getLength(); i++) {
                    Element componentElement = (Element) components.item(i);
                    ComponentAttributes component = parseComponentAttributes(componentElement);
                    if (instance instanceof ComponentAttributes) {
                        ((ComponentAttributes) instance).addChild(component);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
    }

    private <T> void populateAttributes(Element element, T instance) throws IllegalAccessException {
        // Чтение атрибутов элемента
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Attr attribute = (Attr) element.getAttributes().item(i);
            String attributeName = attribute.getName();
            String attributeValue = attribute.getValue();

            // Попытка найти поле с таким именем
            try {
                Field field = instance.getClass().getDeclaredField(attributeName);
                field.setAccessible(true);
                field.set(instance, convertValue(field.getType(), attributeValue));
            } catch (NoSuchFieldException e) {
            }
        }
    }

    private ComponentAttributes parseComponentAttributes(Element componentElement) {
        ComponentAttributes component = new ComponentAttributes();
        try {
            // Заполнение атрибутов для компонента
            populateAttributes(componentElement, component);

            // Разбираем вложенные элементы (например, panelOptions)
            NodeList panelOptionsList = componentElement.getElementsByTagName("panelOptions");
            if (panelOptionsList.getLength() > 0) {
                Element panelOptionsElement = (Element) panelOptionsList.item(0);
                populateAttributes(panelOptionsElement, component); // Заполнение panelOptions
            }

            // Разбираем bounds
            NodeList boundsList = componentElement.getElementsByTagName("bounds");
            if (boundsList.getLength() > 0) {
                Element boundsElement = (Element) boundsList.item(0);
                populateBounds(boundsElement, component);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse component", e);
        }
        return component;
    }

    private void populateBounds(Element boundsElement, ComponentAttributes component) {
        // Пример извлечения атрибутов для bounds
        String x = boundsElement.getAttribute("x");
        String y = boundsElement.getAttribute("y");
        String width = boundsElement.getAttribute("width");
        String height = boundsElement.getAttribute("height");

        component.setBounds(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(width), Integer.parseInt(height));
    }

    private Object convertValue(Class<?> type, String value) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        }
        return value;
    }
}