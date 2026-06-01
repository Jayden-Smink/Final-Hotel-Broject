package view;

import model.Guest;
import model.GuestState;
import java.awt.*;

public class GuestRenderer {

    public static void draw(Graphics2D g2, Guest g, int globalOffset) {

        // Gast is binnen (kamer/faciliteit) — niet tekenen
        if (g.state == GuestState.IDLE && !g.isCheckingOut) {
            return;
        }

        int drawX = (int) g.x + globalOffset;
        int drawY = (int) g.y;

        if (g.isCheckingOut) {
            g2.setColor(Color.RED);
        } else if (g.state == GuestState.IN_LIFT) {
            return;
        } else {
            g2.setColor(Color.CYAN);
        }

        g2.fillOval(drawX - 10, drawY - 10, 20, 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("G" + g.id, drawX - 8, drawY - 12);
    }
}