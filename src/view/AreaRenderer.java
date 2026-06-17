package view;

import controller.CleanerController;
import model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AreaRenderer {
    private static final Set<String> ACTIVITY_AREAS = Set.of("ROOM", "RESTAURANT", "CINEMA", "FITNESS");
    private static final Set<String> FRONT_LAYER_AREAS = Set.of("LOBBY", "RECEPTION");

    private final AssetLoader assetLoader;
    private final CleanerController cleanerController;

    public AreaRenderer(AssetLoader assetLoader, CleanerController cleanerController) {
        this.assetLoader = assetLoader;
        this.cleanerController = cleanerController;
    }

    public void drawAreas(Graphics2D g2, SimulationData data, boolean frontLayerOnly) {
        if (data.areas == null) return;
        for (Area area : data.areas) {
            boolean isFrontLayer = FRONT_LAYER_AREAS.contains(area.AreaType.toUpperCase());
            if (frontLayerOnly == isFrontLayer) {
                int count = countGuests(area, data.guests, data.tileSize);
                drawArea(g2, area, count, data.tileSize, data.horizontalOffset);
            }
        }
    }

    private void drawArea(Graphics2D g2, Area area, int guestCount, int tileSize, int horizontalOffset) {
        int[] pos = area.getPos();
        int[] dim = area.getDim();
        int x = (pos[0] * tileSize) + horizontalOffset;
        int y = pos[1] * tileSize;
        int w = dim[0] * tileSize;
        int h = dim[1] * tileSize;

        String assetKey = area.AreaType.toUpperCase();
        if (assetKey.contains("SCHACHT")) assetKey = "ELEVATOR-SHAFT";
        if (assetKey.contains("TRAP")) assetKey = "STAIRS";

        // Pick image based on destruction state
        BufferedImage img;
        if (area.isDestroyed) {
            img = assetLoader.getDestroyed(assetKey);
        } else if (area.isOnFire) {
            img = assetLoader.getBurning(assetKey);
        } else {
            img = assetLoader.get(assetKey);
        }

        if (FRONT_LAYER_AREAS.contains(area.AreaType.toUpperCase())) {
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, w, h);
        }

        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        } else {
            if (assetKey.equals("RECEPTION")) g2.setColor(new Color(255, 218, 170));
            else if (assetKey.equals("LOBBY")) g2.setColor(new Color(45, 45, 45));
            else g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x, y, w, h);
        }

        // Skip overlays and labels for destroyed/burning areas — the image says it all
        if (area.isDestroyed || area.isOnFire) {
            return;
        }

        // Red overlay for occupied areas
        if (guestCount > 0 && ACTIVITY_AREAS.contains(area.AreaType.toUpperCase())) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(220, 50, 50));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);
        }

        // Green overlay for cleaning
        if (cleanerController != null) {
            List<Cleaner> cleaners = cleanerController.getActiveCleaners();
            for (Cleaner cleaner : cleaners) {
                if (cleaner.assignedRoomId == area.id && cleaner.state == CleanerState.CLEANING) {
                    Composite original = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                    g2.setColor(new Color(50, 220, 50));
                    g2.fillRect(x, y, w, h);
                    g2.setComposite(original);
                    break;
                }
            }
        }

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString(area.AreaType, x + 5, y + 15);

        if (ACTIVITY_AREAS.contains(area.AreaType.toUpperCase())) {
            drawGuestCountBadge(g2, x, y, w, guestCount);
        }
    }

    private void drawGuestCountBadge(Graphics2D g2, int x, int y, int w, int guestCount) {
        String text = String.valueOf(guestCount);
        int badgeSize = 18;
        int badgeX = x + w - badgeSize - 4;
        int badgeY = y + 4;

        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(badgeX, badgeY, badgeSize, badgeSize);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textX = badgeX + (badgeSize - fm.stringWidth(text)) / 2;
        int textY = badgeY + (badgeSize - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }

    private int countGuests(Area area, Map<Integer, Guest> guests, int tileSize) {
        if (guests == null) return 0;
        int areaX = area.getPos()[0] * tileSize;
        int areaY = area.getPos()[1] * tileSize;
        int areaW = area.getDim()[0] * tileSize;
        int areaH = area.getDim()[1] * tileSize;

        return (int) guests.values().stream().filter(g ->
                g.state == GuestState.IDLE &&
                        g.x >= areaX - 10 && g.x <= areaX + areaW + 10 &&
                        g.y >= areaY && g.y <= areaY + areaH
        ).count();
    }
}