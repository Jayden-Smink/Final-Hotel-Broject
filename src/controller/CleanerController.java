package controller;

import model.Area;
import model.Cleaner;
import model.CleanerState;
import model.StairModel;
import model.SimulationData;
import view.LogPanel;
import java.util.List;

/**
 * Beheert de logica en statusovergangen van MEERDERE schoonmakers in de hotelsimulatie via een CleanerPool.
 */
public class CleanerController {
    private final SimulationData data;
    private final LogPanel logPanel;

    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    // SRP Verbetering: Nu gekoppeld aan de specifieke CleanerMover in plaats van GuestMover
    private final CleanerMover cleanerMover;
    private final CleanerPool cleanerPool; // SRP: De pool beheert de verzameling schoonmakers

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;

        // We geven direct de StairModel mee aan de CleanerMover constructor
        this.cleanerMover = new CleanerMover(data, new StairModel(data.areas));
        this.cleanerPool = new CleanerPool();
    }

    public void handleCleaningEmergency(int roomId) {
        Cleaner mainCleaner = data.cleaner;
        if (mainCleaner == null) return;

        // Zorg ervoor dat de pool gevuld en klaar is
        cleanerPool.setupWorkers(mainCleaner);

        // Zoek naar de eerste beschikbare (IDLE) schoonmaker in de pool voor het noodgeval
        for (Cleaner worker : cleanerPool.getWorkers()) {
            if (worker.state == CleanerState.IDLE) {
                for (int i = 0; i < data.areas.size(); i++) {
                    Area a = data.areas.get(i);
                    if (a.AreaType.equalsIgnoreCase("ROOM") && !a.currentOccupants.isEmpty()) {
                        assignCleanerToRoom(worker, a.id);
                        return; // Noodgeval succesvol toegewezen aan deze specifieke werknemer
                    }
                }
            }
        }
    }

    public void update() {
        Cleaner mainCleaner = data.cleaner;
        if (mainCleaner == null) return;

        // Zorgt dat de pool automatisch gevuld is met de 2 schoonmakers zodra de simulatie start
        cleanerPool.setupWorkers(mainCleaner);

        // GEFIXT: We loopen nu onafhankelijk door alle actieve schoonmakers in het hotel
        for (Cleaner worker : cleanerPool.getWorkers()) {

            // Roep de mover aan om de posities te berekenen voor deze specifieke werknemer
            cleanerMover.moveCleaner(worker);

            if (worker.state == CleanerState.CLEANING) {
                worker.cleaningTimer++;

                if (worker.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                    worker.cleaningTimer = 0;

                    // Pak de eerstvolgende taak uit de centrale inbox van de mainCleaner
                    if (!mainCleaner.dirtyRooms.isEmpty()) {
                        int nextRoomId = mainCleaner.dirtyRooms.remove(0);
                        assignCleanerToRoom(worker, nextRoomId);
                        if (logPanel != null) logPanel.addLog("✅ Klaar! Schoonmaker " + worker.id + " gaat naar volgende kamer.");
                    } else {
                        worker.state = CleanerState.WALKING_BACK;
                        sendCleanerToLobby(worker);
                        if (logPanel != null) logPanel.addLog("✅ Schoonmaker " + worker.id + " klaar! Gaat terug.");
                    }
                }
            }

            if (Math.abs(worker.x - worker.targetX) < 5 && Math.abs(worker.y - worker.targetY) < 5) {
                if (worker.state == CleanerState.WALKING_TO_ROOM) {
                    worker.state = CleanerState.CLEANING;
                    if (logPanel != null) logPanel.addLog("🧽 Schoonmaker " + worker.id + " is begonnen met schoonmaken.");
                } else if (worker.state == CleanerState.WALKING_BACK) {
                    worker.state = CleanerState.IDLE;
                    worker.assignedRoomId = -1;
                }
            }

            if (worker.state == CleanerState.IDLE && !mainCleaner.dirtyRooms.isEmpty()) {
                int nextRoomId = mainCleaner.dirtyRooms.remove(0);
                assignCleanerToRoom(worker, nextRoomId);
            }
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

                if (logPanel != null) logPanel.addLog("🧹 Schoonmaker " + cleaner.id + " gaat naar kamer " + a.id + " op verdieping " + a.getPos()[1]);
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

    /**
     * Geeft de lijst met actieve schoonmakers terug ten behoeve van de rendering/view laag.
     */
    public List<Cleaner> getActiveCleaners() {
        return cleanerPool.getWorkers();
    }
}