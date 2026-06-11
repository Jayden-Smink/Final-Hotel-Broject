package view;

import controller.CleanerController;
import model.Cleaner;
import model.CleanerState;
import model.SimulationData;
import java.awt.*;
import java.util.List;

public class CleanerRenderer {
    private final CleanerController cleanerController;

    public CleanerRenderer(CleanerController cleanerController) {
        this.cleanerController = cleanerController;
    }

    public void drawCleaners(Graphics2D g2, int horizontalOffset) {
        if (cleanerController == null) return;

        List<Cleaner> cleaners = cleanerController.getActiveCleaners();
        for (Cleaner cleaner : cleaners) {
            if (cleaner.state == CleanerState.CLEANING) continue;

            int drawX = (int) cleaner.x + horizontalOffset;
            int drawY = (int) cleaner.y;

            g2.setColor(new Color(50, 205, 50));
            g2.fillOval(drawX - 10, drawY - 10, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("C" + cleaner.id, drawX - 6, drawY - 12);
        }
    }
}