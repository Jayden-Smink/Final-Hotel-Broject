package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;
import controller.CleanerController;

public class SimulationController implements HotelEventListener {

    private final SimulationData data;

    private final ElevatorController elevatorController;
    private final ReceptionistController receptionistController;
    private final GuestController guestController;
    private final GuestActivityController guestActivityController;

    private final LogPanel logPanel;

    private final HotelEventManager eventManager;
    private final CleanerController cleanerController;

    public SimulationController(SimulationData data, LogPanel logPanel) {

        this.data = data;
        this.logPanel = logPanel;

        this.elevatorController = new ElevatorController(data);

        this.receptionistController = new ReceptionistController(data, logPanel);

        this.guestController = new GuestController(data, logPanel);

        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);

        this.cleanerController = new CleanerController(data, logPanel);

        // Observer setup
        this.eventManager = new HotelEventManager();
        eventManager.register(this);

        // Start scenario
        eventManager.start(3);
    }

    /**
     * OBSERVER PATTERN
     */
    @Override
    public void notify(HotelEvent event) {

        switch (event.getEventType()) {

            case CHECK_IN:

                Guest guest = new Guest(
                        event.getGuestId(),
                        0,
                        0
                );

                guestController.spawnGuest(guest);

                receptionistController.handleCheckIn(
                        event.getGuestId(),
                        event.getData()
                );

                if (logPanel != null) {
                    logPanel.addLog(
                            "👤 Gast " + guest.id + " is ingecheckt."
                    );
                }

                break;

            case CHECK_OUT:

                Guest leavingGuest = data.guests.get(event.getGuestId());

                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true;

                    // Find lobby floor so the guest walks to the correct Y
                    data.areas.stream()
                            .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                            .findFirst()
                            .ifPresent(lobby -> {
                                double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;
                                leavingGuest.setTarget(20.0, exitY);
                            });

                    if (logPanel != null) {
                        logPanel.addLog("🚪 Gast " + leavingGuest.id + " checkt uit.");
                    }
                }

                break;

            case NEED_FOOD:

                if (logPanel != null) {
                    logPanel.addLog(
                            "🍔 Gast " + event.getGuestId() + " wil eten."
                    );
                }
                break;

            case GOTO_FITNESS:

                if (logPanel != null) {
                    logPanel.addLog(
                            "🏋️ Gast " + event.getGuestId() + " gaat fitnessen."
                    );
                }
                break;

            case GOTO_CINEMA:

                if (logPanel != null) {
                    logPanel.addLog(
                            "🎬 Gast " + event.getGuestId() + " gaat naar cinema."
                    );
                }
                break;

            case CLEANING_EMERGENCY:
                cleanerController.handleCleaningEmergency(event.getData());
                if (logPanel != null) logPanel.addLog("🧹 Cleaning emergency!");
                break;

            case EVACUATE:

                if (logPanel != null) {
                    logPanel.addLog("🚨 EVACUATIE!");
                }
                break;

            case GODZILLA:

                if (logPanel != null) {
                    logPanel.addLog("🦖 GODZILLA ATTACK!");
                }
                break;

            case START_CINEMA:

                if (logPanel != null) {
                    logPanel.addLog("🎥 Cinema gestart.");
                }
                break;

            case NONE:
            default:
                break;
        }
    }

    /**
     * MAIN GAME LOOP
     */
    public void updateTick() {

        guestController.update();

        elevatorController.update();

        guestActivityController.updateActivities();

        cleanerController.update();
    }
}