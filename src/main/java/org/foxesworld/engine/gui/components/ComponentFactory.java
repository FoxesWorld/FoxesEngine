package org.foxesworld.engine.gui.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.button.Button;
import org.foxesworld.engine.gui.components.button.ButtonStyle;
import org.foxesworld.engine.gui.components.checkbox.Checkbox;
import org.foxesworld.engine.gui.components.checkbox.CheckboxStyle;
import org.foxesworld.engine.gui.components.dropBox.DropBox;
import org.foxesworld.engine.gui.components.dropBox.DropBoxStyle;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.label.LabelStyle;
import org.foxesworld.engine.gui.components.multiButton.MultiButton;
import org.foxesworld.engine.gui.components.multiButton.MultiButtonStyle;
import org.foxesworld.engine.gui.components.passfield.PassField;
import org.foxesworld.engine.gui.components.passfield.PassFieldStyle;
import org.foxesworld.engine.gui.components.progressBar.ProgressBarStyle;
import org.foxesworld.engine.gui.components.serverBox.ServerBox;
import org.foxesworld.engine.gui.components.serverBox.ServerBoxStyle;
import org.foxesworld.engine.gui.components.slider.Slider;
import org.foxesworld.engine.gui.components.slider.TexturedSliderUI;
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

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

@SuppressWarnings("unused")
public class ComponentFactory extends JComponent {

    public Engine engine;
    private final LanguageProvider LANG;
    private final IconUtils iconUtils;
    private final Map<String, Map<String, StyleAttributes>> componentStyles = new HashMap<>();
    public StyleAttributes style = null;
    private ComponentAttributes componentAttribute;
    private Rectangle bounds;
    private ComponentFactoryListener componentFactoryListener;
    private CustomTooltip customTooltip;

    public ComponentFactory(Engine engine){
        this.engine = engine;
        this.iconUtils = new IconUtils(engine);
        this.LANG = engine.getLANG();
    }

    public void createComponentAsync(ComponentAttributes componentAttributes, ComponentCreationCallback callback) {
        new SwingWorker<JComponent, Void>() {
            @Override
            protected JComponent doInBackground() {
                return createComponent(componentAttributes);
            }

            @Override
            protected void done() {
                try {
                    JComponent component = get();
                    SwingUtilities.invokeLater(() -> {
                        callback.onComponentCreated(component);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public JComponent createComponent(ComponentAttributes componentAttributes) {
        customTooltip = new CustomTooltip(hexToColor("#000000c4"), Color.WHITE, 15, new Font("Arial", Font.PLAIN, 12));
        componentFactoryListener.onComponentCreation(componentAttributes);
        if(componentAttributes.getComponentStyle() != null && componentAttributes.getComponentStyle() != null) {
            if(componentStyles.get(componentAttributes.getComponentStyle()) == null){
                componentStyles.put(componentAttributes.getComponentType(), engine.getStyleProvider().getElementStyles().get(componentAttributes.getComponentType()));
            }
            style = componentStyles.get(componentAttributes.getComponentType()).get(componentAttributes.getComponentStyle());
        }
        JComponent component;
        bounds = componentAttributes.getBounds();
        this.componentAttribute = componentAttributes;

        if (this.componentAttribute.getToolTip() != null) {
            String tooltipStyle = "default";
            if (this.componentAttribute.getTooltipStyle() != null) {
                tooltipStyle = this.componentAttribute.getTooltipStyle();
            }
                TooltipAttributes attributes = loadTooltipAttributes(tooltipStyle);
                if (attributes != null) {
                    Color bgColor = hexToColor(attributes.getBgColor());
                    Color textColor = hexToColor(attributes.getTextColor());
                    Font font = this.engine.getFONTUTILS().getFont(attributes.getFont(), attributes.getFontSize());
                    customTooltip = new CustomTooltip(bgColor, textColor, attributes.getBorderRadius(), font);
                }
        }

        switch (componentAttributes.getComponentType()) {

            case "progressBar" -> {
                ProgressBarStyle progressBarStyle = new ProgressBarStyle(this);
                component = new JProgressBar();
                progressBarStyle.apply((JProgressBar) component);
                component.setName(componentAttributes.getComponentId());
                component.setBounds(bounds);
            }

            case "label" -> {
                LabelStyle labelStyle = new LabelStyle(this);
                component = new Label(this);
                labelStyle.apply((Label) component);
                if(componentAttributes.getImageIcon() != null) {
                    ImageIcon icon = this.iconUtils.getIcon(componentAttributes);
                    ((Label) component).setIcon(icon);
                }

                component.setFont(this.engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));

                if(componentAttributes.getInitialValue() != null) {
                    ((Label) component).setText(LANG.getString(componentAttributes.getLocaleKey()) + " " + componentAttributes.getInitialValue());
                }

                if(componentAttributes.getColor() != null) {
                    component.setForeground(hexToColor(componentAttributes.getColor()));
                }
            }

            case "textArea" -> {
                AreaStyle areaStyle = new AreaStyle(this);
                component = new TextArea(this);
                ((TextArea)component).setLineWrap(componentAttributes.isLineWrap());
                areaStyle.apply((TextArea) component);
                component.setFont(this.engine.getFONTUTILS().getFont(style.getFont(), componentAttributes.getFontSize()));

                if(componentAttributes.getInitialValue() != null) {
                    ((TextArea) component).setText(LANG.getString(componentAttributes.getLocaleKey()) + " " + componentAttributes.getInitialValue());
                }

                if(componentAttributes.getColor() != null) {
                    component.setForeground(hexToColor(componentAttributes.getColor()));
                }
                ((TextArea) component).setEditable(componentAttributes.isEnabled());
            }

            case "checkBox" -> {
                CheckboxStyle checkboxStyle = new CheckboxStyle(this);
                component = new Checkbox(this, LANG.getString(componentAttributes.getLocaleKey()));
                checkboxStyle.apply((Checkbox) component);
                if(componentAttributes.getInitialValue() != null) {
                    ((Checkbox)component).setSelected(Boolean.parseBoolean(String.valueOf(componentAttributes.getInitialValue())));
                }
                if (componentAttributes.getKeyCode() != null) {
                    component.setFocusable(true);
                    component.requestFocus();
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(componentAttributes.getKeyCode());
                    AbstractAction buttonAction = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ((Checkbox)component).toggleCheckbox();
                            ((Checkbox)component).doClick();
                        }
                    };

                    component.getActionMap().put(componentAttributes.getComponentId(), buttonAction);
                    component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, componentAttributes.getComponentId());
                    component.setEnabled(componentAttributes.isEnabled());
                }
            }

            case "textField" -> {
                TextFieldStyle textfieldStyle = new TextFieldStyle(this);
                component = new TextField(this);
                textfieldStyle.apply((TextField) component);
                ((TextField)component).setActionCommand(componentAttributes.getComponentId());
                ((TextField)component).addActionListener(engine);
                if(componentAttributes.getInitialValue() != null) ((TextField)component).setText(String.valueOf(componentAttributes.getInitialValue()));
            }

            case "spriteImage" -> component = new SpriteAnimation(this);

            case "passField" -> {
                PassFieldStyle passfieldStyle = new PassFieldStyle(this);
                component = new PassField(this, LANG.getString(componentAttributes.getLocaleKey()));
                passfieldStyle.apply((PassField) component);
                component.setFont(this.engine.getFONTUTILS().getFont(style.getFont(), style.getFontSize()));
                ((PassField)component).setActionCommand(componentAttributes.getComponentId());
            }

            case "spinner" -> {
                component = new Spinner(Integer.parseInt((String) componentAttributes.getInitialValue()), componentAttributes.getMinValue(), componentAttributes.getMaxValue(), componentAttributes.getMajorSpacing());
            }

            case "button" -> {
                ButtonStyle buttonStyle = new ButtonStyle(this);
                if (componentAttributes.getImageIcon() != null) {
                    ImageIcon icon = iconUtils.getIcon(componentAttributes);
                    component = new Button(this, icon);
                } else {
                    component = new Button(this, LANG.getString(componentAttributes.getLocaleKey()));
                }
                component.setBounds(bounds);
                buttonStyle.apply((Button) component);
                ((Button)component).setActionCommand(componentAttributes.getComponentId());
                ((Button) component).addActionListener(engine);
                if (componentAttributes.getKeyCode() != null) {
                    component.setFocusable(true);
                    component.requestFocus();
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(componentAttributes.getKeyCode());
                    AbstractAction buttonAction = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ((Button) component).ButtonClick();
                            ((Button) component).doClick();
                            ((Button) component).setPressed(false);
                        }
                    };

                    component.getActionMap().put(componentAttributes.getComponentId(), buttonAction);
                    component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, componentAttributes.getComponentId());
                    component.setEnabled(componentAttributes.isEnabled());
                }
            }

            case "multiButton" -> {
                MultiButtonStyle multiButtonStyle = new MultiButtonStyle(this, componentAttributes);
                component = new MultiButton(this);
                multiButtonStyle.apply((MultiButton) component);
                ((MultiButton) component).setActionCommand(componentAttributes.getComponentId());
                ((MultiButton) component).addActionListener(engine);
            }

            case "dropBox" -> {
                DropBoxStyle dropBoxStyle = new DropBoxStyle(this);
                component = new DropBox(this, (int) bounds.getY());
                dropBoxStyle.apply((DropBox) component);
                component.repaint();
            }

            case "serverBox" -> {
                ServerBoxStyle serverBoxStyle = new ServerBoxStyle(this);
                component = new ServerBox();
                ((ServerBox) component).updateBox(componentAttributes.getComponentId(), this.engine.getImageUtils().getLocalImage(style.getTexture()).getSubimage(16, 0, 16, 16));
                serverBoxStyle.apply((ServerBox) component);
                component.setBackground(hexToColor(componentAttributes.getColor()));
                component.setForeground(hexToColor(componentAttributes.getColor()));
            }

            case "slider" -> {
                component = new Slider(this);
                component.setBounds(bounds);
                ((Slider) component).setMinimum(componentAttributes.getMinValue());
                ((Slider) component).setMaximum(componentAttributes.getMaxValue());
                if(componentAttributes.getInitialValue() != null) {
                    ((Slider) component).setValue(Integer.parseInt(String.valueOf(componentAttributes.getInitialValue())));
                }
                ((Slider) component).setMajorTickSpacing(componentAttributes.getMajorSpacing());
                ((Slider) component).setMinorTickSpacing(componentAttributes.getMinorSpacing());
                ((Slider) component).setPaintTicks(true);
                ((Slider) component).setPaintLabels(true);
                ((Slider) component).setSnapToTicks(true);
                if(!Objects.equals(style.getThumbImage(), "") & !Objects.equals(style.getTrackImage(), "")) {
                    ((Slider) component).setUI(new TexturedSliderUI(this, (Slider) component, style.getThumbImage(), style.getTrackImage()));
                }
            }

            default -> throw new IllegalArgumentException("Unsupported component type: " + componentAttributes.getComponentType());
        }

        component.setName(componentAttributes.getComponentId());
        component.setOpaque(style.isOpaque());
        if(componentAttributes.getToolTip() != null) {
            customTooltip.attachToComponent(component, this.engine.getLANG().getString(componentAttributes.getToolTip()));
        }
        component.setBounds(bounds);

        return  component;
    }

    @Override
    public void setToolTipText(String text) {
        String oldText = getToolTipText();
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setInitialDelay(1);

        if (text != null) {
            if (oldText == null) {
                toolTipManager.registerComponent(this);
            }
        } else {
            toolTipManager.unregisterComponent(this);
        }
    }


    public void setComponentFactoryListener(ComponentFactoryListener componentFactoryListener) {
        this.componentFactoryListener = componentFactoryListener;
    }
    public enum Align {
        LEFT, CENTER, RIGHT
    }
    public LanguageProvider getLANG() {
        return LANG;
    }
    public IconUtils getIconUtils() {
        return iconUtils;
    }
    public Rectangle getBounds() {
        return bounds;
    }
    public ComponentAttributes getComponentAttribute() {
        return componentAttribute;
    }

    public CustomTooltip getCustomTooltip() {
        return customTooltip;
    }

    public interface ComponentCreationCallback {
        void onComponentCreated(JComponent component);
    }

    private TooltipAttributes loadTooltipAttributes(String styleKey) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("assets/styles/tooltip.json");
             InputStreamReader reader = new InputStreamReader(inputStream)) {

            if (inputStream == null) {
                System.out.println("Could not find the JSON file.");
                return null;
            }

            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject attributes = jsonObject.getAsJsonObject(styleKey);

            if (attributes != null) {
                Gson gson = new Gson();
                return gson.fromJson(attributes, TooltipAttributes.class);
            } else {
                System.out.println("Style key not found: " + styleKey);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
