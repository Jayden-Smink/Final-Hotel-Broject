package controller;

import model.*;
import view.LogPanel;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Verantwoordelijk voor het berekenen en instellen van loopbestemmingen van gasten (SRP).
 * Weet waar de exit, faciliteiten en kamers zijn — de rest van de code niet.
 */
public class GuestNavigator {

    private final SimulationData data;
    private final LogPanel logPanel;
    private final Random random = new Random();

    public GuestNavigator(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    /**
     * Stuurt een gast naar de hoteluitgang (linkerkant van de lobby).
     */
    public void sendGuestToExit(Guest g) {
        data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                .findFirst()
                .ifPresent(lobby -> {
                    double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize / 2.0;
                    g.setTarget(20.0, exitY);
                });
    }

    /**
     * Kiest een willekeurige faciliteit en stuurt de gast daarheen.
     */
    public void sendGuestToRandomFacility(Guest g) {
        List<String> types = Arrays.asList("RESTAURANT", "CINEMA", "FITNESS");

        var facilities = data.areas.stream()
                .filter(a -> types.contains(a.AreaType.toUpperCase()))
                .toList();

        if (!facilities.isEmpty()) {
            var area = facilities.get(random.nextInt(facilities.size()));

            double targetX = (area.getPos()[0] * data.tileSize) + ((area.getDim()[0] * data.tileSize) / 2.0);
            double targetY = (area.getPos()[1] * data.tileSize) + data.tileSize / 2.0;

            g.currentActivity = "WALKING_TO_FACILITY";
            g.currentFacility = area.AreaType.toUpperCase();
            g.setTarget(targetX, targetY);

            if (logPanel != null) {
                logPanel.addLog("🏃 Activiteit: Gast " + g.id + " loopt naar het " + area.AreaType.toLowerCase() + ".");
            }
        }
    }

    /**
     * Stuurt de gast terug naar zijn eigen gereserveerde hotelkamer.
     */
    public void returnGuestToRoom(Guest g) {
        if (g.assignedRoomId == -1) return;

        data.areas.stream()
                .filter(a -> a.id == g.assignedRoomId)
                .findFirst()
                .ifPresent(room -> {
                    double targetX = (room.getPos()[0] * data.tileSize) + ((room.getDim()[0] * data.tileSize) / 2.0);
                    double targetY = (room.getPos()[1] * data.tileSize) + data.tileSize / 2.0;

                    g.currentActivity = "WALKING_TO_ROOM";
                    g.setTarget(targetX, targetY);

                    if (logPanel != null) {
                        logPanel.addLog("🛏️ Terugkeer: Gast " + g.id + " loopt terug naar hotelkamer " + g.assignedRoomId + ".");
                    }
                });
    }
}