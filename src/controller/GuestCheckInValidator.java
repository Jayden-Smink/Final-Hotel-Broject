package controller;

import model.Area;
import model.SimulationData;
import view.LogPanel;

public class GuestCheckInValidator {
    private final SimulationData data;
    private final LogPanel logPanel;

    public GuestCheckInValidator(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    public boolean validate(int guestId) {
        if (guestId <= 0) {
            log("⚠️ Ongeldige guestId genegeerd: " + guestId);
            return false;
        }
        if (data.guests.containsKey(guestId)) {
            log("⚠️ Dubbele check-in genegeerd voor gast " + guestId + ".");
            return false;
        }
        if (!hasArea("LOBBY")) {
            log("⚠️ Gast " + guestId + " kan niet spawnen: lobby ontbreekt.");
            return false;
        }
        if (!hasArea("RECEPTION")) {
            log("⚠️ Gast " + guestId + " kan niet spawnen: receptie ontbreekt.");
            return false;
        }
        return true;
    }

    private boolean hasArea(String type) {
        if (data.areas == null) return false;
        for (int i = 0; i < data.areas.size(); i++) {
            if (data.areas.get(i).AreaType.equalsIgnoreCase(type)) return true;
        }
        return false;
    }

    private void log(String message) {
        if (logPanel != null) logPanel.addLog(message);
    }
}