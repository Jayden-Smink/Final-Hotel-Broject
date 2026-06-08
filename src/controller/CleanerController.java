package controller;

import model.Area;
import model.Cleaner;
import model.CleanerState;
import model.StairModel;
import model.SimulationData;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Beheert de logica en statusovergangen van MEERDERE schoonmakers in de hotelsimulatie.
 */
public class CleanerController {
    private final SimulationData data;
    private final LogPanel logPanel;

    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    // SRP Verbetering: Gekoppeld aan de specifieke CleanerMover
    private final CleanerMover cleanerMover;

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;

        // We geven direct de StairModel mee aan de CleanerMover constructor
        this.cleanerMover = new CleanerMover(data, new StairModel(data.areas));
    }

    /**
     * Vangt een noodgeval op en verdeelt de kamer over de schoonmaker met de minste taken.
     */
    public void handleCleaningEmergency(int roomId) {
        if (data.cleaners.isEmpty()) return;

        // Zoek een kamer om schoon te maken via de eigen areas lijst
        // (event roomId komt uit het scenario en matcht niet met interne Area IDs)
        int targetRoomId = -1;
        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (a.AreaType.equalsIgnoreCase("ROOM")) {
                targetRoomId = a.id;
                break;
            }
        }

        if (targetRoomId == -1) return;

        // Geef voorkeur aan een IDLE schoonmaker
        Cleaner bestWorker = null;
        for (Cleaner worker : data.cleaners.values()) {
            if (worker.state == CleanerState.IDLE) {
                bestWorker = worker;
                break;
            }
        }

        // Niemand IDLE? Pak degene met de kortste wachtrij
        if (bestWorker == null) {
            for (Cleaner worker : data.cleaners.values()) {
                if (bestWorker == null || worker.dirtyRooms.size() < bestWorker.dirtyRooms.size()) {
                    bestWorker = worker;
                }
            }
        }

        if (bestWorker == null) return;

        if (bestWorker.state == CleanerState.IDLE) {
            assignCleanerToRoom(bestWorker, targetRoomId);
        } else {
            bestWorker.dirtyRooms.add(targetRoomId);
        }

        if (logPanel != null) {
            logPanel.addLog("🚨 Noodgeval! Schoonmaker " + bestWorker.id + " gestuurd naar kamer " + targetRoomId);
        }
    }

    /**
     * De hoofd-update loop stuurt alle schoonmakers frame-by-frame aan.
     */
    public void update() {
        // We loopen direct onafhankelijk door alle actieve schoonmakers uit data.cleaners
        for (Cleaner worker : data.cleaners.values()) {

            // Roep de mover aan om de posities te berekenen voor deze specifieke werknemer
            cleanerMover.moveCleaner(worker);

            // --- Status logica per schoonmaker ---
            if (worker.state == CleanerState.CLEANING) {
                worker.cleaningTimer++;

                if (worker.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                    worker.cleaningTimer = 0;

                    // Pak de eerstvolgende taak uit de EIGEN wachtrij van deze schoonmaker
                    if (!worker.dirtyRooms.isEmpty()) {
                        int nextRoomId = worker.dirtyRooms.remove(0);
                        assignCleanerToRoom(worker, nextRoomId);
                        if (logPanel != null) logPanel.addLog("✅ Klaar! Schoonmaker " + worker.id + " gaat naar volgende kamer.");
                    } else {
                        worker.state = CleanerState.WALKING_BACK;
                        sendCleanerToLobby(worker);
                        if (logPanel != null) logPanel.addLog("✅ Schoonmaker " + worker.id + " klaar! Gaat terug naar de lobby.");
                    }
                }
            }

            // Check of deze specifieke schoonmaker zijn doel heeft bereikt
            if (Math.abs(worker.x - worker.targetX) < 5 && Math.abs(worker.y - worker.targetY) < 5) {
                if (worker.state == CleanerState.WALKING_TO_ROOM) {
                    worker.state = CleanerState.CLEANING;
                    if (logPanel != null) logPanel.addLog("🧽 Schoonmaker " + worker.id + " is begonnen met schoonmaken.");
                } else if (worker.state == CleanerState.WALKING_BACK) {
                    worker.state = CleanerState.IDLE;
                    worker.assignedRoomId = -1;
                }
            }

            // Als de schoonmaker niks te doen heeft, maar er staan wel kamers in zijn wachtrij: gaan!
            if (worker.state == CleanerState.IDLE && !worker.dirtyRooms.isEmpty()) {
                int nextRoomId = worker.dirtyRooms.remove(0);
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
                double tx = (a.getPos()[0] * tileSize) + horizontalOffset + ((a.getDim()[0] * tileSize) / 2.0);
                double ty = (a.getPos()[1] * tileSize) + 25.0;
                cleaner.setTarget(tx, ty);
                return;
            }
        }
    }

    /**
     * Geeft de lijst met actieve schoonmakers terug ten behoeve van de rendering/view laag.
     */
    public List<Cleaner> getActiveCleaners() {
        return new ArrayList<>(data.cleaners.values());
    }
}