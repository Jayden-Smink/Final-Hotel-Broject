package view;

import model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class GodzillaRenderer {
    private final int tileSize;
    private final int horizontalOffset;
    private final int hotelHeightPx;   // pixel height from top of hotel to bottom of lobby
    private final int lobbyBottomPx;   // pixel Y of the bottom edge of the lobby row
    private BufferedImage godzillaImg;

    /**
     * @param tileSize         pixels per tile
     * @param horizontalOffset left margin in pixels
     * @param hotelGridHeight  number of grid rows (including lobby row)
     */
    public GodzillaRenderer(int tileSize, int horizontalOffset, int hotelGridHeight) {
        this.tileSize        = tileSize;
        this.horizontalOffset = horizontalOffset;
        this.hotelHeightPx   = hotelGridHeight * tileSize;
        this.lobbyBottomPx   = hotelHeightPx + tileSize; // bottom of lobby tile

        try {
            File file = new File("src/view/Picture/godzilla.png");
            if (file.exists()) {
                godzillaImg = ImageIO.read(file);
            }
        } catch (Exception e) {
            System.err.println("Godzilla afbeelding niet gevonden, fallback gebruikt.");
        }
    }

    public void render(Graphics2D g2, SimulationData data, GodzillaModel godzilla) {
        if (godzilla == null || !godzilla.isActive) return;

//        drawDestroyedAreas(g2, data);
//        drawFireAreas(g2, data);
//        drawDeadGuests(g2, data);
        drawGodzilla(g2, godzilla);
    }

//    private void drawDestroyedAreas(Graphics2D g2, SimulationData data) {
//        for (Area a : data.areas) {
//            if (!a.isDestroyed) continue;
//
//            int x = (a.getPos()[0] * tileSize) + horizontalOffset;
//            int y = a.getPos()[1] * tileSize;
//            int w = a.getDim()[0] * tileSize;
//            int h = a.getDim()[1] * tileSize;
//
//            Composite original = g2.getComposite();
//            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
//            g2.setColor(new Color(20, 20, 20));
//            g2.fillRect(x, y, w, h);
//            g2.setComposite(original);
//
//            g2.setColor(new Color(150, 0, 0));
//            g2.setStroke(new BasicStroke(3));
//            g2.drawLine(x, y, x + w, y + h);
//            g2.drawLine(x + w, y, x, y + h);
//        }
//    }
//
//    private void drawFireAreas(Graphics2D g2, SimulationData data) {
//        for (Area a : data.areas) {
//            if (!a.isOnFire) continue;
//
//            int x = (a.getPos()[0] * tileSize) + horizontalOffset;
//            int y = a.getPos()[1] * tileSize;
//            int w = a.getDim()[0] * tileSize;
//            int h = a.getDim()[1] * tileSize;
//
//            Composite original = g2.getComposite();
//            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
//            g2.setColor(new Color(255, 80, 0));
//            g2.fillRect(x, y, w, h);
//            g2.setComposite(original);
//
//            g2.setFont(new Font("Arial", Font.BOLD, 20));
//            g2.setColor(Color.YELLOW);
//            g2.drawString("🔥", x + w / 2 - 10, y + h / 2);
//        }
//    }
//
//    private void drawDeadGuests(Graphics2D g2, SimulationData data) {
//        for (Guest g : data.guests.values()) {
//            if (!g.isDead) continue;
//
//            int drawX = (int) g.x + horizontalOffset;
//            int drawY = (int) g.y;
//
//            g2.setColor(Color.RED);
//            g2.setStroke(new BasicStroke(2));
//            g2.drawLine(drawX - 10, drawY - 10, drawX + 10, drawY + 10);
//            g2.drawLine(drawX + 10, drawY - 10, drawX - 10, drawY + 10);
//
//            g2.setFont(new Font("Arial", Font.PLAIN, 10));
//            g2.setColor(Color.RED);
//            g2.drawString("💀" + g.id, drawX - 8, drawY - 12);
//        }
//    }

    private void drawGodzilla(Graphics2D g2, GodzillaModel godzilla) {
        // Godzilla width = 2 tiles, height = full hotel height
        int gWidth  = tileSize * 2;
        int gHeight = hotelHeightPx;

        // Feet land exactly at the bottom of the lobby row
        int drawY = lobbyBottomPx - gHeight;
        int drawX = (int) godzilla.x + horizontalOffset - gWidth / 2;

        if (godzillaImg != null) {
            g2.drawImage(godzillaImg, drawX, drawY, gWidth, gHeight, null);
        } else {
            // Fallback: green silhouette
            g2.setColor(new Color(0, 120, 0, 200));
            g2.fillRect(drawX, drawY, gWidth, gHeight);
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            g2.drawString("🦖", drawX, drawY + gHeight / 2);
        }
    }
}