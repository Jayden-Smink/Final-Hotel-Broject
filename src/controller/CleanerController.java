package controller;

import model.Cleaner;
import model.CleanerState;
import model.StairModel;
import model.SimulationData;
import model.Area;
import view.LogPanel;


public class CleanerController {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    private final GuestMover guestMover;

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
        this.guestMover = new GuestMover(data, new StairModel(data.areas));
    }

    public void handleCleaningEmergency(int roomId) {
        Cleaner cleaner = data.cleaner;
        if (cleaner == null || cleaner.state != CleanerState.IDLE) return;

        // Since jar sends -1, find the first occupied room
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.currentOccupants.isEmpty())
                .findFirst()
                .ifPresent(room -> {
                    cleaner.assignedRoomId = room.id;
                    cleaner.state = CleanerState.WALKING_TO_ROOM;

                    double tx = (room.getPos()[0] * tileSize) + horizontalOffset + (room.getDim()[0] * tileSize / 2.0);
                    double ty = (room.getPos()[1] * tileSize) + 25.0;
                    cleaner.setTarget(tx, ty);

                    if (logPanel != null) logPanel.addLog("🧹 Schoonmaker gaat naar kamer " + room.id + " op verdieping " + room.getPos()[1]);
                });
    }

    public void update() {
        Cleaner cleaner = data.cleaner;

        if (cleaner == null) return;
        guestMover.moveCleaner(cleaner);

        if (cleaner.state == CleanerState.CLEANING) {
            cleaner.cleaningTimer++;

            if (cleaner.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                // Done cleaning
                cleaner.cleaningTimer = 0;
                cleaner.state = CleanerState.WALKING_BACK;

                // Replace the "send back to home room" part with:
                data.areas.stream()
                        .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                        .findFirst()
                        .ifPresent(lobby -> {
                            int[] pos = lobby.getPos();
                            int[] dim = lobby.getDim();
                            double tx = (pos[0] + dim[0] / 2.0) * 60.0;
                            double ty = (pos[1] * 60.0) + 25.0;
                            cleaner.setTarget(tx, ty);
                        });

                if (logPanel != null) logPanel.addLog("✅ Schoonmaker klaar! Gaat terug.");
            }
        }

        // Check if cleaner arrived at destination
        if (Math.abs(cleaner.x - cleaner.targetX) < 5 && Math.abs(cleaner.y - cleaner.targetY) < 5) {
            if (cleaner.state == CleanerState.WALKING_TO_ROOM) {
                cleaner.state = CleanerState.CLEANING;
                if (logPanel != null) logPanel.addLog("🧽 Schoonmaker is begonnen met schoonmaken.");
            } else if (cleaner.state == CleanerState.WALKING_BACK) {
                cleaner.state = CleanerState.IDLE;
                cleaner.assignedRoomId = -1;
            }
        }
    }
}