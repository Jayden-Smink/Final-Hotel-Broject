package view;

import controller.CleanerController;
import model.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import model.CleanerState;

public class SimulationRenderer {
    private final SimulationData data;
    private final CleanerController cleanerController;

    private final Map<String, BufferedImage> assetCache = new HashMap<>();

    public SimulationRenderer(SimulationData data, CleanerController cleanerController) {
        loadAssets();
        this.data = data;
        this.cleanerController = cleanerController;
    }

    private void loadAssets() {
        String[] typesToLoad = {
                "ROOM", "CINEMA", "RESTAURANT", "FITNESS",
                "LOBBY", "RECEPTION", "STAIRS", "ELEVATOR-SHAFT", "ELEVATOR",
                "BACKROOMS"
        };

        for (int i = 0; i < typesToLoad.length; i++) {
            String type = typesToLoad[i];
            try {
                File file = new File("src/view/Picture/" + type.toLowerCase() + ".png");
                if (file.exists()) {
                    assetCache.put(type.toUpperCase(), ImageIO.read(file));
                    System.out.println("Geladen: " + type);
                }
            } catch (IOException e) {
                System.err.println("Fout bij laden: " + type);
            }
        }
    }

    public void render(Graphics2D g2, SimulationData data) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, 0, 2000, 2000);

        drawAreas(g2, data, false);
        drawElevator(g2, data);
        drawAreas(g2, data, true);
        drawGuests(g2, data);
        drawCleaners(g2);
    }

    private void drawAreas(Graphics2D g2, SimulationData data, boolean frontLayerOnly) {
        if (data.areas == null) return;

        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            boolean isFrontLayer = isFrontLayerArea(area.AreaType);
            if (frontLayerOnly == isFrontLayer) {
                int guestCount = countGuests(area, data.guests);
                drawArea(g2, area, guestCount);
            }
        }
    }

    private void drawElevator(Graphics2D g2, SimulationData data) {
        if (data.elevator == null) return;

        BufferedImage elevatorImg = assetCache.get("ELEVATOR");
        int elevatorWidth = 46;
        int elevatorX = data.horizontalOffset + (data.tileSize / 2) - (elevatorWidth / 2);
        int elevatorY = (int) data.elevator.curY;

        if (elevatorImg != null) {
            g2.drawImage(elevatorImg, elevatorX, elevatorY, elevatorWidth, data.tileSize - 10, null);
        } else {
            g2.setColor(new Color(60, 120, 255));
            g2.fillRoundRect(elevatorX, elevatorY, elevatorWidth, data.tileSize - 10, 10, 10);
        }
    }

    private void drawGuests(Graphics2D g2, SimulationData data) {
        if (data.guests == null) return;

        List<Guest> guestSnapshot;
        synchronized (data.guests) {
            guestSnapshot = new ArrayList<>(data.guests.values());
        }

        for (int i = 0; i < guestSnapshot.size(); i++) {
            Guest guest = guestSnapshot.get(i);
            if (guest.state != GuestState.IN_LIFT) {
                GuestRenderer.draw(g2, guest, data.horizontalOffset);
            }
        }
    }

    private void drawCleaners(Graphics2D g2) {
        if (cleanerController == null) return;

        List<Cleaner> cleaners = cleanerController.getActiveCleaners();
        for (int i = 0; i < cleaners.size(); i++) {
            Cleaner cleaner = cleaners.get(i);
            if (cleaner.state == CleanerState.CLEANING) continue;

            int drawX = (int) cleaner.x + data.horizontalOffset;
            int drawY = (int) cleaner.y;

            g2.setColor(new Color(50, 205, 50));
            g2.fillOval(drawX - 10, drawY - 10, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("C" + cleaner.id, drawX - 6, drawY - 12);
        }
    }

    private int countGuests(Area area, Map<Integer, Guest> guests) {
        if (guests == null) return 0;

        int areaX = area.getPos()[0] * data.tileSize;
        int areaY = area.getPos()[1] * data.tileSize;
        int areaW = area.getDim()[0] * data.tileSize;
        int areaH = area.getDim()[1] * data.tileSize;

        int count = 0;
        for (Guest guest : guests.values()) {
            if (guest.state == GuestState.IDLE &&
                    guest.x >= areaX - 10 &&
                    guest.x <= areaX + areaW + 10 &&
                    guest.y >= areaY &&
                    guest.y <= areaY + areaH) {
                count++;
            }
        }
        return count;
    }

    private void drawArea(Graphics2D g2, Area area, int guestCount) {
        int[] pos = area.getPos();
        int[] dim = area.getDim();

        int x = (pos[0] * data.tileSize) + data.horizontalOffset;
        int y = pos[1] * data.tileSize;
        int w = dim[0] * data.tileSize;
        int h = dim[1] * data.tileSize;

        String assetKey = area.AreaType.toUpperCase();
        if (assetKey.contains("SCHACHT")) assetKey = "ELEVATOR-SHAFT";
        if (assetKey.contains("TRAP"))    assetKey = "STAIRS";

        BufferedImage img = assetCache.get(assetKey);

        if (isFrontLayerArea(area.AreaType)) {
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, w, h);
        }

        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        } else {
            if      (assetKey.equals("RECEPTION")) g2.setColor(new Color(255, 218, 170));
            else if (assetKey.equals("LOBBY"))     g2.setColor(new Color(45, 45, 45));
            else if (assetKey.equals("BACKROOMS")) g2.setColor(new Color(75, 70, 45));
            else                                   g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x, y, w, h);
        }

        if (guestCount > 0 && isActivityArea(area.AreaType)) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(220, 50, 50));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString(area.AreaType, x + 5, y + 15);

        if (isActivityArea(area.AreaType)) {
            drawGuestCountBadge(g2, x, y, w, guestCount);
        }

        if (cleanerController != null) {
            List<Cleaner> cleaners = cleanerController.getActiveCleaners();
            for (int i = 0; i < cleaners.size(); i++) {
                Cleaner cleaner = cleaners.get(i);
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

    private boolean isActivityArea(String type) {
        String upperType = type.toUpperCase();
        return upperType.equals("ROOM") || upperType.equals("RESTAURANT") || upperType.equals("CINEMA") || upperType.equals("FITNESS") || upperType.equals("RECEPTION");
    }

    private boolean isFrontLayerArea(String type) {
        String upperType = type.toUpperCase();
        return upperType.equals("LOBBY") || upperType.equals("RECEPTION");
    }
}