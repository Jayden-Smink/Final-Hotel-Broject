package view;

import model.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class GodzillaRenderer {
    private final int tileSize;
    private final int horizontalOffset;
    private BufferedImage godzillaImg;

    public GodzillaRenderer(int tileSize, int horizontalOffset) {
        this.tileSize = tileSize;
        this.horizontalOffset = horizontalOffset;

        // Try to load godzilla image
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

        // Draw destroyed areas
        drawDestroyedAreas(g2, data);

        // Draw fire areas
        drawFireAreas(g2, data);

        // Draw dead guests
        drawDeadGuests(g2, data);

        // Draw godzilla
        drawGodzilla(g2, godzilla);
    }

    private void drawDestroyedAreas(Graphics2D g2, SimulationData data) {
        for (Area a : data.areas) {
            if (!a.isDestroyed) continue;

            int x = (a.getPos()[0] * tileSize) + horizontalOffset;
            int y = a.getPos()[1] * tileSize;
            int w = a.getDim()[0] * tileSize;
            int h = a.getDim()[1] * tileSize;

            // Black overlay for destroyed areas
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            g2.setColor(new Color(20, 20, 20));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);

            // Draw X
            g2.setColor(new Color(150, 0, 0));
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(x, y, x + w, y + h);
            g2.drawLine(x + w, y, x, y + h);
        }
    }

    private void drawFireAreas(Graphics2D g2, SimulationData data) {
        for (Area a : data.areas) {
            if (!a.isOnFire) continue;

            int x = (a.getPos()[0] * tileSize) + horizontalOffset;
            int y = a.getPos()[1] * tileSize;
            int w = a.getDim()[0] * tileSize;
            int h = a.getDim()[1] * tileSize;

            // Animated fire overlay
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2.setColor(new Color(255, 80, 0));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);

            // Draw fire emoji text
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.setColor(Color.YELLOW);
            g2.drawString("🔥", x + w/2 - 10, y + h/2);
        }
    }

    private void drawDeadGuests(Graphics2D g2, SimulationData data) {
        for (Guest g : data.guests.values()) {
            if (!g.isDead) continue;

            int drawX = (int) g.x + horizontalOffset;
            int drawY = (int) g.y;

            // Draw red X for dead guest
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(drawX - 10, drawY - 10, drawX + 10, drawY + 10);
            g2.drawLine(drawX + 10, drawY - 10, drawX - 10, drawY + 10);

            // Label
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.setColor(Color.RED);
            g2.drawString("💀" + g.id, drawX - 8, drawY - 12);
        }
    }

    private void drawGodzilla(Graphics2D g2, GodzillaModel godzilla) {
        int drawX = (int) godzilla.x + horizontalOffset;
        int drawY = 0; // Godzilla spans full height

        if (godzillaImg != null) {
            g2.drawImage(godzillaImg, drawX, drawY, tileSize * 2, tileSize * 4, null);
        } else {
            // Fallback — green rectangle
            g2.setColor(new Color(0, 150, 0, 180));
            g2.fillRect(drawX, drawY, tileSize, tileSize * 4);
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("🦖", drawX, drawY + tileSize * 2);
        }
    }
}