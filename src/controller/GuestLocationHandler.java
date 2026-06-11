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

        for (int i = 0; i < guestList.size(); i++) {
            Guest guest = guestList.get(i);

            // Dode gast: kamer vrijmaken en verwijderen
            if (guest.isDead) {
                freeRoomAndAssignCleaner(guest);
                continue;
            }

            // Uitcheckende gast heeft de uitgang bereikt
            if (guest.isCheckingOut && guest.state == GuestState.AT_DESTINATION
                    && Math.abs(guest.x - 20.0) < 15) {
                handleGuestExit(guest);
                continue;
            }

            if (guest.state == GuestState.AT_DESTINATION) {

                // Situatie A: Gast staat bij de receptie
                if (isAtArea(guest, "RECEPTION") && !guest.isCheckingOut) {
                    handleReceptionArrival(guest);
                }
                // Situatie B: Gast is aangekomen bij zijn eigen kamer
                else if (isAtAssignedRoom(guest) && guest.currentActivity.equals("WALKING_TO_ROOM")) {
                    guest.state = GuestState.IDLE;
                    guest.isInRoom = true;
                    guest.currentActivity = "ROOM";
                    guest.activityTimer = 0;
                }
                // Situatie C: Gast is aangekomen bij een faciliteit
                else if (guest.currentActivity.equals("WALKING_TO_FACILITY")) {
                    guest.state = GuestState.IDLE;
                    guest.isInRoom = false;
                    guest.currentActivity = "USING_FACILITY";
                    guest.activityTimer = 0;
                }
            }
        }
    }

    /**
     * Verwijdert de gast uit het hotel en wijst zijn kamer toe aan de minst drukke schoonmaker.
     */
    private void handleGuestExit(Guest guest) {
        freeRoomAndAssignCleaner(guest);
        if (logPanel != null) logPanel.addLog("🚶 Gast " + guest.id + " heeft het hotel verlaten.");
    }

    /**
     * Gedeelde logica: maakt de kamer vrij, verwijdert de gast uit de simulatie,
     * en wijst de kamer toe aan de minst bezette schoonmaker.
     */
    private void freeRoomAndAssignCleaner(Guest guest) {
        int roomId = guest.assignedRoomId;

        roomController.maakGastVrij(data, guest.id);
        data.guests.remove(guest.id);

        if (roomId != -1 && !data.cleaners.isEmpty()) {
            Cleaner bestCleaner = null;
            List<Cleaner> cleanerList = new ArrayList<>(data.cleaners.values());
            for (int j = 0; j < cleanerList.size(); j++) {
                Cleaner cleaner = cleanerList.get(j);
                if (bestCleaner == null || cleaner.dirtyRooms.size() < bestCleaner.dirtyRooms.size()) {
                    bestCleaner = cleaner;
                }
            }
            if (bestCleaner != null) {
                bestCleaner.dirtyRooms.add(roomId);
                if (logPanel != null) {
                    logPanel.addLog("🛏️ Kamer " + roomId + " toegewezen aan schoonmaker " + bestCleaner.id);
                }
            }
        }
    }

    /**
     * Handelt de aankomst bij de receptie af: stuur naar kamer of, als het hotel vol is, naar de uitgang.
     */
    private void handleReceptionArrival(Guest guest) {
        receptionistController.sendToRoom(guest);

        if (guest.assignedRoomId == -1) {
            guest.isCheckingOut = true;
            navigator.sendGuestToExit(guest);
            if (logPanel != null) {
                logPanel.addLog("❌ Receptie: Geen kamer vrij! Gast " + guest.id + " moet het hotel verlaten.");
            }
        } else {
            guest.isInRoom = false;
            guest.currentActivity = "WALKING_TO_ROOM";
        }
    }

    /**
     * Wiskundige bounding-box check of een gast zich bij een bepaald type ruimte bevindt.
     */
    private boolean isAtArea(Guest guest, String type) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            if (!area.AreaType.equalsIgnoreCase(type)) continue;
            int areaX = area.getPos()[0] * data.tileSize;
            int areaY = area.getPos()[1] * data.tileSize;
            if (guest.x >= (areaX - 15) &&
                    guest.x <= (areaX + (area.getDim()[0] * data.tileSize) + 15) &&
                    guest.y >= areaY &&
                    guest.y <= (areaY + (area.getDim()[1] * data.tileSize))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controleert of de gast dicht genoeg bij zijn kamerdoel staat (binnen 15 pixels marge).
     */
    private boolean isAtAssignedRoom(Guest guest) {
        if (guest.assignedRoomId == -1) return false;
        return Math.abs(guest.x - guest.targetX) < 15 &&
                Math.abs(guest.y - guest.targetY) < 15;
    }
}