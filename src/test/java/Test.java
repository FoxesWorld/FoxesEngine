import javax.swing.*;
import java.awt.*;

public class Test extends JFrame {

    public static void main(String[] args){
        new Test();
    }

    public  Test(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 768);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new FlowLayout(FlowLayout.LEADING));
        this.setVisible(true);
        JButton button = new JButton("Show");
        button.setVisible(true);
        getContentPane().add(button);
    }
}
