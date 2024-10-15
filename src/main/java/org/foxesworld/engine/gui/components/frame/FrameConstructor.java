package org.foxesworld.engine.gui.components.frame;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.panel.Panel;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FrameConstructor extends JFrame {
    private final Engine engine;
    private Panel panel;
    private Dimension screenSize;
    private JPanel rootPanel;
    private final LanguageProvider LANG;

    public FrameConstructor(Engine engine) {
        engine.getLOGGER().info("FrameConstructor initialization");
        this.engine = engine;
        this.LANG = engine.getLANG();
        buildFrame("assets/frames/frame.json");
    }

    private void buildFrame(String path){
        Gson gson = new Gson();
        FrameAttributes frameAttributes;
        InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8);
        frameAttributes = gson.fromJson(reader, FrameAttributes.class);
        buildFrame(frameAttributes);
    }

    public void buildFrame(FrameAttributes frameAttributes) {
        Engine.getLOGGER().info("Building FrameConstructor...");
        if(!frameAttributes.getAppIcon().endsWith(".svg")) {
            setIconImage(this.engine.getImageUtils().getLocalImage(frameAttributes.getAppIcon()));
        } else {
            setIconImage(new FlatSVGIcon(frameAttributes.getAppIcon(), 1).getImage());
        }
        setTitle(LANG.getString(frameAttributes.getAppTitle()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(frameAttributes.getWidth(), frameAttributes.getHeight());
        setResizable(frameAttributes.isResizable());
        setUndecorated(frameAttributes.isUndecorated());
        if(frameAttributes.getBorderRadius() != 0) {
            this.setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), frameAttributes.getBorderRadius(), frameAttributes.getBorderRadius()));
        }

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        panel = new Panel(this);
        this.rootPanel = panel.setRootPanel(frameAttributes);
        this.rootPanel.setName("rootPanel");
        setContentPane(this.rootPanel);
        setVisible(true);
    }

    public Dimension getScreenSize() {
        return screenSize;
    }
    public JPanel getRootPanel() {
        return this.rootPanel;
    }
    public Engine getAppFrame() {
        return engine;
    }
    public Panel getPanel() {
        return panel;
    }

    public void setFrameSize(int width, int height){
        this.setSize(width, height);
    }
}
