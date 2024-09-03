package org.foxesworld.engine.gui.components.frame;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.panel.Panel;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.utils.CurrentMonth;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Frame {
    private Engine engine;
    private Panel panel;
    private Dimension screenSize;
    private JPanel rootPanel;
    private final JFrame frame;
    private final LanguageProvider LANG;

    public Frame(Engine engine) {
        engine.getLOGGER().info("Frame initialization");
        this.engine = engine;
        this.frame = new JFrame();
        this.LANG = engine.getLANG();
        buildFrame("assets/frames/frame.json");
    }

    private void buildFrame(String path){
        Gson gson = new Gson();
        FrameAttributes frameAttributes;
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Engine.class.getClassLoader().getResourceAsStream(path)), StandardCharsets.UTF_8);
        frameAttributes = gson.fromJson(reader, FrameAttributes.class);
        buildFrame(frameAttributes);
    }

    public void buildFrame(FrameAttributes frameAttributes) {
        engine.getLOGGER().info("Building Frame...");

        frame.setIconImage(ImageUtils.getLocalImage(frameAttributes.getAppIcon()));
        frame.setTitle(LANG.getString(frameAttributes.getAppTitle()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameAttributes.getWidth(), frameAttributes.getHeight());
        frame.setResizable(frameAttributes.isResizable());
        frame.setUndecorated(frameAttributes.isUndecorated());
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
        panel = new Panel(this);
        this.rootPanel = panel.setRootPanel(frameAttributes);
        this.rootPanel.setName("rootPanel");
        frame.setContentPane(this.rootPanel);
        frame.setVisible(true);
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

    public JFrame getFrame() {
        return frame;
    }

    public Panel getPanel() {
        return panel;
    }
}
