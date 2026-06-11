package controller;

import model.*;
import view.LogPanel;
import java.util.List;

public class GodzillaController {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final IDestructionStrategy strategy;
    private final GodzillaModel godzilla;
    private int maxColumn;

    public GodzillaController(SimulationData data, LogPanel logPanel, IDestructionStrategy strategy) {
        this.data = data;
        this.logPanel = logPanel;
        this.strategy = strategy;

        // Find max column from layout
        this.maxColumn = data.areas.stream()
                .mapToInt(a -> a.getPos()[0] + a.getDim()[0])
                .max()
                .orElse(10);

        // Godzilla starts left of the hotel
        this.godzilla = new GodzillaModel(0, 0, 1.5);
    }

    public void activate() {
        godzilla.isActive = true;
        if (logPanel != null) logPanel.addLog("🦖 GODZILLA ATTACK! Run!");
    }

    public void update() {
        if (!godzilla.isActive) return;

        // Update fire timers for all burning areas
        for (Area a : data.areas) {
            if (a.isOnFire) strategy.update(a);
        }

        // Move godzilla
        godzilla.moveRight();
        godzilla.columnDestroyTimer++;

        // Every COLUMN_DESTROY_INTERVAL frames destroy next column
        if (godzilla.columnDestroyTimer >= GodzillaModel.COLUMN_DESTROY_INTERVAL) {
            destroyColumn(godzilla.currentColumn);
            godzilla.nextColumn();
        }

        // Handle guests in destroyed areas
        handleGuestsInDestroyedAreas();

        // Check if done
        if (godzilla.isDone(maxColumn)) {
            godzilla.isActive = false;
            if (logPanel != null) logPanel.addLog("🦖 Godzilla heeft het hotel verwoest!");
        }
    }

    private void destroyColumn(int column) {
        for (Area a : data.areas) {
            if (a.getPos()[0] == column && !a.isDestroyed) {
                strategy.destroy(a);

                // Special cases for elevator and stairs
                if (a.AreaType.equalsIgnoreCase("LIFTSCHACHT")) {
                    data.elevator = null; // elevator destroyed!
                    if (logPanel != null) logPanel.addLog("💥 Liftschacht vernietigd!");
                }
                if (a.AreaType.equalsIgnoreCase("TRAP")) {
                    if (logPanel != null) logPanel.addLog("💥 Trap vernietigd! Gasten vallen!");
                }
            }
        }
    }

    private void handleGuestsInDestroyedAreas() {
        for (Guest g : data.guests.values()) {
            if (g.isDead) continue;

            // Check if guest is in a destroyed area
            for (Area a : data.areas) {
                if (a.isDestroyed && isGuestInArea(g, a)) {
                    killGuest(g);
                    break;
                }
            }

            // If guest's target is destroyed → flee to lobby
            if (isTargetDestroyed(g)) {
                fleeToLobby(g);
            }

            // If guest is on destroyed stairs → fall to death
            if (isOnDestroyedStairs(g)) {
                killGuest(g);
            }
        }
    }

    private void killGuest(Guest g) {
        g.isDead = true;
        if (logPanel != null) logPanel.addLog("💀 Gast " + g.id + " heeft het niet overleefd.");
    }

    private void fleeToLobby(Guest g) {
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY") && !a.isDestroyed)
                .findFirst()
                .ifPresent(lobby -> {
                    double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize / 2.0;
                    g.setTarget(20.0, exitY);
                    if (logPanel != null) logPanel.addLog("🏃 Gast " + g.id + " vlucht!");
                });
    }

    private boolean isGuestInArea(Guest g, Area a) {
        int areaX = a.getPos()[0] * data.tileSize;
        int areaY = a.getPos()[1] * data.tileSize;
        int areaW = a.getDim()[0] * data.tileSize;
        int areaH = a.getDim()[1] * data.tileSize;

        return g.x >= areaX && g.x <= areaX + areaW &&
                g.y >= areaY && g.y <= areaY + areaH;
    }

    private boolean isTargetDestroyed(Guest g) {
        return data.areas.stream()
                .anyMatch(a -> a.isDestroyed && isGuestInArea(g, a) &&
                        Math.abs(g.targetX - (a.getPos()[0] * data.tileSize)) < data.tileSize);
    }

    private boolean isOnDestroyedStairs(Guest g) {
        return data.areas.stream()
                .anyMatch(a -> a.AreaType.equalsIgnoreCase("TRAP") &&
                        a.isDestroyed && isGuestInArea(g, a));
    }

    public GodzillaModel getGodzilla() {
        return godzilla;
    }
}