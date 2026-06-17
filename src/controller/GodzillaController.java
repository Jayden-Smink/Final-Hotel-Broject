package controller;

import model.*;
import view.LogPanel;
import util.SoundManager; // Geïmporteerd voor de muziek-wissel
import java.util.List;

public class GodzillaController {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final IDestructionStrategy strategy;
    private final model.FireDestruction fireUpdater = new model.FireDestruction(3);
    private final GodzillaModel godzilla;
    private final SoundManager soundManager; // Opgeslagen om de muziek aan te sturen
    private int maxColumn;

    private boolean hasStarted = false;
    private boolean finished = false;

    public GodzillaController(SimulationData data, LogPanel logPanel, IDestructionStrategy strategy, SoundManager soundManager) {
        this.data = data;
        this.logPanel = logPanel;
        this.strategy = strategy;
        this.soundManager = soundManager; // Hier netjes opgevangen

        this.maxColumn = data.areas.stream()
                .mapToInt(a -> a.getPos()[0] + a.getDim()[0])
                .max()
                .orElse(10);

        this.godzilla = new GodzillaModel(0, 0, 0.8);
    }

    public void activate() {
        godzilla.isActive = true;
        hasStarted = true;
        finished = false;
        if (logPanel != null) logPanel.addLog("🦖 GODZILLA ATTACK! Run!");

        // ✅ Schakel de audio direct over naar Godzilla muziek!
        if (soundManager != null) {
            soundManager.stopMusic();
            soundManager.playBackgroundMusic("/music/godzilla.wav");
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void update() {
        if (!godzilla.isActive || finished) return;

        for (Area a : data.areas) {
            if (a.isOnFire) fireUpdater.update(a);
        }

        godzilla.moveRight();

        if (godzilla.hasReachedNextColumn(data.tileSize, data.horizontalOffset)) {
            destroyColumn(godzilla.currentColumn);
            godzilla.nextColumn();
        }

        handleGuestsInDestroyedAreas();

        if (godzilla.isDone(maxColumn)) {
            godzilla.isActive = false;
            finished = true;
            // ✅ Geüpdatet met een swingende logmelding!
            if (logPanel != null) logPanel.addLog("💃 Godzilla heeft al dansend het hotel verwoest! 🦖");
        }
    }

    private static final java.util.Random rng = new java.util.Random();

    private void destroyColumn(int column) {
        for (Area a : data.areas) {
            if (a.getPos()[0] == column && !a.isDestroyed) {
                if (rng.nextInt(100) < 30) {
                    new model.FireDestruction(3).destroy(a);
                    if (logPanel != null) logPanel.addLog("🔥 " + a.AreaType + " staat in brand!");
                } else {
                    new model.InstantDestruction().destroy(a);
                }

                if (a.AreaType.equalsIgnoreCase("LIFTSCHACHT")) {
                    data.elevator = null;
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

            for (Area a : data.areas) {
                if (a.isDestroyed && isGuestInArea(g, a)) {
                    killGuest(g);
                    break;
                }
            }
            if (g.isDead) continue;

            if (g.state == model.GuestState.IDLE) {
                for (Area a : data.areas) {
                    if (a.isDestroyed && (a.id == g.assignedRoomId || a.currentOccupants.contains(g.id))) {
                        g.x = (a.getPos()[0] * data.tileSize) + (a.getDim()[0] * data.tileSize) / 2.0;
                        g.y = (a.getPos()[1] * data.tileSize) + (a.getDim()[1] * data.tileSize) / 2.0;
                        killGuest(g);
                        break;
                    }
                }
            }

            if (!g.isDead && g.state == model.GuestState.IDLE) {
                for (Area a : data.areas) {
                    if (a.isDestroyed && isPersonInArea(g.x, g.y, a)) {
                        killGuest(g);
                        break;
                    }
                }
            }

            if (g.isDead) continue;

            if (isTargetDestroyed(g)) {
                fleeToLobby(g);
            }

            if (isOnDestroyedStairs(g)) {
                killGuest(g);
            }
        }

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
        g.state = model.GuestState.DEAD;
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