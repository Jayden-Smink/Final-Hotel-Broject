package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;

public class SimulationController implements HotelEventListener {

    private final SimulationData data;

    private final ElevatorController elevatorController;
    private final ReceptionistController receptionistController;
    private final GuestController guestController;
    private final GuestActivityController guestActivityController;

    private final LogPanel logPanel;
    private final HotelEventManager eventManager;
    private final CleanerController cleanerController;

    public SimulationController(SimulationData data, LogPanel logPanel, int selectedScenario) {
        this.data = data;
        this.logPanel = logPanel;

        this.elevatorController = new ElevatorController(data, logPanel);
        this.receptionistController = new ReceptionistController(data, logPanel);
        this.guestController = new GuestController(data, logPanel, this.receptionistController);
        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);
        this.cleanerController = new CleanerController(data, logPanel);

        this.eventManager = new HotelEventManager();
        this.eventManager.register(this);

        System.out.println("Gestart scenario: " + selectedScenario);
        this.eventManager.start(selectedScenario);
    }

    @Override
    public void notify(HotelEvent event) {
        switch (event.getEventType()) {

            case CHECK_IN:
                guestController.processCheckIn(event.getGuestId(), event.getData());
                break;

            case CHECK_OUT:
                Guest leavingGuest = data.guests.get(event.getGuestId());

                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true;

                    for (int i = 0; i < data.areas.size(); i++) {
                        Area area = data.areas.get(i);
                        if (area.AreaType.equalsIgnoreCase("LOBBY")) {
                            double exitY = (area.getPos()[1] * data.tileSize) + data.tileSize / 2.0;
                            leavingGuest.setTarget(20.0, exitY);
                            break;
                        }
                    }

                    if (logPanel != null) logPanel.addLog("🚪 Gast " + leavingGuest.id + " checkt uit.");
                }
                break;

            case NEED_FOOD:
                if (logPanel != null) logPanel.addLog("🍔 Gast " + event.getGuestId() + " wil eten.");
                break;

            case GOTO_FITNESS:
                if (logPanel != null) logPanel.addLog("🏋️ Gast " + event.getGuestId() + " gaat fitnessen.");
                break;

            case GOTO_CINEMA:
                if (logPanel != null) logPanel.addLog("🎬 Gast " + event.getGuestId() + " gaat naar cinema.");
                break;

            case CLEANING_EMERGENCY:
                int roomId = event.getData();
                cleanerController.handleCleaningEmergency(roomId);
                if (logPanel != null) logPanel.addLog("🧹 Cleaning emergency in kamer " + roomId + "!");
                break;

            case EVACUATE:
                if (logPanel != null) logPanel.addLog("🚨 EVACUATIE!");
                break;

            case GODZILLA:
                if (logPanel != null) logPanel.addLog("🦖 GODZILLA ATTACK!");
                break;

            case START_CINEMA:
                if (logPanel != null) logPanel.addLog("🎥 Cinema gestart.");
                break;

            case NONE:
            default:
                break;
        }
    }

    public void updateTick() {
        guestController.update();
        elevatorController.update();
        guestActivityController.updateActivities();
        cleanerController.update();
    }

    public CleanerController getCleanerController() {
        return this.cleanerController;
    }
}