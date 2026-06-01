package view;

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

    private final Map<String, BufferedImage> assetCache = new HashMap<>();

    // Area types that get the occupied overlay and guest count badge
    private static final Set<String> ACTIVITY_AREAS = Set.of(
            "ROOM", "RESTAURANT", "CINEMA", "FITNESS", "RECEPTION"
    );

    /*
     * Deze areas worden NA de elevator getekend.
     * Daardoor liggen ze visueel bovenop de elevator.
     */
    private static final Set<String> FRONT_LAYER_AREAS = Set.of(
            "LOBBY", "RECEPTION"
    );

    public SimulationRenderer(SimulationData data) {
        loadAssets();
        this.data = data;
    }

    private void loadAssets() {
        String[] typesToLoad = {
                "ROOM", "CINEMA", "RESTAURANT", "FITNESS",
                "LOBBY", "RECEPTION", "STAIRS", "ELEVATOR-SHAFT", "ELEVATOR",
                "BACKROOMS"
        };

        for (String type : typesToLoad) {
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

        // 1. Teken alle areas behalve LOBBY en RECEPTION
        drawAreas(g2, data, false);

        // 2. Teken elevator
        drawElevator(g2, data);

        // 3. Teken LOBBY en RECEPTION bovenop de elevator
        drawAreas(g2, data, true);

        // 4. Teken gasten
        drawGuests(g2, data);

        // 5. Teken schoonmaker
        drawCleaner(g2, data);

    }

    private void drawAreas(Graphics2D g2, SimulationData data, boolean frontLayerOnly) {
        if (data.areas == null) return;

        for (Area a : data.areas) {
            boolean isFrontLayer = FRONT_LAYER_AREAS.contains(a.AreaType.toUpperCase());

            if (frontLayerOnly == isFrontLayer) {
                int count = countGuests(a, data.guests);
                drawArea(g2, a, count);
            }
        }
    }

    private void drawElevator(Graphics2D g2, SimulationData data) {
        if (data.elevator == null) return;

        BufferedImage elevatorImg = assetCache.get("ELEVATOR");
        int elevatorWidth = 46;
        int elevatorX = data.horizontalOffset + (data.tileSize / 2) - (elevatorWidth / 2);
        int curY = (int) data.elevator.curY;

        if (elevatorImg != null) {
            g2.drawImage(elevatorImg, elevatorX, curY, elevatorWidth, data.tileSize - 10, null);
        } else {
            g2.setColor(new Color(60, 120, 255));
            g2.fillRoundRect(elevatorX, curY, elevatorWidth, data.tileSize - 10, 10, 10);
        }
    }

    private void drawGuests(Graphics2D g2, SimulationData data) {
        if (data.guests == null) return;

        List<Guest> guestSnapshot;
        synchronized (data.guests) {
            guestSnapshot = new ArrayList<>(data.guests.values());
        }

        for (Guest g : guestSnapshot) {
            if (g.state != GuestState.IN_LIFT) {
                GuestRenderer.draw(g2, g, data.horizontalOffset);
            }
        }
    }
    private void drawCleaner(Graphics2D g2, SimulationData data) {
        if (data.cleaner == null || data.cleaner.state == CleanerState.CLEANING) return;

        int drawX = (int) data.cleaner.x + data.horizontalOffset;
        int drawY = (int) data.cleaner.y;

        g2.setColor(new Color(50, 205, 50));
        g2.fillOval(drawX - 10, drawY - 10, 20, 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("C", drawX - 4, drawY - 12);
    }

    /**
     * Telt het aantal gasten (IDLE) binnen de grenzen van deze area.
     */
    private int countGuests(Area a, Map<Integer, Guest> guests) {
        if (guests == null) return 0;

        int areaX = a.getPos()[0] * data.tileSize;
        int areaY = a.getPos()[1] * data.tileSize;
        int areaW = a.getDim()[0] * data.tileSize;
        int areaH = a.getDim()[1] * data.tileSize;

        return (int) guests.values().stream().filter(g ->
                g.state == GuestState.IDLE &&
                        g.x >= areaX - 10 &&
                        g.x <= areaX + areaW + 10 &&
                        g.y >= areaY &&
                        g.y <= areaY + areaH
        ).count();
    }

    private void drawArea(Graphics2D g2, Area a, int guestCount) {
        int[] pos = a.getPos();
        int[] dim = a.getDim();

        int x = (pos[0] * data.tileSize) + data.horizontalOffset;
        int y = pos[1] * data.tileSize;
        int w = dim[0] * data.tileSize;
        int h = dim[1] * data.tileSize;

        String assetKey = a.AreaType.toUpperCase();

        if (assetKey.contains("SCHACHT")) assetKey = "ELEVATOR-SHAFT";
        if (assetKey.contains("TRAP")) assetKey = "STAIRS";

        BufferedImage img = assetCache.get(assetKey);

        /*
         * Als LOBBY of RECEPTION transparante pixels heeft,
         * kan de elevator anders nog zichtbaar blijven.
         * Daarom tekenen we eerst een donkere basislaag.
         */
        if (FRONT_LAYER_AREAS.contains(a.AreaType.toUpperCase())) {
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, w, h);
        }

        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        } else {
            if (assetKey.equals("RECEPTION")) {
                g2.setColor(new Color(255, 218, 170));
            } else if (assetKey.equals("LOBBY")) {
                g2.setColor(new Color(45, 45, 45));
            } else if (assetKey.equals("BACKROOMS")) {
                g2.setColor(new Color(75, 70, 45));
            } else {
                g2.setColor(Color.DARK_GRAY);
            }

            g2.fillRect(x, y, w, h);
        }

        // Rood transparant overlay voor bezette activiteitsruimtes
        if (guestCount > 0 && ACTIVITY_AREAS.contains(a.AreaType.toUpperCase())) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(220, 50, 50));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);
        }

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString(a.AreaType, x + 5, y + 15);

        // Gast-telbadge rechtsbovenhoek, alleen voor activiteitsruimtes
        if (ACTIVITY_AREAS.contains(a.AreaType.toUpperCase())) {
            drawGuestCountBadge(g2, x, y, w, guestCount);
        }

        // Green overlay — cleaner is cleaning this room
        if (data.cleaner != null &&
                data.cleaner.assignedRoomId == a.id &&
                data.cleaner.state == CleanerState.CLEANING) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(50, 220, 50));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);
        }


    }


    /**
     * Tekent een klein badge-cirkel met het aantal gasten in de rechterbovenhoek van de area.
     */
    private void drawGuestCountBadge(Graphics2D g2, int x, int y, int w, int guestCount) {
        String text = String.valueOf(guestCount);

        int badgeSize = 18;
        int badgeX = x + w - badgeSize - 4;
        int badgeY = y + 4;

        // Achtergrond: donker met lichte rand
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(badgeX, badgeY, badgeSize, badgeSize);

        // Getal
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        int textX = badgeX + (badgeSize - fm.stringWidth(text)) / 2;
        int textY = badgeY + (badgeSize - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }
}