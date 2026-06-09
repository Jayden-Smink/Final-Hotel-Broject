package view;

import model.Guest;
import model.GuestState;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GuestRenderer {

    /*
     * Koppelt echte scenario guestId's aan nette zichtbare labels.
     *
     * Voorbeeld:
     * echte guest.id = 7  -> zichtbaar G1
     * echte guest.id = 36 -> zichtbaar G2
     *
     * Belangrijk:
     * De interne guest.id verandert NIET.
     * Alleen het label op het scherm verandert.
     */
    private static final Map<Integer, Integer> displayIds = new HashMap<>();
    private static int nextDisplayId = 1;

    public static void draw(Graphics2D g2, Guest guest, int globalOffset) {

        if (guest == null) {
            return;
        }

        /*
         * Gast is binnen in kamer/faciliteit — niet tekenen.
         *
         * Let op:
         * Als je wachtende gasten buiten zichtbaar wilt houden, dan moeten ze NIET
         * currentActivity = "USING_FACILITY" hebben.
         */
        if (guest.state == GuestState.IDLE && !guest.isCheckingOut) {
            return;
        }

        if (guest.state == GuestState.IN_LIFT) {
            return;
        }

        int drawX = (int) guest.x + globalOffset;
        int drawY = (int) guest.y;

        if (guest.isCheckingOut) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.CYAN);
        }

        g2.fillOval(drawX - 10, drawY - 10, 20, 20);

        g2.setColor(new Color(0, 120, 140));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(drawX - 10, drawY - 10, 20, 20);

        String label = "G" + getDisplayId(guest.id);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        int labelX = drawX - (fm.stringWidth(label) / 2);
        int labelY = drawY - 12;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(label, labelX + 1, labelY + 1);

        g2.setColor(Color.WHITE);
        g2.drawString(label, labelX, labelY);
    }

    private static int getDisplayId(int realGuestId) {
        if (!displayIds.containsKey(realGuestId)) {
            displayIds.put(realGuestId, nextDisplayId);
            nextDisplayId++;
        }

        return displayIds.get(realGuestId);
    }

    /*
     * Gebruik dit bij het starten van een nieuwe simulatie.
     * Anders blijven oude display-labels bestaan als je opnieuw start zonder app-herstart.
     */
    public static void resetDisplayIds() {
        displayIds.clear();
        nextDisplayId = 1;
    }
}