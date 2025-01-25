package org.foxesworld.engine.gui.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.button.Button;
import org.foxesworld.engine.gui.components.button.ButtonStyle;
import org.foxesworld.engine.gui.components.checkbox.Checkbox;
import org.foxesworld.engine.gui.components.checkbox.CheckboxStyle;
import org.foxesworld.engine.gui.components.compositeSlider.CompositeSlider;
import org.foxesworld.engine.gui.components.dropBox.DropBox;
import org.foxesworld.engine.gui.components.dropBox.DropBoxStyle;
import org.foxesworld.engine.gui.components.fileSelector.FileSelector;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.label.LabelStyle;
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
import java.awt.Rectangle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class ComponentFactory extends JComponent {

    private final Engine engine;
    private final LanguageProvider langProvider;
    private final IconUtils iconUtils;
    private final Map<String, Map<String, StyleAttributes>> componentStyles = new HashMap<>();
    private final Map<String, Function<ComponentAttributes, JComponent>> componentRegistry = new HashMap<>();
    private StyleAttributes style;
    private ComponentAttributes componentAttribute;
    private ComponentFactoryListener componentFactoryListener;
    private Rectangle bounds;

    public ComponentFactory(Engine engine) {
        this.engine = engine;
        this.langProvider = engine.getLANG();
        this.iconUtils = new IconUtils(engine);

        registerComponent("label", this::createLabel);
        registerComponent("progressBar", this::createProgressBar);
        registerComponent("button", this::createButton);
        registerComponent("textArea", this::createTextArea);
        registerComponent("checkBox", this::createCheckbox);
        registerComponent("textField", this::createTextField);
        registerComponent("spriteImage", this::createSpriteImage);
        registerComponent("passField", this::createPassField);
        registerComponent("spinner", this::createSpinner);
        registerComponent("multiButton", this::createMultiButton);
        registerComponent("dropBox", this::createDropBox);
        registerComponent("slider", this::createSlider);
        registerComponent("compositeSlider", this::createCompositeSlider);
        registerComponent("fileSelector", this::createFileSelector);
    }

    public void registerComponent(String type, Function<ComponentAttributes, JComponent> creator) {
        componentRegistry.put(type, creator);
        Engine.LOGGER.info("    - Registered component: {}", type);
    }

    public CompletableFuture<JComponent> createComponentAsync(ComponentAttributes attributes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createComponent(attributes);
            } catch (Exception e) {
                Engine.LOGGER.error("Error creating component: {}", attributes.getComponentType(), e);
                return null;
            }
        });
    }

    public JComponent createComponent(ComponentAttributes attributes) {
        this.componentAttribute = attributes;
        this.bounds = attributes.getBounds().getBounds();
        loadStyle(attributes);
        String componentType = attributes.getComponentType();
        componentFactoryListener.onComponentCreation(attributes);
        Function<ComponentAttributes, JComponent> creator = componentRegistry.get(componentType);

        if (creator == null) {
            throw new IllegalArgumentException("Unsupported component type: " + componentType);
        }

        JComponent component = creator.apply(attributes);
        component.setName(attributes.getComponentId());
        component.setBounds(attributes.getBounds());
        component.setOpaque(style != null && style.isOpaque());

        if (attributes.getToolTip() != null) {
            initializeTooltip(component, attributes);
        }


        return component;
    }

    private void initializeTooltip(JComponent component, ComponentAttributes attributes) {
        String toolTipStyle = attributes.getTooltipStyle() != null ? attributes.getTooltipStyle() : "default";
        TooltipAttributes tooltipAttributes = loadTooltipAttributes(toolTipStyle);

        if (tooltipAttributes != null) {
            CustomTooltip tooltip = new CustomTooltip(
                    hexToColor(tooltipAttributes.getBgColor()),
                    hexToColor(tooltipAttributes.getTextColor()),
                    tooltipAttributes.getBorderRadius(),
                    engine.getFONTUTILS().getFont(tooltipAttributes.getFont(), tooltipAttributes.getFontSize())
            );
            tooltip.attachToComponent(component, langProvider.getString(attributes.getToolTip()), 2000);
        }
    }

    private void loadStyle(ComponentAttributes attributes) {
        String componentType = attributes.getComponentType();
        String componentStyle = attributes.getComponentStyle();

        if (componentStyle != null) {
            componentStyles.putIfAbsent(componentType, engine.getStyleProvider().getElementStyles().get(componentType));
            this.style = componentStyles.get(componentType).get(componentStyle);
        }
    }

    private TooltipAttributes loadTooltipAttributes(String styleName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("assets/styles/tooltip.json");
             InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return new Gson().fromJson(jsonObject.get(styleName), TooltipAttributes.class);
        } catch (Exception e) {
            Engine.LOGGER.error("Failed to load tooltip attributes for style: {}", styleName, e);
            return null;
        }
    }

    private JComponent createLabel(ComponentAttributes componentAttributes) {
        LabelStyle labelStyle = new LabelStyle(this);
        Label label = new Label(this);
        labelStyle.apply(label);
        label.setIcon(iconUtils.getIcon(componentAttributes));
        String initial = (componentAttributes.getInitialValue() != null) ? String.valueOf(componentAttributes.getInitialValue()) : "";
        label.setText(this.getEngine().getLANG().getString(componentAttributes.getLocaleKey()) + " " + initial);
        if(!label.isGradientText()) {
            label.setForeground(hexToColor(componentAttributes.getColor()));
        }
        label.setFont(engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));
        return label;
    }

    private JComponent createProgressBar(ComponentAttributes componentAttributes) {
        ProgressBarStyle progressBarStyle = new ProgressBarStyle(this);
        JProgressBar progressBar = new JProgressBar();
        progressBarStyle.apply(progressBar);
        progressBar.setBounds(bounds);
        return progressBar;
    }
    private JComponent createButton(ComponentAttributes componentAttributes) {
        ButtonStyle buttonStyle = new ButtonStyle(this);
        Button button = (componentAttributes.getImageIcon() != null) ? new Button(this, iconUtils.getIcon(componentAttributes)) : new Button(this, this.getEngine().getLANG().getString(componentAttributes.getLocaleKey()));
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

    private JComponent createTextArea(ComponentAttributes componentAttributes) {
        AreaStyle areaStyle = new AreaStyle(this);
        TextArea textArea = new TextArea(this);
        textArea.setLineWrap(componentAttributes.isLineWrap());
        areaStyle.apply(textArea);
        String initial = (componentAttributes.getInitialValue() != null) ? String.valueOf(componentAttributes.getInitialValue()) : "";
        textArea.setText(this.getEngine().getLANG().getString(componentAttributes.getLocaleKey()) + " " + initial);
        textArea.setForeground(hexToColor(componentAttributes.getColor()));
        textArea.setEditable(componentAttributes.isEnabled());
        textArea.setFont(engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));
        return textArea;
    }

    private JComponent createCheckbox(ComponentAttributes componentAttributes) {
        CheckboxStyle checkboxStyle = new CheckboxStyle(this);
        Checkbox checkbox = new Checkbox(this, this.getEngine().getLANG().getString(componentAttributes.getLocaleKey()));
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

    private JComponent createSpriteImage(ComponentAttributes attributes) {
        return new SpriteAnimation(this);
    }

    private JComponent createPassField(ComponentAttributes componentAttributes) {
        PassFieldStyle passFieldStyle = new PassFieldStyle(this);
        PassField passField = new PassField(this, this.getEngine().getLANG().getString(componentAttributes.getLocaleKey()));
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

    public Engine getEngine() {
        return engine;
    }

    public LanguageProvider getLangProvider() {
        return langProvider;
    }

    public IconUtils getIconUtils() {
        return iconUtils;
    }

    public Map<String, Map<String, StyleAttributes>> getComponentStyles() {
        return componentStyles;
    }

    public Map<String, Function<ComponentAttributes, JComponent>> getComponentRegistry() {
        return componentRegistry;
    }

    public StyleAttributes getStyle() {
        return style;
    }

    public ComponentAttributes getComponentAttribute() {
        return componentAttribute;
    }

    public void setComponentFactoryListener(ComponentFactoryListener componentFactoryListener) {
        this.componentFactoryListener = componentFactoryListener;
    }
}
