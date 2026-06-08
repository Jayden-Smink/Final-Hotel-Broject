package controller;

import model.*;
import view.LogPanel;
import java.util.*;

/**
 * Beheert de levenscyclus, het gedrag en de AI-activiteiten van hotelgasten.
 * Regelt het inchecken, uitchecken en de rotatie tussen kamers en faciliteiten.
 */
public class GuestActivityController {

    private final SimulationData data;
    private final ReceptionistController receptionistController;
    private final LogPanel logPanel;
    private final IRoomAssigner roomController;
    private final Random random = new Random();

    public GuestActivityController(SimulationData data, ReceptionistController receptionistController, LogPanel logPanel) {
        this.data = data;
        this.receptionistController = receptionistController;
        this.logPanel = logPanel;
        this.roomController = new RoomController(); // Verantwoordelijk voor kamerbeheer (vrijmaken/toewijzen)
    }

    /**
     * Hoofd-update loop voor de gastenactiviteiten. Wordt elke frame aangeroepen.
     */
    public void updateActivities() {
        processLocationLogic();         // 1. Controleer OF en WAAR gasten zijn aangekomen
        handleDynamicGuestActivities(); // 2. Beheer de timers voor nieuwe activiteiten van stilstaande gasten
    }

    /**
     * Controleert of een gast op een specifieke bestemming is aangekomen en switcht hun status.
     */
    private void processLocationLogic() {
        // Kopie van de lijst om ConcurrentModificationExceptions tijdens het verwijderen (checkout) te voorkomen
        List<Guest> guestList = new ArrayList<>(data.guests.values());

        for (Guest g : guestList) {

            // Snel-check: Is de gast aan het uitchecken en staat hij bij de hoteluitgang (X-as rond de 20)?
            if (g.isCheckingOut && g.state == GuestState.AT_DESTINATION
                    && Math.abs(g.x - 20.0) < 15) {

                int roomId = g.assignedRoomId; // ← save before freeing

                roomController.maakGastVrij(data, g.id);
                data.guests.remove(g.id);

                // GEFIXT: Werkverdeling over de nieuwe map met meerdere schoonmakers
                if (roomId != -1 && !data.cleaners.isEmpty()) {
                    Cleaner bestCleaner = null;

                    // Zoek de schoonmaker met de minste vieze kamers in zijn wachtrij
                    for (Cleaner c : data.cleaners.values()) {
                        if (bestCleaner == null || c.dirtyRooms.size() < bestCleaner.dirtyRooms.size()) {
                            bestCleaner = c;
                        }
                    }

                    // Wijs de kamer toe aan de schoonmaker die het minst druk is
                    if (bestCleaner != null) {
                        bestCleaner.dirtyRooms.add(roomId);
                        if (logPanel != null) {
                            logPanel.addLog("🛏️ Kamer " + roomId + " toegewezen aan schoonmaker " + bestCleaner.id);
                        }
                    }
                }

                if (logPanel != null) logPanel.addLog("🚶 Gast " + g.id + " heeft het hotel verlaten.");
                continue;
            }

            // Logica voor gasten die hun tussentijdse bestemming hebben bereikt
            if (g.state == GuestState.AT_DESTINATION) {

                // Situatie A: Gast staat bij de receptie (klaar om in te checken)
                if (isAtArea(g, "RECEPTION") && !g.isCheckingOut) {

                    receptionistController.sendToRoom(g); // Vraag de receptionist om een kamer te geven

                    // Als het hotel vol is (assignedRoomId blijft -1), stuur de gast direct naar de uitgang
                    if (g.assignedRoomId == -1) {
                        g.isCheckingOut = true;
                        sendGuestToExit(g);

                        if (logPanel != null) {
                            logPanel.addLog("❌ Receptie: Geen kamer vrij! Gast " + g.id + " moet het hotel verlaten.");
                        }
                    } else {
                        g.isInRoom = false;
                        g.currentActivity = "WALKING_TO_ROOM";
                    }
                }

                // Situatie B: Gast is aangekomen bij zijn/haar toegewezen hotelkamer
                else if (isAtAssignedRoom(g) && g.currentActivity.equals("WALKING_TO_ROOM")) {
                    g.state = GuestState.IDLE; // Gast staat stil
                    g.isInRoom = true;
                    g.currentActivity = "ROOM";
                    g.activityTimer = 0;       // Reset de timer voor de volgende activiteit
                }

                // Situatie C: Gast is aangekomen bij een faciliteit (Bioscoop, Restaurant, etc.)
                else if (g.currentActivity.equals("WALKING_TO_FACILITY")) {
                    g.state = GuestState.IDLE;
                    g.isInRoom = false;
                    g.currentActivity = "USING_FACILITY";
                    g.activityTimer = 0;       // Reset de timer voor de duur van het faciliteitbezoek
                }
            }
        }
    }

    /**
     * Regelt de timers van stilstaande (IDLE) gasten en stuurt ze na verloop van tijd ergens anders heen.
     */
    private void handleDynamicGuestActivities() {
        for (Guest g : data.guests.values()) {

            if (g.state == GuestState.IDLE) {

                // Als een gast de status heeft om uit te checken, stuur hem direct naar de uitgang
                if (g.isCheckingOut) {
                    sendGuestToExit(g);
                    continue;
                }

                g.activityTimer++; // Hoog de activiteitstimer op (tikt elke frame)

                // Na 300 frames (~5 seconden op normale snelheid) wisselt de gast van activiteit
                int duration = getFacilityDuration(g);
                if (g.activityTimer >= duration) {

                    if (g.currentActivity.equals("ROOM")) {
                        // Als hij in zijn kamer zat, ga naar een willekeurige faciliteit
                        sendGuestToRandomFacility(g);
                    } else if (g.currentActivity.equals("USING_FACILITY")) {
                        // Als hij in een faciliteit zat, loop weer terug naar de kamer
                        returnGuestToRoom(g);
                    }

                    g.activityTimer = 0; // Reset de timer
                }
            }
        }
    }

    /**
     * Stuurt een gast naar de lobby (de exit-coördinaten van het hotel).
     */
    private void sendGuestToExit(Guest g) {
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                .findFirst()
                .ifPresent(lobby -> {
                    double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;
                    g.setTarget(20.0, exitY); // X = 20 is de uiterste linkerkant van de lobby (de deur)
                });
    }

    /**
     * Kiest een willekeurige faciliteit (Restaurant, Cinema, Fitness) en stuurt de gast daarheen.
     */
    private void sendGuestToRandomFacility(Guest g) {
        List<String> types = Arrays.asList("RESTAURANT", "CINEMA", "FITNESS");

        // Filter alle hotelruimtes op de bovenstaande types
        var facilities = data.areas.stream()
                .filter(a -> types.contains(a.AreaType.toUpperCase()))
                .toList();

        if (!facilities.isEmpty()) {
            // Pak een willekeurige faciliteit uit de gefilterde lijst
            var area = facilities.get(random.nextInt(facilities.size()));

            // Bereken het exacte middelpunt van deze faciliteit
            double targetX = (area.getPos()[0] * data.tileSize) + ((area.getDim()[0] * data.tileSize) / 2.0);
            double targetY = (area.getPos()[1] * data.tileSize) + data.tileSize/2.0;

            g.currentActivity = "WALKING_TO_FACILITY";
            g.currentFacility = area.AreaType.toUpperCase(); // ADD THIS
            g.setTarget(targetX, targetY); // Geef het wandeldoel mee aan de gast

            if (logPanel != null) {
                logPanel.addLog("🏃 Activiteit: Gast " + g.id + " loopt naar het " + area.AreaType.toLowerCase() + ".");
            }
        }
    }

    /**
     * Stuurt de gast weer terug naar zijn/haar eigen gereserveerde hotelkamer.
     */
    private void returnGuestToRoom(Guest g) {
        if (g.assignedRoomId == -1) return;

        data.areas.stream()
                .filter(a -> a.id == g.assignedRoomId)
                .findFirst()
                .ifPresent(room -> {
                    // Bereken het middelpunt van de eigen hotelkamer
                    double targetX = (room.getPos()[0] * data.tileSize) + ((room.getDim()[0] * data.tileSize) / 2.0);
                    double targetY = (room.getPos()[1] * data.tileSize) + data.tileSize/2.0;

                    g.currentActivity = "WALKING_TO_ROOM";
                    g.setTarget(targetX, targetY);

                    if (logPanel != null) {
                        logPanel.addLog("🛏️ Terugkeer: Gast " + g.id + " loopt terug naar hotelkamer " + g.assignedRoomId + ".");
                    }
                });
    }

    /**
     * Wiskundige bounding-box check om te kijken of een gast zich binnen een bepaald type ruimte bevindt.
     */
    private boolean isAtArea(Guest g, String type) {
        return data.areas.stream().anyMatch(a -> {
            if (!a.AreaType.equalsIgnoreCase(type)) return false;

            int areaX = a.getPos()[0] * data.tileSize;
            int areaY = a.getPos()[1] * data.tileSize;

            // Controleer of de X- en Y-coördinaten van de gast binnen de grenzen van de Area vallen (+- 15 pixels marge)
            return g.x >= (areaX - 15) &&
                    g.x <= (areaX + (a.getDim()[0] * data.tileSize) + 15) &&
                    g.y >= areaY &&
                    g.y <= (areaY + (a.getDim()[1] * data.tileSize));
        });
    }

    /**
     * Controleert of de gast dicht genoeg bij zijn berekende kamerdoel staat (binnen een marge van 15 pixels).
     */
    private boolean isAtAssignedRoom(Guest g) {
        if (g.assignedRoomId == -1) return false;

        return Math.abs(g.x - g.targetX) < 15 &&
                Math.abs(g.y - g.targetY) < 15;
    }

    private int getFacilityDuration(Guest g) {
        if (g.currentActivity.equals("USING_FACILITY")) {
            switch (g.currentFacility) {
                case "CINEMA":
                    return data.facilitySettings.getCinemaDurationFrames();
                case "RESTAURANT":
                    return data.facilitySettings.getRestaurantDurationFrames();
                case "FITNESS":
                    return data.facilitySettings.getFitnessDurationFrames();
                default:
                    return 300;
            }
        }
        return 300;
    }
}