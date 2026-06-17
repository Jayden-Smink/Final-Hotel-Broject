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

        drawGodzilla(g2, godzilla);
    }

    private void drawGodzilla(Graphics2D g2, GodzillaModel godzilla) {
        // Godzilla width = 2 tiles, height = full hotel height
        int gWidth  = tileSize * 2;
        int gHeight = hotelHeightPx;

        // Basis posities
        int drawY = lobbyBottomPx - gHeight;
        int drawX = (int) godzilla.x + horizontalOffset - gWidth / 2;

        // ✅ GEFIXT: Voeg de dans-offsets toe aan de uiteindelijke render-coördinaten!
        drawX += godzilla.getDanceOffsetX();
        drawY += godzilla.getDanceOffsetY();

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