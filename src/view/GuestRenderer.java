package view;

import model.Guest;
import model.GuestState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GuestRenderer {

    private static final Map<Integer, Integer> displayIds = new HashMap<>();
    private static int nextDisplayId = 1;

    /** Backward-compatible overload used by tests (no AssetLoader). */
    public static void draw(Graphics2D g2, Guest guest, int globalOffset) {
        draw(g2, guest, globalOffset, null);
    }

    public static void draw(Graphics2D g2, Guest guest, int globalOffset, AssetLoader assetLoader) {

        if (guest == null) return;

        int drawX = (int) guest.x + globalOffset;
        int drawY = (int) guest.y;

        // Draw dead-guest image (stays permanently at death position)
        if (guest.state == GuestState.DEAD) {
            drawDeadPerson(g2, drawX, drawY, "G" + getDisplayId(guest.id),
                    assetLoader != null ? assetLoader.get("DEAD-GUEST") : null);
            return;
        }

        // Not visible while idle inside a room/facility
        if (guest.state == GuestState.IDLE && !guest.isCheckingOut) return;
        if (guest.state == GuestState.IN_LIFT) return;

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

    /** Draws the dead-person image (or fallback skull emoji) with a label. */
    public static void drawDeadPerson(Graphics2D g2, int cx, int cy, String label, BufferedImage img) {
        int size = 28;
        if (img != null) {
            g2.drawImage(img, cx - size / 2, cy - size / 2, size, size, null);
        } else {
            // Fallback: dark circle + skull emoji
            g2.setColor(new Color(40, 40, 40, 200));
            g2.fillOval(cx - 11, cy - 11, 22, 22);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("💀", cx - 8, cy + 6);
        }

        // Label above
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int lx = cx - fm.stringWidth(label) / 2;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(label, lx + 1, cy - size / 2);
        g2.setColor(new Color(255, 80, 80));
        g2.drawString(label, lx, cy - size / 2 - 1);
    }

    private static int getDisplayId(int realGuestId) {
        if (!displayIds.containsKey(realGuestId)) {
            displayIds.put(realGuestId, nextDisplayId);
            nextDisplayId++;
        }
        return displayIds.get(realGuestId);
    }

    public static void resetDisplayIds() {
        displayIds.clear();
        nextDisplayId = 1;
    }
}
