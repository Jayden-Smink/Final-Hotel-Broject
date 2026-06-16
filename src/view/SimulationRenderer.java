package view;

import controller.CleanerController;
import model.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationRenderer {
    private final SimulationData data;
    private final AssetLoader assetLoader;
    private final AreaRenderer areaRenderer;
    private final ElevatorRenderer elevatorRenderer;
    private final CleanerRenderer cleanerRenderer;
    private GodzillaRenderer godzillaRenderer;
    private controller.GodzillaController godzillaController;

    public SimulationRenderer(SimulationData data, CleanerController cleanerController) {
        this.data = data;
        this.assetLoader = new AssetLoader();
        this.areaRenderer = new AreaRenderer(assetLoader, cleanerController);
        this.elevatorRenderer = new ElevatorRenderer(assetLoader);
        this.cleanerRenderer = new CleanerRenderer(cleanerController);
    }

    public void setGodzillaController(controller.GodzillaController gc, int tileSize, int horizontalOffset, int hotelGridHeight) {
        this.godzillaController = gc;
        this.godzillaRenderer = new GodzillaRenderer(tileSize, horizontalOffset, hotelGridHeight);
    }

    public void render(Graphics2D g2, SimulationData data) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, 0, 2000, 2000);

        areaRenderer.drawAreas(g2, data, false);
        elevatorRenderer.drawElevator(g2, data);
        areaRenderer.drawAreas(g2, data, true);
        drawGuests(g2, data);
        cleanerRenderer.drawCleaners(g2, data.horizontalOffset);

        // Draw Godzilla on top of everything else
        if (godzillaRenderer != null && godzillaController != null) {
            godzillaRenderer.render(g2, data, godzillaController.getGodzilla());
        }
    }

    private void drawGuests(Graphics2D g2, SimulationData data) {
        if (data.guests == null) return;

        List<Guest> guestSnapshot;
        synchronized (data.guests) {
            guestSnapshot = new ArrayList<>(data.guests.values());
        }

        for (Guest guest : guestSnapshot) {
            if (guest.isDead || guest.state != GuestState.IN_LIFT) {
                GuestRenderer.draw(g2, guest, data.horizontalOffset);
            }
        }
    }
}