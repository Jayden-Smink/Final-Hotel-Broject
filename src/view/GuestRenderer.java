package view;

import model.Guest;
import model.GuestState;
import java.awt.*;

public class GuestRenderer {

    public static void draw(Graphics2D g2, Guest guest, int globalOffset) {

        // Gast is binnen (kamer/faciliteit) — niet tekenen
        if (guest.state == GuestState.IDLE && !guest.isCheckingOut) {
            return;
        }

        int drawX = (int) guest.x + globalOffset;
        int drawY = (int) guest.y;

        if (guest.isCheckingOut) {
            g2.setColor(Color.RED);
        } else if (guest.state == GuestState.IN_LIFT) {
            return;
        } else {
            g2.setColor(Color.CYAN);
        }

        g2.fillOval(drawX - 10, drawY - 10, 20, 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("G" + guest.id, drawX - 8, drawY - 12);
    }
}