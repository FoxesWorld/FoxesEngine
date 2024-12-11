import javax.swing.*;
import java.awt.*;
import javax.swing.SpinnerNumberModel;

public class CustomComponent extends JComponent {
    private JSlider slider;
    private JLabel label;
    private JSpinner spinner;

    public CustomComponent(String labelText, int minValue, int maxValue, int initialValue) {
        initializeComponents(labelText, minValue, maxValue, initialValue);
        configureLayout();
        addListeners();
    }

    private void initializeComponents(String labelText, int minValue, int maxValue, int initialValue) {
        slider = new JSlider(minValue, maxValue, initialValue);
        configureSlider();

        label = new JLabel(labelText);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(initialValue, minValue, maxValue, 1);
        spinner = new JSpinner(spinnerModel);
    }

    private void configureSlider() {
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing((slider.getMaximum() - slider.getMinimum()) / 2);
        slider.setMinorTickSpacing((slider.getMaximum() - slider.getMinimum()) / 10);
    }

    private void configureLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        add(label, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(slider, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(spinner, gbc);
    }

    private void addListeners() {
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                spinner.setValue(slider.getValue());
            }
        });

        spinner.addChangeListener(e -> updateSliderFromSpinner());
    }

    private void updateSliderFromSpinner() {
        int value = (Integer) spinner.getValue();
        if (value >= slider.getMinimum() && value <= slider.getMaximum()) {
            slider.setValue(value);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Custom Component Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        CustomComponent customComponent = new CustomComponent("Value:", 0, 100, 50);
        frame.add(customComponent, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
