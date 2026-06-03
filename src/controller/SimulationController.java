package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;
import controller.CleanerController;

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

    private final HotelEventManager eventManager; // De externe event-generator (uit de JAR)
    private final CleanerController cleanerController;

    public SimulationController(SimulationData data, LogPanel logPanel) {

        this.data = data;
        this.logPanel = logPanel;

        // Initialiseer alle sub-controllers en geef de benodigde data/panels mee
        this.elevatorController = new ElevatorController(data);
        this.receptionistController = new ReceptionistController(data, logPanel);
        this.guestController = new GuestController(data, logPanel);
        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);
        this.cleanerController = new CleanerController(data, logPanel);

        // OBSERVER SETUP: Registreer deze controller bij de eventManager zodat notify() wordt aangeroepen bij events
        this.eventManager = new HotelEventManager();
        eventManager.register(this);

        // Start het scenario (in dit geval scenario 3)
        eventManager.start(3);
    }

    /**
     * OBSERVER PATTERN - Vangt binnenkomende gebeurtenissen op vanuit de HotelEventManager.
     */
    @Override
    public void notify(HotelEvent event) {

        // Handel het event af op basis van het type (EventType)
        switch (event.getEventType()) {

            case CHECK_IN:
                // Maak een nieuw, leeg gast-object aan op positie (0,0)
                Guest guest = new Guest(
                        event.getGuestId(),
                        0,
                        0
                );

                // Laat de GuestController de gast fysiek in de lobby spawnen en naar de receptie sturen
                guestController.spawnGuest(guest);

                // Laat de receptionist administratief een kamer zoeken en toewijzen
                receptionistController.handleCheckIn(
                        event.getGuestId(),
                        event.getData()
                );

                if (logPanel != null) {
                    logPanel.addLog("👤 Gast " + guest.id + " is ingecheckt.");
                }
                break;

            case CHECK_OUT:
                // Haal de vertrekkende gast op uit de centrale database
                Guest leavingGuest = data.guests.get(event.getGuestId());

                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true; // Zet de vlag op uitchecken

                    // Zoek de lobby op om te bepalen naar welke hoogte (Y) de gast moet lopen om het hotel te verlaten
                    data.areas.stream()
                            .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                            .findFirst()
                            .ifPresent(lobby -> {
                                double exitY = (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;
                                leavingGuest.setTarget(20.0, exitY); // Stuur de gast naar de hoteluitgang (X=20)
                            });

                    if (logPanel != null) {
                        logPanel.addLog("🚪 Gast " + leavingGuest.id + " checkt uit.");
                    }
                }
                // Merk op: de daadwerkelijke verwijdering uit het hotel gebeurt in de GuestActivityController zodra het doel bereikt is.
                break;

            case NEED_FOOD:
                if (logPanel != null) {
                    logPanel.addLog("🍔 Gast " + event.getGuestId() + " wil eten.");
                }
                break;

            case GOTO_FITNESS:
                if (logPanel != null) {
                    logPanel.addLog("🏋️ Gast " + event.getGuestId() + " gaat fitnessen.");
                }
                break;

            case GOTO_CINEMA:
                if (logPanel != null) {
                    logPanel.addLog("🎬 Gast " + event.getGuestId() + " gaat naar cinema.");
                }
                break;

            case CLEANING_EMERGENCY:
                // Delegeer het noodgeval direct naar de CleanerController
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
     * MAIN GAME LOOP - Wordt via de GameLoop klasse elke frame/tick aangeroepen.
     * Dit zorgt ervoor dat de hele simulatie synchroon blijft lopen.
     */
    public void updateTick() {
        // 1. Verplaats de lopende gasten een stapje
        guestController.update();

        // 2. Laat de lift bewegen en mensen in-/uitstappen
        elevatorController.update();

        // 3. Update de timers en het AI-gedrag van de gasten (ruimtewissels)
        guestActivityController.updateActivities();

        // 4. Verplaats de schoonmaker en update zijn poets-timer
        cleanerController.update();
    }
}