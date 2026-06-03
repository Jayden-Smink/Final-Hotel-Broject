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

    // Grid-instellingen voor de grafische weergave (60 pixels per vakje)
    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    // Verantwoordelijk voor de daadwerkelijke beweging over assen en trappen
    private final GuestMover guestMover;

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
        this.guestMover = new GuestMover(data, new StairModel(data.areas));
    }

    /**
     * Reageert op een schoonmaak-noodgeval door de schoonmaker naar een bezette kamer te sturen.
     */
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

    /**
     * De hoofd-update loop (tikt elke frame). Regelt beweging, timers en statuswissels.
     */
    public void update() {
        Cleaner cleaner = data.cleaner;
        if (cleaner == null) return;

        guestMover.moveCleaner(cleaner);

        if (cleaner.state == CleanerState.CLEANING) {
            cleaner.cleaningTimer++;

            if (cleaner.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                cleaner.cleaningTimer = 0;

                if (!cleaner.dirtyRooms.isEmpty()) {
                    // Ga direct naar de volgende vieze kamer, sla lobby over
                    int nextRoomId = cleaner.dirtyRooms.remove(0);
                    assignCleanerToRoom(cleaner, nextRoomId);
                    if (logPanel != null) logPanel.addLog("✅ Klaar! Schoonmaker gaat naar volgende kamer.");
                } else {
                    // Geen kamers meer, ga terug naar lobby
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

        // Nodig voor als de schoonmaker al IDLE is wanneer een nieuwe kamer binnenkomt
        if (cleaner.state == CleanerState.IDLE && !cleaner.dirtyRooms.isEmpty()) {
            int nextRoomId = cleaner.dirtyRooms.remove(0);
            assignCleanerToRoom(cleaner, nextRoomId);
        }
    }

    /**
     * Stuurt de schoonmaker naar een specifieke kamer om deze schoon te maken.
     */
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

    /**
     * Stuurt de schoonmaker terug naar de lobby.
     */
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