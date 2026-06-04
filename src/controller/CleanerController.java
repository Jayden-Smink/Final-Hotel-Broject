package controller;

import model.Area;
import model.Cleaner;
import model.CleanerState;
import model.StairModel;
import model.SimulationData;
import view.LogPanel;

/**
 * Beheert de logica en statusovergangen van de schoonmaker in de hotelsimulatie.
 */
public class CleanerController {
    private final SimulationData data;
    private final LogPanel logPanel;

    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    // SRP Verbetering: Nu gekoppeld aan de specifieke CleanerMover in plaats van GuestMover
    private final CleanerMover cleanerMover;

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;

        // GEFIXT: We geven nu direct de StairModel mee aan de CleanerMover constructor
        this.cleanerMover = new CleanerMover(data, new StairModel(data.areas));
    }

    public void handleCleaningEmergency(int roomId) {
        Cleaner cleaner = data.cleaner;
        if (cleaner == null || cleaner.state != CleanerState.IDLE) return;

        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (a.AreaType.equalsIgnoreCase("ROOM") && !a.currentOccupants.isEmpty()) {
                assignCleanerToRoom(cleaner, a.id);
                return;
            }
        }
    }

    public void update() {
        Cleaner cleaner = data.cleaner;
        if (cleaner == null) return;

        // Roep de nieuwe mover aan om de posities te berekenen
        cleanerMover.moveCleaner(cleaner);

        if (cleaner.state == CleanerState.CLEANING) {
            cleaner.cleaningTimer++;

            if (cleaner.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                cleaner.cleaningTimer = 0;

                if (!cleaner.dirtyRooms.isEmpty()) {
                    int nextRoomId = cleaner.dirtyRooms.remove(0);
                    assignCleanerToRoom(cleaner, nextRoomId);
                    if (logPanel != null) logPanel.addLog("✅ Klaar! Schoonmaker gaat naar volgende kamer.");
                } else {
                    cleaner.state = CleanerState.WALKING_BACK;
                    sendCleanerToLobby(cleaner);
                    if (logPanel != null) logPanel.addLog("✅ Schoonmaker klaar! Gaat terug.");
                }
            }
        }

        if (Math.abs(cleaner.x - cleaner.targetX) < 5 && Math.abs(cleaner.y - cleaner.targetY) < 5) {
            if (cleaner.state == CleanerState.WALKING_TO_ROOM) {
                cleaner.state = CleanerState.CLEANING;
                if (logPanel != null) logPanel.addLog("🧽 Schoonmaker is begonnen met schoonmaken.");
            } else if (cleaner.state == CleanerState.WALKING_BACK) {
                cleaner.state = CleanerState.IDLE;
                cleaner.assignedRoomId = -1;
            }
        }

        if (cleaner.state == CleanerState.IDLE && !cleaner.dirtyRooms.isEmpty()) {
            int nextRoomId = cleaner.dirtyRooms.remove(0);
            assignCleanerToRoom(cleaner, nextRoomId);
        }
    }

    private void assignCleanerToRoom(Cleaner cleaner, int roomId) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (a.id == roomId && a.AreaType.equalsIgnoreCase("ROOM")) {
                cleaner.assignedRoomId = a.id;
                cleaner.state = CleanerState.WALKING_TO_ROOM;

                double tx = (a.getPos()[0] * tileSize) + horizontalOffset + (a.getDim()[0] * tileSize / 2.0);
                double ty = (a.getPos()[1] * tileSize) + 25.0;
                cleaner.setTarget(tx, ty);

                if (logPanel != null) logPanel.addLog("🧹 Schoonmaker gaat naar kamer " + a.id + " op verdieping " + a.getPos()[1]);
                return;
            }
        }
    }

    private void sendCleanerToLobby(Cleaner cleaner) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (a.AreaType.equalsIgnoreCase("LOBBY")) {
                int[] pos = a.getPos();
                int[] dim = a.getDim();
                double tx = (pos[0] + dim[0] / 2.0) * 60.0;
                double ty = (pos[1] * 60.0) + 25.0;
                cleaner.setTarget(tx, ty);
                return;
            }
        }
    }
}