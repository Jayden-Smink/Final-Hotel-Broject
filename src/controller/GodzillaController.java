package controller;

import model.*;
import view.LogPanel;
import java.util.List;

public class GodzillaController {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final IDestructionStrategy strategy;
    private final model.FireDestruction fireUpdater = new model.FireDestruction(3);
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
        this.godzilla = new GodzillaModel(0, 0, 0.8);
    }

    private boolean hasStarted = false;
    private boolean finished = false;

    public void activate() {
        godzilla.isActive = true;
        hasStarted = true;
        finished = false;
        if (logPanel != null) logPanel.addLog("🦖 GODZILLA ATTACK! Run!");
    }

    /** True only after Godzilla has fully walked through the hotel. */
    public boolean isFinished() {
        return finished;
    }

    public void update() {
        if (!godzilla.isActive || finished) return;

        // Update fire timers for all burning areas (3-second burn before collapse)
        for (Area a : data.areas) {
            if (a.isOnFire) fireUpdater.update(a);
        }

        // Move godzilla
        godzilla.moveRight();

        // Destroy the column the instant Godzilla's foot reaches it
        if (godzilla.hasReachedNextColumn(data.tileSize, data.horizontalOffset)) {
            destroyColumn(godzilla.currentColumn);
            godzilla.nextColumn();
        }

        // Handle guests in destroyed areas
        handleGuestsInDestroyedAreas();

        // Check if done
        if (godzilla.isDone(maxColumn)) {
            godzilla.isActive = false;
            finished = true;
            if (logPanel != null) logPanel.addLog("🦖 Godzilla heeft het hotel verwoest!");
        }
    }

    private static final java.util.Random rng = new java.util.Random();

    private void destroyColumn(int column) {
        for (Area a : data.areas) {
            if (a.getPos()[0] == column && !a.isDestroyed) {
                // 30% chance of fire, 70% instant destruction
                if (rng.nextInt(100) < 30) {
                    new model.FireDestruction(3).destroy(a);
                    if (logPanel != null) logPanel.addLog("🔥 " + a.AreaType + " staat in brand!");
                } else {
                    new model.InstantDestruction().destroy(a);
                }

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

        // Also handle cleaners in destroyed areas
        for (model.Cleaner c : data.cleaners.values()) {
            if (c.isDead) continue;
            for (Area a : data.areas) {
                if (a.isDestroyed && isPersonInArea(c.x, c.y, a)) {
                    killCleaner(c);
                    break;
                }
            }
        }
    }

    private void killGuest(Guest g) {
        g.isDead = true;
        if (logPanel != null) logPanel.addLog("💀 Gast " + g.id + " heeft het niet overleefd.");
    }

    private void killCleaner(model.Cleaner c) {
        c.isDead = true;
        if (logPanel != null) logPanel.addLog("💀 Schoonmaker " + c.id + " heeft het niet overleefd.");
    }

    private boolean isPersonInArea(double px, double py, Area a) {
        int areaX = a.getPos()[0] * data.tileSize;
        int areaY = a.getPos()[1] * data.tileSize;
        int areaW = a.getDim()[0] * data.tileSize;
        int areaH = a.getDim()[1] * data.tileSize;
        return px >= areaX && px <= areaX + areaW && py >= areaY && py <= areaY + areaH;
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