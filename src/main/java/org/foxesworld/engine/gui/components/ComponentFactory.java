package org.foxesworld.engine.gui.components;

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
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.IconUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

@SuppressWarnings("unused")
public class ComponentFactory {

    public Engine engine;
    private final LanguageProvider LANG;
    private final IconUtils iconUtils;
    private final Map<String, Map<String, StyleAttributes>> componentStyles = new HashMap<>();
    public StyleAttributes style = null;
    private ComponentAttributes componentAttribute;
    private  Bounds bounds;
    private ComponentFactoryListener componentFactoryListener;

    public ComponentFactory(Engine engine){
        this.engine = engine;
        this.iconUtils = new IconUtils(engine);
        this.LANG = engine.getLANG();
    }
    public JComponent createComponent(ComponentAttributes componentAttributes) {
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

        switch (componentAttributes.getComponentType()) {

            case "progressBar" -> {
                ProgressBarStyle progressBarStyle = new ProgressBarStyle(this);
                component = new JProgressBar();
                progressBarStyle.apply((JProgressBar) component);
                component.setName(componentAttributes.getComponentId());
                component.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
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
                component.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
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
                component = new DropBox(this,  bounds.getY());
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
                component.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
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
            component.setToolTipText(this.engine.getLANG().getString(componentAttributes.getToolTip()));
        }
        component.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());

        return  component;
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
    public Bounds getBounds() {
        return bounds;
    }
    public ComponentAttributes getComponentAttribute() {
        return componentAttribute;
    }
}
