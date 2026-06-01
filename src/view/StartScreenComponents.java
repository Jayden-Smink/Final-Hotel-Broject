package view;

import factory.UIFactory;
import javax.swing.*;
import java.awt.*;

public class StartScreenComponents {
    public static JPanel createBackgroundPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UIFactory.BG_TOP, 0, getHeight(), UIFactory.BG_BOTTOM));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
    }

    public static JLabel createTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Trebuchet MS", Font.BOLD, 34));
        l.setForeground(UIFactory.GOLD_LIGHT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}