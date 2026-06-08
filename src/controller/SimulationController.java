package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;

/**
 * De centrale controller van de simulatie. Deze klasse luistert naar hotel-events
 * (Observer Pattern) en stuurt bij elke klokslag alle sub-controllers aan.
 */
public class SimulationController implements HotelEventListener {

    private final SimulationData data;

    // De sub-controllers die elk een specifiek onderdeel van het hotel beheren
    private final ElevatorController elevatorController;
    private final ReceptionistController receptionistController;
    private final GuestController guestController;
    private final GuestActivityController guestActivityController;

    private final LogPanel logPanel;
    private final HotelEventManager eventManager; // We maken de variabele hier weer netjes aan
    private final CleanerController cleanerController;

    public SimulationController(SimulationData data, LogPanel logPanel, int selectedScenario) {

        this.data = data;
        this.logPanel = logPanel;

        // 1. Initialiseer de sub-controllers
        this.elevatorController = new ElevatorController(data, logPanel);
        this.receptionistController = new ReceptionistController(data, logPanel);
        this.guestController = new GuestController(data, logPanel, this.receptionistController);
        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);
        this.cleanerController = new CleanerController(data, logPanel);

        // GEFIXT: We maken de instantie weer aan via 'new' om de non-static error op te lossen
        this.eventManager = new HotelEventManager();
        this.eventManager.register(this);

        // Start het gekozen scenario via de gemaakte instantie
        System.out.println("Gestart scenario: " + selectedScenario);
        this.eventManager.start(selectedScenario);
    }

    /**
     * OBSERVER PATTERN - Vangt binnenkomende gebeurtenissen op vanuit de HotelEventManager.
     */
    @Override
    public void notify(HotelEvent event) {

        // Handel het event af op basis van het type (EventType)
        switch (event.getEventType()) {

            case CHECK_IN:
                guestController.processCheckIn(
                        event.getGuestId(),
                        event.getData()
                );
                break;

            case CHECK_OUT:
                Guest leavingGuest = data.guests.get(event.getGuestId());

                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true;

                    data.areas.stream()
                            .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                            .findFirst()
                            .ifPresent(lobby -> {
                                double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize / 2.0;
                                leavingGuest.setTarget(20.0, exitY);
                            });

                    if (logPanel != null) {
                        logPanel.addLog("🚪 Gast " + leavingGuest.id + " checkt uit.");
                    }
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
                // GEFIXT: event.getData() is al een int. Geen parseInt-fouten of crashes meer!
                int roomId = event.getData();
                cleanerController.handleCleaningEmergency(roomId);

                if (logPanel != null) {
                    logPanel.addLog("🧹 Cleaning emergency in kamer " + roomId + "!");
                }
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

    /**
     * MAIN GAME LOOP
     */
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