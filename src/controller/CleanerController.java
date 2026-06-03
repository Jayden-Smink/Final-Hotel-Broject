package controller;

import model.Cleaner;
import model.CleanerState;
import model.StairModel;
import model.SimulationData;
import model.Area;
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
        // Alleen actie ondernemen als er een schoonmaker is en deze momenteel niks te doen heeft
        if (cleaner == null || cleaner.state != CleanerState.IDLE) return;

        // Zoek de eerste kamer die niet leeg is (aangezien de jar -1 als roomId stuurt)
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.currentOccupants.isEmpty())
                .findFirst()
                .ifPresent(room -> {
                    cleaner.assignedRoomId = room.id;
                    cleaner.state = CleanerState.WALKING_TO_ROOM;

                    // Bereken het exacte middelpunt van de kamer op het scherm
                    double tx = (room.getPos()[0] * tileSize) + horizontalOffset + (room.getDim()[0] * tileSize / 2.0);
                    double ty = (room.getPos()[1] * tileSize) + 25.0; // +25px zodat poppetje op de vloer staat
                    cleaner.setTarget(tx, ty);

                    if (logPanel != null) logPanel.addLog("🧹 Schoonmaker gaat naar kamer " + room.id + " op verdieping " + room.getPos()[1]);
                });
    }

    /**
     * De hoofd-update loop (tikt elke frame). Regelt beweging, timers en statuswissels.
     */
    public void update() {
        Cleaner cleaner = data.cleaner;
        if (cleaner == null) return;

        // Verplaats de schoonmaker een stapje richting zijn doel
        guestMover.moveCleaner(cleaner);

        // Logica wanneer de schoonmaker daadwerkelijk aan het poetsen is
        if (cleaner.state == CleanerState.CLEANING) {
            cleaner.cleaningTimer++;

            // Controleer of de schoonmaaktijd (in frames) is verstreken
            if (cleaner.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                cleaner.cleaningTimer = 0;
                cleaner.state = CleanerState.WALKING_BACK;

                // Zoek de Lobby op om de schoonmaker weer naar de thuisbasis te sturen
                data.areas.stream()
                        .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                        .findFirst()
                        .ifPresent(lobby -> {
                            int[] pos = lobby.getPos();
                            int[] dim = lobby.getDim();
                            // Bereken het middelpunt van de lobby
                            double tx = (pos[0] + dim[0] / 2.0) * 60.0;
                            double ty = (pos[1] * 60.0) + 25.0;
                            cleaner.setTarget(tx, ty);
                        });

                if (logPanel != null) logPanel.addLog("✅ Schoonmaker klaar! Gaat terug.");
            }
        }

        // Controleer of de schoonmaker zijn doel (binnen een marge van 5 pixels) heeft bereikt
        if (Math.abs(cleaner.x - cleaner.targetX) < 5 && Math.abs(cleaner.y - cleaner.targetY) < 5) {
            if (cleaner.state == CleanerState.WALKING_TO_ROOM) {
                // Aangekomen bij de vieze kamer -> start met poetsen
                cleaner.state = CleanerState.CLEANING;
                if (logPanel != null) logPanel.addLog("🧽 Schoonmaker is begonnen met schoonmaken.");
            } else if (cleaner.state == CleanerState.WALKING_BACK) {
                // Terug in de lobby -> reset naar stand-by modus
                cleaner.state = CleanerState.IDLE;
                cleaner.assignedRoomId = -1;
            }
        }
    }
}