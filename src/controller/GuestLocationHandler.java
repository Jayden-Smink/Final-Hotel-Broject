package controller;

import model.*;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Verantwoordelijk voor het detecteren of een gast een bestemming heeft bereikt
 * en het aansturen van de juiste vervolgactie (SRP).
 */
public class GuestLocationHandler {

    private final SimulationData data;
    private final ReceptionistController receptionistController;
    private final GuestNavigator navigator;
    private final LogPanel logPanel;
    private final IRoomAssigner roomController;

    public GuestLocationHandler(SimulationData data, ReceptionistController receptionistController, GuestNavigator navigator, LogPanel logPanel) {
        this.data = data;
        this.receptionistController = receptionistController;
        this.navigator = navigator;
        this.logPanel = logPanel;
        this.roomController = new RoomController();
    }

    /**
     * Controleert per gast of hij een bestemming heeft bereikt en handelt dit af.
     */
    public void processLocationLogic() {
        List<Guest> guestList = new ArrayList<>(data.guests.values());

        for (Guest g : guestList) {

            // Uitcheckende gast heeft de uitgang bereikt
            if (g.isCheckingOut && g.state == GuestState.AT_DESTINATION
                    && Math.abs(g.x - 20.0) < 15) {

                handleGuestExit(g);
                continue;
            }

            if (g.state == GuestState.AT_DESTINATION) {

                // Situatie A: Gast staat bij de receptie
                if (isAtArea(g, "RECEPTION") && !g.isCheckingOut) {
                    handleReceptionArrival(g);
                }
                // Situatie B: Gast is aangekomen bij zijn eigen kamer
                else if (isAtAssignedRoom(g) && g.currentActivity.equals("WALKING_TO_ROOM")) {
                    g.state = GuestState.IDLE;
                    g.isInRoom = true;
                    g.currentActivity = "ROOM";
                    g.activityTimer = 0;
                }
                // Situatie C: Gast is aangekomen bij een faciliteit
                else if (g.currentActivity.equals("WALKING_TO_FACILITY")) {
                    g.state = GuestState.IDLE;
                    g.isInRoom = false;
                    g.currentActivity = "USING_FACILITY";
                    g.activityTimer = 0;
                }
            }
        }
    }

    /**
     * Verwijdert de gast uit het hotel en wijst zijn kamer toe aan de minst drukke schoonmaker.
     */
    private void handleGuestExit(Guest g) {
        int roomId = g.assignedRoomId;

        roomController.maakGastVrij(data, g.id);
        data.guests.remove(g.id);

        if (roomId != -1 && !data.cleaners.isEmpty()) {
            Cleaner bestCleaner = null;
            for (Cleaner c : data.cleaners.values()) {
                if (bestCleaner == null || c.dirtyRooms.size() < bestCleaner.dirtyRooms.size()) {
                    bestCleaner = c;
                }
            }
            if (bestCleaner != null) {
                bestCleaner.dirtyRooms.add(roomId);
                if (logPanel != null) {
                    logPanel.addLog("🛏️ Kamer " + roomId + " toegewezen aan schoonmaker " + bestCleaner.id);
                }
            }
        }

        if (logPanel != null) logPanel.addLog("🚶 Gast " + g.id + " heeft het hotel verlaten.");
    }

    /**
     * Handelt de aankomst bij de receptie af: stuur naar kamer of, als het hotel vol is, naar de uitgang.
     */
    private void handleReceptionArrival(Guest g) {
        receptionistController.sendToRoom(g);

        if (g.assignedRoomId == -1) {
            g.isCheckingOut = true;
            navigator.sendGuestToExit(g);
            if (logPanel != null) {
                logPanel.addLog("❌ Receptie: Geen kamer vrij! Gast " + g.id + " moet het hotel verlaten.");
            }
        } else {
            g.isInRoom = false;
            g.currentActivity = "WALKING_TO_ROOM";
        }
    }

    /**
     * Wiskundige bounding-box check of een gast zich bij een bepaald type ruimte bevindt.
     */
    private boolean isAtArea(Guest g, String type) {
        return data.areas.stream().anyMatch(a -> {
            if (!a.AreaType.equalsIgnoreCase(type)) return false;
            int areaX = a.getPos()[0] * data.tileSize;
            int areaY = a.getPos()[1] * data.tileSize;
            return g.x >= (areaX - 15) &&
                    g.x <= (areaX + (a.getDim()[0] * data.tileSize) + 15) &&
                    g.y >= areaY &&
                    g.y <= (areaY + (a.getDim()[1] * data.tileSize));
        });
    }

    /**
     * Controleert of de gast dicht genoeg bij zijn kamerdoel staat (binnen 15 pixels marge).
     */
    private boolean isAtAssignedRoom(Guest g) {
        if (g.assignedRoomId == -1) return false;
        return Math.abs(g.x - g.targetX) < 15 &&
                Math.abs(g.y - g.targetY) < 15;
    }
}