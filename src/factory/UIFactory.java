package factory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class UIFactory {

    public static final Color BG_TOP     = new Color(10, 20, 40);
    public static final Color BG_BOTTOM  = new Color(25, 50, 80);
    public static final Color GOLD       = new Color(212, 175, 55);
    public static final Color GOLD_LIGHT = new Color(255, 223, 100);
    public static final Color TEXT_WHITE = new Color(240, 235, 220);

    public static JButton createStyledButton(String text, boolean primary) {

        JButton btn = new JButton(text) {

            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(hovered ? new Color(50, 90, 140) : new Color(30, 60, 100));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                if (primary) {
                    g2.setColor(GOLD);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }

                g2.setColor(primary ? GOLD_LIGHT : TEXT_WHITE);
                g2.setFont(new Font("Tahoma", Font.BOLD, 14));

                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,
                        (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2
                );

                g2.dispose();
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(350, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        return btn;
    }
}