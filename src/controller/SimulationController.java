package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;

/**
 * DE HOOFD-CONTROLLER (Verkeersregelaar):
 * Deze klasse is het hart van de logica. Het luistert naar gebeurtenissen (events)
 * die in het hotel plaatsvinden en delegeert de taken naar de juiste gespecialiseerde controllers
 * (bijv. de GuestController of de CleanerController).
 */
public class SimulationController implements HotelEventListener {

    // HET GEHEUGEN: Bevat de gedeelde data (kamers, gasten, lift) waar alle controllers mee werken.
    private final SimulationData data;
    int timer = 0;


    // DE ONDERAANNEMERS (Sub-controllers):
    // Elk van deze controllers is verantwoordelijk voor een specifiek deel van de logica in het hotel.
    private final ElevatorController elevatorController;         // Bepaalt waar de lift heen gaat.
    private final ReceptionistController receptionistController; // Regelt het toewijzen van kamers en sleutels.
    private final GuestController guestController;               // Bepaalt de bewegingen en acties van de gasten.
    private final GuestActivityController guestActivityController; // Regelt groepsactiviteiten zoals de bioscoop of fitness.
    private final CleanerController cleanerController;           // Stuurt de pool van schoonmakers aan.
    private final GodzillaController godzillaController;         // Beheert de Godzilla aanval.

    // DE COMMUNICATIEMIDDELEN:
    private final LogPanel logPanel;             // Het tekstvak op het scherm waar we meldingen printen.
    private final HotelEventManager eventManager;// De motor die hotel-gebeurtenissen (zoals check-in, brand) genereert.

    // DE OPSTART-FASERING (Constructor):
    // Wordt aangeroepen als de simulatie begint. Maakt alle sub-controllers aan en verbindt ze met elkaar.
    public SimulationController(SimulationData data, LogPanel logPanel, int selectedScenario) {
        this.data = data;
        this.logPanel = logPanel;


        // Iedere sub-controller krijgt toegang tot de data en het logpaneel.
        this.elevatorController = new ElevatorController(data, logPanel);
        this.receptionistController = new ReceptionistController(data, logPanel);
        this.guestController = new GuestController(data, logPanel, this.receptionistController);
        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);
        this.cleanerController = new CleanerController(data, logPanel);
        this.godzillaController = new GodzillaController(data, logPanel, new model.FireDestruction(3));

        // Koppel deze controller aan de EventManager zodat we bericht krijgen als er iets gebeurt.
        this.eventManager = new HotelEventManager();
        this.eventManager.register(this);
        eventManager.setHte(100);

        // Start het simulatiescenario (de vooraf bepaalde lijst met events, bijv. "gast komt aan na 2 seconden").
        System.out.println("Gestart scenario: " + selectedScenario);
        this.eventManager.start(selectedScenario);
    }

    /**
     * HET MELDINGENCENTRUM (Event Listener):
     * Deze methode wordt automatisch aangeroepen door de HotelEventManager wanneer er iets in het hotel moet gebeuren.
     * Het is een soort wisselbord dat het inkomende event doorstuurt naar het juiste onderdeel.
     */
    @Override
    public void notify(HotelEvent event) {


        timer+= 1;
        System.out.println(timer);

        switch (event.getEventType()) {

            // NIEUWE GAST: Geef het gast-ID en de bijbehorende data door aan de GuestController.
            case CHECK_IN:
                guestController.processCheckIn(event.getGuestId(), event.getData());
                break;

            // VERTREKKENDE GAST: Zoek de gast op, verander zijn status, en stuur hem naar de Lobby om het gebouw te verlaten.
            case CHECK_OUT:
                Guest leavingGuest = data.guests.get(event.getGuestId());

                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true;

                    // Zoek de coördinaten van de LOBBY zodat we weten waar de uitgang is.
                    // We gebruiken een for-loop in plaats van een stream voor betere prestaties.
                    for (int i = 0; i < data.areas.size(); i++) {
                        Area area = data.areas.get(i);
                        if (area.AreaType.equalsIgnoreCase("LOBBY")) {
                            double exitY = (area.getPos()[1] * data.tileSize) + data.tileSize / 2.0;
                            leavingGuest.setTarget(20.0, exitY); // Stel het doel van de gast in op de uitgang
                            break;
                        }
                    }

                    if (logPanel != null) logPanel.addLog("🚪 Gast " + leavingGuest.id + " checkt uit.");
                }
                break;

            // GAST WIL ETEN: (Logica voor het restaurant kan hier nog verder uitgebreid worden)
            case NEED_FOOD:
                if (logPanel != null) logPanel.addLog("🍔 Gast " + event.getGuestId() + " wil eten.");
                break;

            // GAST GAAT SPORTEN:
            case GOTO_FITNESS:
                if (logPanel != null) logPanel.addLog("🏋️ Gast " + event.getGuestId() + " gaat fitnessen.");
                break;

            // GAST GAAT NAAR FILM:
            case GOTO_CINEMA:
                if (logPanel != null) logPanel.addLog("🎬 Gast " + event.getGuestId() + " gaat naar cinema.");
                break;

            // ER IS GEPOTST IN EEN KAMER / NOODGEVAL: Geef het kamer-ID door aan de CleanerController.
            case CLEANING_EMERGENCY:
                int roomId = event.getData(); // Haal op om welke kamer het gaat
                cleanerController.handleCleaningEmergency(roomId); // Zet de schoonmakers aan het werk
                if (logPanel != null) logPanel.addLog("🧹 Cleaning emergency in kamer " + roomId + "!");
                break;

            // SPECIALE EVENTS:
            case EVACUATE:
                for (int i = 0; i <= 10; i++){
                    System.out.println("🚨 EVACUATIE!");
                }
                guestActivityController.evacuateAllGuests();
                if (logPanel != null) logPanel.addLog("🚨 EVACUATIE! Alle gasten verlaten het hotel.");
                break;

            case GODZILLA:
                for (int i = 0; i <= 10; i++){
                    System.out.println("🦖 GODZILLA ATTACK!");
                }
                godzillaController.activate();
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
     * DE HARTSLAG (Game Loop Tick):
     * Deze methode wordt elke frame (of tick) van de simulatie aangeroepen door de main loop.
     * Het deelt een 'tikje' uit aan alle actieve onderdelen, zodat zij één stapje kunnen bewegen
     * of nadenken over hun volgende actie.
     */
    public void updateTick() {
        guestController.update();           // Laat gasten een stapje lopen
        elevatorController.update();        // Laat de lift een stukje stijgen/dalen
        guestActivityController.updateActivities(); // Update hoelang activiteiten nog duren
        cleanerController.update();         // Laat de schoonmakers hun werk doen
        godzillaController.update();        // Laat Godzilla het hotel verwoesten
    }

    /** Activeer Godzilla direct (voor de test-knop). */
    public void triggerGodzilla() {
        godzillaController.activate();
    }

    /** Geeft true terug als Godzilla klaar is met het hotel verwoesten. */
    public boolean isGodzillaDone() {
        return godzillaController.isFinished();
    }

    /**
     * DOORGEEFLUIK (Getter):
     * Zorgt ervoor dat andere klasses (zoals de Renderer uit het vorige script)
     * bij de CleanerController kunnen om informatie op te vragen (bijv. om ze te tekenen).
     */
    public CleanerController getCleanerController() {
        return this.cleanerController;
    }

    public GodzillaController getGodzillaController() {
        return this.godzillaController;
    }
}