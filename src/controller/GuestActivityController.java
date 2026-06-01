package controller;

import model.*;
import view.LogPanel;
import java.util.*;

public class GuestActivityController {

    private final SimulationData data;
    private final ReceptionistController receptionistController;
    private final LogPanel logPanel;
    private final Random random = new Random();

    public GuestActivityController(SimulationData data, ReceptionistController receptionistController, LogPanel logPanel) {
        this.data = data;
        this.receptionistController = receptionistController;
        this.logPanel = logPanel;
    }

    public void updateActivities() {
        processLocationLogic();
        handleDynamicGuestActivities();
    }

    private void processLocationLogic() {

        List<Guest> guestList = new ArrayList<>(data.guests.values());

        for (Guest g : guestList) {

            // Checkout check — runs regardless of other state
            if (g.isCheckingOut && g.state == GuestState.AT_DESTINATION
                    && Math.abs(g.x - 20.0) < 15) {

                RoomController.maakGastVrij(data, g.id);
                data.guests.remove(g.id);

                if (logPanel != null) {
                    logPanel.addLog("🚶 Gast " + g.id + " heeft het hotel verlaten.");
                }

                continue;
            }

            if (g.state == GuestState.AT_DESTINATION) {

                // Gast staat bij de receptie
                if (isAtArea(g, "RECEPTION") && !g.isCheckingOut) {

                    receptionistController.sendToRoom(g);

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

                // Gast is bij zijn kamer aangekomen
                else if (isAtAssignedRoom(g) && g.currentActivity.equals("WALKING_TO_ROOM")) {
                    g.state = GuestState.IDLE;
                    g.isInRoom = true;
                    g.currentActivity = "ROOM";
                    g.activityTimer = 0;
                }

                // Gast is bij een faciliteit aangekomen
                else if (g.currentActivity.equals("WALKING_TO_FACILITY")) {
                    g.state = GuestState.IDLE;
                    g.isInRoom = false;
                    g.currentActivity = "USING_FACILITY";
                    g.activityTimer = 0;
                }
            }
        }
    }

    private void handleDynamicGuestActivities() {

        for (Guest g : data.guests.values()) {

            if (g.state == GuestState.IDLE) {

                // Checkout heeft altijd prioriteit
                if (g.isCheckingOut) {
                    sendGuestToExit(g);
                    continue;
                }

                g.activityTimer++;

                if (g.activityTimer >= 300) {

                    if (g.currentActivity.equals("ROOM")) {
                        sendGuestToRandomFacility(g);
                    } else if (g.currentActivity.equals("USING_FACILITY")) {
                        returnGuestToRoom(g);
                    }

                    g.activityTimer = 0;
                }
            }
        }
    }

    private void sendGuestToExit(Guest g) {
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                .findFirst()
                .ifPresent(lobby -> {
                    double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;
                    g.setTarget(20.0, exitY);
                });
    }

    private void sendGuestToRandomFacility(Guest g) {

        List<String> types = Arrays.asList("RESTAURANT", "CINEMA", "FITNESS");

        var facilities = data.areas.stream()
                .filter(a -> types.contains(a.AreaType.toUpperCase()))
                .toList();

        if (!facilities.isEmpty()) {

            var area = facilities.get(random.nextInt(facilities.size()));

            // Center horizontally — no + horizontalOffset, renderer adds it
            double targetX = (area.getPos()[0] * data.tileSize)
                    + ((area.getDim()[0] * data.tileSize) / 2.0);
            double targetY = (area.getPos()[1] * data.tileSize) + data.tileSize/2.0;

            g.currentActivity = "WALKING_TO_FACILITY";
            g.setTarget(targetX, targetY);

            if (logPanel != null) {
                logPanel.addLog("🏃 Activiteit: Gast " + g.id
                        + " loopt naar het " + area.AreaType.toLowerCase() + ".");
            }
        }
    }

    private void returnGuestToRoom(Guest g) {

        if (g.assignedRoomId == -1) return;

        data.areas.stream()
                .filter(a -> a.id == g.assignedRoomId)
                .findFirst()
                .ifPresent(room -> {

                    // Center horizontally — no + horizontalOffset
                    double targetX = (room.getPos()[0] * data.tileSize)
                            + ((room.getDim()[0] * data.tileSize) / 2.0);
                    double targetY = (room.getPos()[1] * data.tileSize) + data.tileSize/2.0;

                    g.currentActivity = "WALKING_TO_ROOM";
                    g.setTarget(targetX, targetY);

                    if (logPanel != null) {
                        logPanel.addLog("🛏️ Terugkeer: Gast " + g.id
                                + " loopt terug naar hotelkamer " + g.assignedRoomId + ".");
                    }
                });
    }

    private boolean isAtArea(Guest g, String type) {

        return data.areas.stream().anyMatch(a -> {

            if (!a.AreaType.equalsIgnoreCase(type)) return false;

            // No + horizontalOffset — g.x is in logical coordinates
            int areaX = a.getPos()[0] * data.tileSize;
            int areaY = a.getPos()[1] * data.tileSize;

            return g.x >= (areaX - 15) &&
                    g.x <= (areaX + (a.getDim()[0] * data.tileSize) + 15) &&
                    g.y >= areaY &&
                    g.y <= (areaY + (a.getDim()[1] * data.tileSize));
        });
    }

    private boolean isAtAssignedRoom(Guest g) {

        if (g.assignedRoomId == -1) return false;

        return Math.abs(g.x - g.targetX) < 15 &&
                Math.abs(g.y - g.targetY) < 15;
    }
}