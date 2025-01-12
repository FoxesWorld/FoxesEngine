package org.foxesworld.engine.gui.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.button.*;
import org.foxesworld.engine.gui.components.button.Button;
import org.foxesworld.engine.gui.components.checkbox.*;
import org.foxesworld.engine.gui.components.checkbox.Checkbox;
import org.foxesworld.engine.gui.components.compositeSlider.CompositeSlider;
import org.foxesworld.engine.gui.components.dropBox.*;
import org.foxesworld.engine.gui.components.fileSelector.FileSelector;
import org.foxesworld.engine.gui.components.label.*;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.multiButton.MultiButton;
import org.foxesworld.engine.gui.components.multiButton.MultiButtonStyle;
import org.foxesworld.engine.gui.components.passfield.PassField;
import org.foxesworld.engine.gui.components.passfield.PassFieldStyle;
import org.foxesworld.engine.gui.components.progressBar.ProgressBarStyle;
import org.foxesworld.engine.gui.components.slider.Slider;
import org.foxesworld.engine.gui.components.spinner.Spinner;
import org.foxesworld.engine.gui.components.sprite.SpriteAnimation;
import org.foxesworld.engine.gui.components.textArea.AreaStyle;
import org.foxesworld.engine.gui.components.textArea.TextArea;
import org.foxesworld.engine.gui.components.textfield.TextField;
import org.foxesworld.engine.gui.components.textfield.TextFieldStyle;
import org.foxesworld.engine.gui.components.utils.tooltip.CustomTooltip;
import org.foxesworld.engine.gui.components.utils.tooltip.TooltipAttributes;
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class ComponentFactory extends JComponent {

    private final Engine engine;
    private final LanguageProvider LANG;
    private final IconUtils iconUtils;
    private final Map<String, Map<String, StyleAttributes>> componentStyles = new HashMap<>();
    private StyleAttributes style;
    private ComponentAttributes componentAttribute;
    private Rectangle bounds;
    private ComponentFactoryListener componentFactoryListener;
    private CustomTooltip customTooltip;

    public ComponentFactory(Engine engine){
        this.engine = engine;
        this.iconUtils = new IconUtils(engine);
        this.LANG = engine.getLANG();
    }

    public JComponent createComponent(ComponentAttributes componentAttributes) {
        initializeTooltip(componentAttributes);
        loadStyle(componentAttributes);
        componentFactoryListener.onComponentCreation(componentAttributes);

        bounds = componentAttributes.getBounds();
        this.componentAttribute = componentAttributes;

        JComponent component = createComponentByType(componentAttributes);
        component.setName(componentAttributes.getComponentId());
        component.setBounds(bounds);
        component.setOpaque(style.isOpaque());

        if (componentAttributes.getToolTip() != null) {
            setTooltip(component, componentAttributes);
        }

        return component;
    }

    private void initializeTooltip(ComponentAttributes componentAttributes) {
        String toolTipStyle = "default";
        if (componentAttributes.getToolTip() != null) {
            if (componentAttributes.getTooltipStyle() != null) {
                toolTipStyle = componentAttributes.getTooltipStyle();
                } else {
                Engine.LOGGER.warn("You have tooltip for #{} but no style! Using `default`", componentAttributes.getComponentId());
            }
            TooltipAttributes attributes = loadTooltipAttributes(toolTipStyle);
            customTooltip = new CustomTooltip(hexToColor(attributes.getBgColor()), hexToColor(attributes.getTextColor()), attributes.getBorderRadius(), this.getEngine().getFONTUTILS().getFont(attributes.getFont(), attributes.getFontSize()));
        }
    }

    private void loadStyle(ComponentAttributes componentAttributes) {
        if (componentAttributes.getComponentStyle() != null) {
            componentStyles.putIfAbsent(componentAttributes.getComponentType(), engine.getStyleProvider().getElementStyles().get(componentAttributes.getComponentType()));
            style = componentStyles.get(componentAttributes.getComponentType()).get(componentAttributes.getComponentStyle());
        }
    }

    private JComponent createComponentByType(ComponentAttributes componentAttributes) {
        JComponent component = null;

        switch (componentAttributes.getComponentType()) {
            case "progressBar" -> component = createProgressBar(componentAttributes);
            case "label" -> component = createLabel(componentAttributes);
            case "textArea" -> component = createTextArea(componentAttributes);
            case "checkBox" -> component = createCheckbox(componentAttributes);
            case "textField" -> component = createTextField(componentAttributes);
            case "spriteImage" -> component = createSpriteImage();
            case "passField" -> component = createPassField(componentAttributes);
            case "spinner" -> component = createSpinner(componentAttributes);
            case "button" -> component = createButton(componentAttributes);
            case "multiButton" -> component = createMultiButton(componentAttributes);
            case "dropBox" -> component = createDropBox(componentAttributes);
            case "slider" -> component = createSlider(componentAttributes);
            case "compositeSlider" -> component = createCompositeSlider(componentAttributes);
            case "fileSelector"-> component = createFileSelector(componentAttributes);
            default -> throw new IllegalArgumentException("Unsupported component type: " + componentAttributes.getComponentType());
        }

        return component;
    }

    private JComponent createProgressBar(ComponentAttributes componentAttributes) {
        ProgressBarStyle progressBarStyle = new ProgressBarStyle(this);
        JProgressBar progressBar = new JProgressBar();
        progressBarStyle.apply(progressBar);
        progressBar.setBounds(bounds);
        return progressBar;
    }

    private JComponent createLabel(ComponentAttributes componentAttributes) {
        LabelStyle labelStyle = new LabelStyle(this);
        Label label = new Label(this);
        labelStyle.apply(label);
        label.setIcon(iconUtils.getIcon(componentAttributes));
        String initial = (componentAttributes.getInitialValue() != null) ? String.valueOf(componentAttributes.getInitialValue()) : "";
        label.setText(LANG.getString(componentAttributes.getLocaleKey()) + " " + initial);
        if(!label.isGradientText()) {
            label.setForeground(hexToColor(componentAttributes.getColor()));
        }
        label.setFont(engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));
        return label;
    }

    private JComponent createTextArea(ComponentAttributes componentAttributes) {
        AreaStyle areaStyle = new AreaStyle(this);
        TextArea textArea = new TextArea(this);
        textArea.setLineWrap(componentAttributes.isLineWrap());
        areaStyle.apply(textArea);
        String initial = (componentAttributes.getInitialValue() != null) ? String.valueOf(componentAttributes.getInitialValue()) : "";
        textArea.setText(LANG.getString(componentAttributes.getLocaleKey()) + " " + initial);
        textArea.setForeground(hexToColor(componentAttributes.getColor()));
        textArea.setEditable(componentAttributes.isEnabled());
        textArea.setFont(engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));
        return textArea;
    }

    private JComponent createCheckbox(ComponentAttributes componentAttributes) {
        CheckboxStyle checkboxStyle = new CheckboxStyle(this);
        Checkbox checkbox = new Checkbox(this, LANG.getString(componentAttributes.getLocaleKey()));
        checkboxStyle.apply(checkbox);
        checkbox.setSelected(Boolean.parseBoolean(String.valueOf(componentAttributes.getInitialValue())));
        if (componentAttributes.getKeyCode() != null) {
            checkbox.setFocusable(true);
            checkbox.requestFocus();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(componentAttributes.getKeyCode());
            AbstractAction buttonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkbox.toggleCheckbox();
                    checkbox.doClick();
                }
            };

            checkbox.getActionMap().put(componentAttributes.getComponentId(), buttonAction);
            checkbox.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, componentAttributes.getComponentId());
        }
        checkbox.setEnabled(componentAttributes.isEnabled());
        return checkbox;
    }

    private JComponent createTextField(ComponentAttributes componentAttributes) {
        TextFieldStyle textFieldStyle = new TextFieldStyle(this);
        TextField textField = new TextField(this);
        textFieldStyle.apply(textField);
        textField.setActionCommand(componentAttributes.getComponentId());
        textField.addActionListener(engine);
        return textField;
    }

    private JComponent createSpriteImage() {
        return new SpriteAnimation(this);
    }

    private JComponent createPassField(ComponentAttributes componentAttributes) {
        PassFieldStyle passFieldStyle = new PassFieldStyle(this);
        PassField passField = new PassField(this, LANG.getString(componentAttributes.getLocaleKey()));
        passFieldStyle.apply(passField);
        passField.setFont(engine.getFONTUTILS().getFont(style.getFont(), style.getFontSize()));
        passField.setActionCommand(componentAttributes.getComponentId());
        return passField;
    }

    private JComponent createSpinner(ComponentAttributes componentAttributes) {
        Spinner spinner = new Spinner(Integer.parseInt((String) componentAttributes.getInitialValue()), componentAttributes.getMinValue(), componentAttributes.getMaxValue(), componentAttributes.getMajorSpacing());
        if(spinner.getSpinnerListener() != null) {
            spinner.init();
        }
        return spinner;
    }

    private JComponent createButton(ComponentAttributes componentAttributes) {
        ButtonStyle buttonStyle = new ButtonStyle(this);
        Button button = (componentAttributes.getImageIcon() != null) ? new Button(this, iconUtils.getIcon(componentAttributes)) : new Button(this, LANG.getString(componentAttributes.getLocaleKey()));
        buttonStyle.apply(button);
        button.setBounds(bounds);
        button.setActionCommand(componentAttributes.getComponentId());
        button.addActionListener(engine);
        if (componentAttributes.getKeyCode() != null) {
            button.setFocusable(true);
            button.requestFocus();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(componentAttributes.getKeyCode());
            AbstractAction buttonAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button.ButtonClick();
                    button.doClick();
                    button.setPressed(false);
                }
            };

            button.getActionMap().put(componentAttributes.getComponentId(), buttonAction);
            button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, componentAttributes.getComponentId());
            button.setEnabled(componentAttributes.isEnabled());
        }
        return button;
    }

    private JComponent createMultiButton(ComponentAttributes componentAttributes) {
        MultiButtonStyle multiButtonStyle = new MultiButtonStyle(this, componentAttributes);
        MultiButton multiButton = new MultiButton(this);
        multiButtonStyle.apply(multiButton);
        multiButton.setActionCommand(componentAttributes.getComponentId());
        multiButton.addActionListener(engine);
        return multiButton;
    }

    private JComponent createDropBox(ComponentAttributes componentAttributes) {
        DropBoxStyle dropBoxStyle = new DropBoxStyle(this);
        DropBox dropBox = new DropBox(this, componentAttributes.getBounds().y);
        dropBoxStyle.apply(dropBox);
        return dropBox;
    }

    private JComponent createSlider(ComponentAttributes componentAttributes) {
        Slider slider = new Slider(this);
        slider.setValue(Integer.parseInt((String) componentAttributes.getInitialValue()));
        return slider;
    }

    private JComponent createCompositeSlider(ComponentAttributes componentAttributes) {
        CompositeSlider compositeSlider = new CompositeSlider(this);
        compositeSlider.setValue(Integer.parseInt((String) componentAttributes.getInitialValue()));
        return compositeSlider;
    }

    private JComponent createFileSelector(ComponentAttributes componentAttributes) {
        FileSelector fileSelector = new FileSelector(this, FileSelector.SelectionMode.valueOf(componentAttributes.getSelectionMode()));
        fileSelector.setValue((String) componentAttributes.getInitialValue());
        return fileSelector;
    }

    private void setTooltip(JComponent component, ComponentAttributes componentAttributes) {
        if(componentAttributes.getToolTip() != null) {
            if(component.getBounds() != null) {
                int delay = (componentAttributes.getDelay() != 0) ? componentAttributes.getDelay() : 2000;
                customTooltip.attachToComponent(component, this.engine.getLANG().getString(componentAttributes.getToolTip()), delay);
            }
        }
    }

    private TooltipAttributes loadTooltipAttributes(String tooltipStyle) {
        JsonObject jsonObject = parseJson("assets/styles/tooltip.json");
        return jsonObject != null ? new Gson().fromJson(jsonObject.get(tooltipStyle), TooltipAttributes.class) : null;
    }

    private JsonObject parseJson(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
             InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    public void setComponentFactoryListener(ComponentFactoryListener componentFactoryListener) {
        this.componentFactoryListener = componentFactoryListener;
    }

    public interface ComponentCreationCallback {
        void onComponentCreated(JComponent component);
    }

    public interface ComponentFactoryListener {
        void onComponentCreation(ComponentAttributes componentAttributes);
    }

    public Engine getEngine() {
        return engine;
    }

    public StyleAttributes getStyle() {
        return style;
    }

    public ComponentAttributes getComponentAttribute() {
        return componentAttribute;
    }

    public IconUtils getIconUtils() {
        return iconUtils;
    }

    public CustomTooltip getCustomTooltip() {
        return customTooltip;
    }
}