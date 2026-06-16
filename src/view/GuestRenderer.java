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

        int drawX = (int) guest.x + globalOffset;
        int drawY = (int) guest.y;

        // Draw skull for dead guests — stays permanently at death position
        if (guest.isDead) {
            drawSkull(g2, drawX, drawY, "G" + getDisplayId(guest.id));
            return;
        }

        /*
         * Gast is binnen in kamer/faciliteit — niet tekenen.
         */
        if (guest.state == GuestState.IDLE && !guest.isCheckingOut) {
            return;
        }

        if (guest.state == GuestState.IN_LIFT) {
            return;
        }

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

    /** Draws a skull emoji-style symbol at the given position with a label above it. */
    public static void drawSkull(Graphics2D g2, int cx, int cy, String label) {
        // Dark circle background
        g2.setColor(new Color(40, 40, 40, 200));
        g2.fillOval(cx - 11, cy - 11, 22, 22);

        // Skull emoji text
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        FontMetrics fm = g2.getFontMetrics();
        String skull = "💀";
        int sx = cx - fm.stringWidth(skull) / 2;
        g2.drawString(skull, sx, cy + 6);

        // Label above
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        int lx = cx - fm.stringWidth(label) / 2;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(label, lx + 1, cy - 14);
        g2.setColor(new Color(255, 80, 80));
        g2.drawString(label, lx, cy - 15);
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