package controller;

import model.Cleaner;
import model.CleanerState;
import model.SimulationData;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

public class EmergencyHandler {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final CleanerAssigner assigner;

    public EmergencyHandler(SimulationData data, LogPanel logPanel, CleanerAssigner assigner) {
        this.data = data;
        this.logPanel = logPanel;
        this.assigner = assigner;
    }

    public void handle(int roomId) {
        if (data.cleaners.isEmpty()) return;

        int targetRoomId = assigner.findFirstRoomId();
        if (targetRoomId == -1) return;

        Cleaner best = selectBestCleaner();
        if (best == null) return;

        if (best.state == CleanerState.IDLE) {
            assigner.assignToRoom(best, targetRoomId);
        } else {
            best.dirtyRooms.add(targetRoomId);
        }

        if (logPanel != null) logPanel.addLog("🚨 Noodgeval! Schoonmaker " + best.id + " gestuurd naar kamer " + targetRoomId);
    }

    private Cleaner selectBestCleaner() {
        List<Cleaner> cleaners = new ArrayList<>(data.cleaners.values());
        for (Cleaner c : cleaners) {
            if (c.state == CleanerState.IDLE) return c;
        }
        Cleaner best = null;
        for (Cleaner c : cleaners) {
            if (best == null || c.dirtyRooms.size() < best.dirtyRooms.size()) best = c;
        }
        return best;
    }
}