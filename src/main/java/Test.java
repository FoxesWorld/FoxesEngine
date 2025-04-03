import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.ComponentValue;
import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactoryListener;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.engine.gui.styles.StyleProvider;
import org.foxesworld.engine.locale.LanguageProvider;
import org.foxesworld.engine.sound.Sound;
import org.foxesworld.engine.utils.Crypt.CryptUtils;
import org.foxesworld.engine.utils.IconUtils;
import org.foxesworld.engine.utils.ServerInfo;
import org.foxesworld.engine.utils.hook.HookException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;

public class Test extends Engine {
    public Test(int poolSize, String worker, Map<String, Class<?>> configFiles) {
        super(poolSize, worker, configFiles);
        preInit();
        init();
    }

    public static void main(String[] args){
        new Test(10, "forge", null);
    }
    private void buildGui(String[] styles) {
        setStyleProvider(new StyleProvider(styles));
        setGuiBuilder(new GuiBuilder(this));
        getGuiBuilder().getComponentFactory().setComponentFactoryListener(new InitialValue(this));
        getGuiBuilder().addGuiBuilderListener(this);
        getGuiBuilder().buildGuiAsync(getFileProperties().getFrameTpl(), getFrame().getRootPanel());
        setIconUtils(new IconUtils(this));
    }

    @Override
    public void init() {
        safeSubmitTask(() -> {
            buildGui(getEngineData().getStyles());
            loadMainPanel(getFileProperties().getMainFrame());
        }, "init");
    }

    @Override
    protected void preInit() {
        System.setProperty("AppDir", System.getenv("APPDATA"));
        System.setProperty("RamAmount", String.valueOf(Runtime.getRuntime().maxMemory() / 45));
        try {
            if (getPreInitHooks().hook(null, null)) {
                LOGGER.info("Pre-init hooks прервали инициализацию");
                return;
            }
        } catch (HookException e) {
            LOGGER.error("Ошибка в pre-init hooks", e);
        }

        this.LANG = new LanguageProvider(this, getFileProperties().getLocaleFile(), 0);
        this.SOUND = new Sound(this, getClass().getClassLoader().getResourceAsStream(getFileProperties().getSoundsFile()));
        this.frameConstructor = new FrameConstructor(this);
        this.CRYPTO = new CryptUtils();
        this.frameConstructor.setFocusStatusListener(this);
    }

    @Override
    protected void postInit() {

    }

    @Override
    public void onPanelsBuilt() {

    }

    @Override
    public void onAdditionalPanelBuild(JPanel panel) {

    }

    @Override
    public void onGuiBuilt() {
        this.getFrame().repaint();
    }

    @Override
    public void onPanelBuild(Map<String, OptionGroups> panels, String componentGroup, Container parentPanel) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void updateFocus(boolean hasFocus) {

    }

    public static class InitialValue extends ComponentValue implements ComponentFactoryListener {

        private int count;
        private final Test launcher;
        public InitialValue(Test launcher) {
            super(launcher);
            this.launcher = launcher;
        }

        @Override
        public void onComponentCreation(ComponentAttributes componentAttributes) {
            if (componentAttributes.getInitialValue() != null) {
                this.setInitialData(componentAttributes);
            }
            count+=1;
        }

        @Override
        public void setInitialData(ComponentAttributes componentAttributes) {
            String[] splitValue = String.valueOf(componentAttributes.getInitialValue()).split("#");
            switch (splitValue[0]) {
                case "version" -> componentAttributes.setInitialValue(this.launcher.getEngineData().getLauncherVersion());
                case "build" -> componentAttributes.setInitialValue(this.launcher.getEngineData().getLauncherBuild());
            }
        }
    }
}
