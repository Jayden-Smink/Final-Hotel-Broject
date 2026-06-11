package controller;

import model.Area;
import model.Cleaner;
import model.CleanerState;
import model.SimulationData;
import view.LogPanel;
import java.util.List;

public class CleanerAssigner {
    private final SimulationData data;
    private final LogPanel logPanel;

    private final int tileSize = 60;
    private final int horizontalOffset = 60;

    public CleanerAssigner(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    public void assignToRoom(Cleaner cleaner, int roomId) {
        List<Area> areas = data.areas;
        for (int i = 0; i < areas.size(); i++) {
            Area area = areas.get(i);
            if (area.id == roomId && area.AreaType.equalsIgnoreCase("ROOM")) {
                cleaner.assignedRoomId = area.id;
                cleaner.state = CleanerState.WALKING_TO_ROOM;

                double targetX = (area.getPos()[0] * tileSize) + horizontalOffset + (area.getDim()[0] * tileSize / 2.0);
                double targetY = (area.getPos()[1] * tileSize) + 25.0;
                cleaner.setTarget(targetX, targetY);

                if (logPanel != null) logPanel.addLog("🧹 Schoonmaker " + cleaner.id + " gaat naar kamer " + area.id + " op verdieping " + area.getPos()[1]);
                return;
            }
        }
    }

    public void sendToLobby(Cleaner cleaner) {
        List<Area> areas = data.areas;
        for (int i = 0; i < areas.size(); i++) {
            Area area = areas.get(i);
            if (area.AreaType.equalsIgnoreCase("LOBBY")) {
                double targetX = (area.getPos()[0] * tileSize) + horizontalOffset + ((area.getDim()[0] * tileSize) / 2.0);
                double targetY = (area.getPos()[1] * tileSize) + 25.0;
                cleaner.setTarget(targetX, targetY);
                return;
            }
        }
    }

    public int findFirstRoomId() {
        List<Area> areas = data.areas;
        for (int i = 0; i < areas.size(); i++) {
            Area area = areas.get(i);
            if (area.AreaType.equalsIgnoreCase("ROOM")) return area.id;
        }
        return -1;
    }
}