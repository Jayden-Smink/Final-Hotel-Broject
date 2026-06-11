package controller;

import model.Cleaner;
import model.CleanerState;
import model.SimulationData;
import view.LogPanel;

public class CleanerStateHandler {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final CleanerAssigner assigner;

    public CleanerStateHandler(SimulationData data, LogPanel logPanel, CleanerAssigner assigner) {
        this.data = data;
        this.logPanel = logPanel;
        this.assigner = assigner;
    }

    public void update(Cleaner worker) {
        if (worker.state == CleanerState.CLEANING) {
            worker.cleaningTimer++;
            if (worker.cleaningTimer >= data.cleanerSettings.getCleaningDurationFrames()) {
                worker.cleaningTimer = 0;
                finishCleaning(worker);
            }
        }

        if (isNearTarget(worker)) {
            handleArrival(worker);
        }

        if (worker.state == CleanerState.IDLE && !worker.dirtyRooms.isEmpty()) {
            int nextRoomId = worker.dirtyRooms.remove(0);
            assigner.assignToRoom(worker, nextRoomId);
        }
    }

    private void finishCleaning(Cleaner worker) {
        if (!worker.dirtyRooms.isEmpty()) {
            int nextRoomId = worker.dirtyRooms.remove(0);
            assigner.assignToRoom(worker, nextRoomId);
            if (logPanel != null) logPanel.addLog("✅ Klaar! Schoonmaker " + worker.id + " gaat naar volgende kamer.");
        } else {
            worker.state = CleanerState.WALKING_BACK;
            assigner.sendToLobby(worker);
            if (logPanel != null) logPanel.addLog("✅ Schoonmaker " + worker.id + " klaar! Gaat terug naar de lobby.");
        }
    }

    private void handleArrival(Cleaner worker) {
        if (worker.state == CleanerState.WALKING_TO_ROOM) {
            worker.state = CleanerState.CLEANING;
            if (logPanel != null) logPanel.addLog("🧽 Schoonmaker " + worker.id + " is begonnen met schoonmaken.");
        } else if (worker.state == CleanerState.WALKING_BACK) {
            worker.state = CleanerState.IDLE;
            worker.assignedRoomId = -1;
        }
    }

    private boolean isNearTarget(Cleaner worker) {
        return Math.abs(worker.x - worker.targetX) < 5 && Math.abs(worker.y - worker.targetY) < 5;
    }
}