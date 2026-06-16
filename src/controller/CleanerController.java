package controller;

import model.Cleaner;
import model.StairModel;
import model.SimulationData;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

public class CleanerController {
    private final SimulationData data;
    private final CleanerMover cleanerMover;
    private final CleanerStateHandler stateHandler;
    private final EmergencyHandler emergencyHandler;

    public CleanerController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        CleanerAssigner assigner = new CleanerAssigner(data, logPanel);
        this.cleanerMover = new CleanerMover(data, new StairModel(data.areas));
        this.stateHandler = new CleanerStateHandler(data, logPanel, assigner);
        this.emergencyHandler = new EmergencyHandler(data, logPanel, assigner);
    }

    public void handleCleaningEmergency(int roomId) {
        emergencyHandler.handle(roomId);
    }

    public void update() {
        List<Cleaner> active = new ArrayList<>(data.cleaners.values());
        for (Cleaner worker : active) {
            if (worker.isDead) continue;
            cleanerMover.moveCleaner(worker);
            stateHandler.update(worker);
        }
    }

    public List<Cleaner> getActiveCleaners() {
        return new ArrayList<>(data.cleaners.values());
    }
}