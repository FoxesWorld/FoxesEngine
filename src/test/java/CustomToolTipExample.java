import org.foxesworld.engine.gui.components.utils.tooltip.CustomTooltip;

import javax.swing.*;
import java.awt.*;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

class CustomToolTipExample {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Custom Tooltip Example");
        frame.setLayout(new FlowLayout());

        JButton button = new JButton("Hover over me");
        JTextField textField = new JTextField(15);
        JLabel label = new JLabel("Hover over this label");

        // Создаем кастомные тултипы с параметрами стилей
        CustomTooltip customTooltip = new CustomTooltip(hexToColor("#000000c4"), Color.WHITE, 15, new Font("Arial", Font.PLAIN, 12));
        customTooltip.attachToComponent(button, "This is a button tooltip");

        customTooltip.attachToComponent(textField, "This is a text field tooltip");

        customTooltip.attachToComponent(label, "This is a label tooltip");

        frame.add(button);
        frame.add(textField);
        frame.add(label);

        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
