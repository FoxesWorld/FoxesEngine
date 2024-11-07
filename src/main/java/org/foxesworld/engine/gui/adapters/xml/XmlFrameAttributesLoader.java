package org.foxesworld.engine.gui.adapters.xml;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.FrameAttributesLoader;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;

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
            Document document = builder.parse(new InputSource(new InputStreamReader(inputStream)));

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
                populateFields(root, instance);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
    }

    private <T> void populateFields(Element element, T instance) throws IllegalAccessException {
        NodeList childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String fieldName = childElement.getTagName();
                String fieldValue = childElement.getTextContent();

                try {
                    Field field = instance.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, convertValue(field.getType(), fieldValue));
                } catch (NoSuchFieldException e) {
                    // Field not found in the class, ignoring.
                }
            }
        }
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
