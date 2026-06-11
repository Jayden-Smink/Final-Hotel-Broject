package controller;

import model.Area;
import model.Guest;
import model.GuestState;
import model.SimulationData;
import view.LogPanel;

public class GuestSpawner {
    private final SimulationData data;
    private final LogPanel logPanel;

    public GuestSpawner(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    public boolean spawn(Guest guest) {
        if (guest == null) return false;

        Area lobby = findAreaByType("LOBBY");
        Area reception = findAreaByType("RECEPTION");

        if (lobby == null || reception == null) return false;

        double lobbyY = (lobby.getPos()[1] * data.tileSize) + data.tileSize / 2.0;

        guest.x = 20.0;
        guest.y = lobbyY;
        guest.state = GuestState.WALKING;
        guest.isInRoom = false;
        guest.isCheckingOut = false;
        guest.currentActivity = "WALKING_TO_RECEPTION";
        guest.currentFacility = "";
        guest.activityTimer = 0;

        double receptionX = (reception.getPos()[0] * data.tileSize)
                + ((reception.getDim()[0] * data.tileSize) / 2.0);
        guest.setTarget(receptionX, lobbyY);

        data.guests.put(guest.id, guest);

        if (logPanel != null) logPanel.addLog("👤 Gast " + guest.id + " is ingecheckt.");
        return true;
    }

    private Area findAreaByType(String type) {
        if (data.areas == null) return null;
        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            if (area.AreaType.equalsIgnoreCase(type)) return area;
        }
        return null;
    }
}