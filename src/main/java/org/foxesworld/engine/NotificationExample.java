package org.foxesworld.engine;

import com.formdev.flatlaf.FlatIntelliJLaf;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotificationExample {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        JFrame frame = new JFrame("Notification Example");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        Notifications.getInstance().setJFrame(frame);

        JButton button = new JButton("Show Notification");
        button.setBounds(50, 50, 200, 50);
        frame.add(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Hello");
            }
        });

        frame.setVisible(true);
    }
}