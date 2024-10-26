package org.foxesworld.engine.gui.components;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.attributes.ComponentAttributes;
import org.foxesworld.engine.gui.components.button.Button;
import org.foxesworld.engine.gui.components.button.ButtonStyle;
import org.foxesworld.engine.gui.components.checkbox.Checkbox;
import org.foxesworld.engine.gui.components.checkbox.CheckboxStyle;
import org.foxesworld.engine.gui.components.label.Label;
import org.foxesworld.engine.gui.components.label.LabelStyle;
import org.foxesworld.engine.gui.components.multiButton.MultiButton;
import org.foxesworld.engine.gui.components.multiButton.MultiButtonStyle;
import org.foxesworld.engine.gui.components.passfield.PassField;
import org.foxesworld.engine.gui.components.passfield.PassFieldStyle;
import org.foxesworld.engine.gui.components.progressBar.ProgressBarStyle;
import org.foxesworld.engine.gui.components.scrollBox.ScrollBox;
import org.foxesworld.engine.gui.components.scrollBox.ScrollBoxStyle;
import org.foxesworld.engine.gui.components.sprite.SpriteAnimation;
import org.foxesworld.engine.gui.components.textfield.TextField;
import org.foxesworld.engine.gui.components.textfield.TextFieldStyle;
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class ComponentFactory {

    public Engine engine;
    private LanguageProvider LANG;
    private Map<String, Map<String, StyleAttributes>> componentStyles = new HashMap<>();
    private TextFieldStyle textfieldStyle;
    private PassFieldStyle passfieldStyle;
    private ProgressBarStyle progressBarStyle;
    private LabelStyle labelStyle;
    private ButtonStyle buttonStyle;
    private CheckboxStyle checkboxStyle;
    private MultiButtonStyle multiButtonStyle;
    private ComponentAttributes componentAttributes;
    private ScrollBoxStyle scrollBoxStyle;
    public StyleAttributes style = null;

    public ComponentFactory(Engine engine){
        this.engine = engine;
        this.LANG = engine.getLANG();
    }
    public JComponent createComponent(ComponentAttributes componentAttributes) {

        if(componentAttributes.componentType != null && componentAttributes.componentStyle != null) {
            if(componentStyles.get(componentAttributes.componentType) == null){
                componentStyles.put(componentAttributes.componentType, engine.getStyleProvider().getElementStyles().get(componentAttributes.componentType));
            }
            style = componentStyles.get(componentAttributes.componentType).get(componentAttributes.componentStyle);
        }
        this.componentAttributes = componentAttributes;
        String[] bounds = componentAttributes.bounds.split(",");
        int xPos = Integer.parseInt(bounds[0]);
        int yPos = Integer.parseInt(bounds[1]);
        int width = Integer.parseInt(bounds[2]);
        int height = Integer.parseInt(bounds[3]);
        switch (componentAttributes.componentType) {

            case "progressBar":
                progressBarStyle = new ProgressBarStyle(style);
                JProgressBar progressBar = new JProgressBar();
                progressBarStyle.apply(progressBar);
                progressBar.setName(componentAttributes.componentId);
                progressBar.setBounds(xPos, yPos, width, height);
                return progressBar;

            case "label":
                labelStyle = new LabelStyle(this);
                Label label = new Label(LANG.getString(componentAttributes.localeKey));
                labelStyle.apply(label);
                if(componentAttributes.imageIcon != null) {
                    label.setIcon(new ImageIcon(ImageUtils.getScaledImage(ImageUtils.getLocalImage(componentAttributes.imageIcon), componentAttributes.iconWidth, componentAttributes.iconHeight)));
                }
                label.setFont(this.engine.getFontUtils().getFont(style.font, componentAttributes.fontSize));
                labelStyle.apply(label);
                if(componentAttributes.getColor() != null) {label.setForeground(hexToColor(componentAttributes.getColor()));}
                label.setName(componentAttributes.componentId);
                label.setBounds(xPos, yPos, width, height);
                if(componentAttributes.initialValue != null) {
                    if (!componentAttributes.initialValue.equals("")) {
                        label.setText(this.engine.getEngineData().getUpdaterVersion());
                    }
                }
                return label;

            case "checkBox":
                checkboxStyle = new CheckboxStyle(this);
                Checkbox checkbox = new Checkbox(LANG.getString(componentAttributes.localeKey));
                checkboxStyle.apply(checkbox);
                checkbox.setBounds(xPos, yPos, width, height);
                checkbox.setName(componentAttributes.componentId);
                checkbox.setEnabled(componentAttributes.enabled);
                return checkbox;

            case "textField":
                textfieldStyle = new TextFieldStyle(this);
                TextField textfield = new TextField(LANG.getString(componentAttributes.localeKey));
                textfieldStyle.apply(textfield);
                textfield.setName(componentAttributes.componentId);
                textfield.setBounds(xPos, yPos, textfieldStyle.width, textfieldStyle.height);
                textfield.setActionCommand(componentAttributes.componentId);
                //textfield.addActionListener(engine);
                return textfield;

            case "passField":
                passfieldStyle = new PassFieldStyle(this);
                PassField passfield = new PassField(LANG.getString(componentAttributes.localeKey));
                passfieldStyle.apply(passfield);
                passfield.setName(componentAttributes.componentId);
                passfield.setBounds(xPos, yPos, style.width, style.height);
                passfield.setFont(this.engine.getFontUtils().getFont(style.font, style.fontSize));
                passfield.setActionCommand(componentAttributes.componentId);
                return passfield;

            case "spriteImage":
                SpriteAnimation spriteAnimation = new SpriteAnimation(this);
                spriteAnimation.setBounds(xPos,yPos,width,height);
                spriteAnimation.setName(componentAttributes.componentId);
                return  spriteAnimation;

            case "button":
                buttonStyle = new ButtonStyle(this);
                Button button = new Button(LANG.getString(componentAttributes.localeKey));
                if(componentAttributes.imageIcon != null){
                    ImageIcon icon = new ImageIcon(ImageUtils.getScaledImage(ImageUtils.getLocalImage(componentAttributes.imageIcon), componentAttributes.iconWidth, componentAttributes.iconHeight));
                    button = new Button(icon);
                }
                buttonStyle.apply(button);
                button.setName(componentAttributes.localeKey);
                button.setActionCommand(componentAttributes.componentId);
                button.setBounds(xPos, yPos, width, height);
                button.addActionListener(engine);
                return button;

            case "multiButton":
                multiButtonStyle = new MultiButtonStyle(style, componentAttributes);
                MultiButton multiButton = new MultiButton();
                multiButtonStyle.apply(multiButton);
                multiButton.setName(componentAttributes.componentId);
                multiButton.setActionCommand(componentAttributes.componentId);
                multiButton.setBounds(xPos, yPos, style.width, style.height);
                multiButton.addActionListener(engine);
                return multiButton;

            case "scrollBox":
                scrollBoxStyle = new ScrollBoxStyle(this);
                ScrollBox scrollBox = new ScrollBox(this, new String[]{"GG", "WP"}, yPos);
                scrollBoxStyle.apply(scrollBox);
                scrollBox.setBounds(xPos,yPos, width,height);
                scrollBox.setName(componentAttributes.localeKey);
                return  scrollBox;

            default: throw new IllegalArgumentException("Unsupported component type: " + componentAttributes.componentType);
        }
    }

    public ComponentAttributes getComponentAttributes() {
        return componentAttributes;
    }
}
