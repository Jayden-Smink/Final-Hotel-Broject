package controller;

import hotelevents.HotelEvent;
import hotelevents.HotelEventListener;
import hotelevents.HotelEventManager;
import model.*;
import view.LogPanel;
import util.SoundManager; // Netjes geïmporteerd voor de SoundManager

/**
 * DE HOOFD-CONTROLLER (Verkeersregelaar):
 * Het hart van de logica. Luistert naar hotel-events en delegeert taken
 * naar de juiste gespecialiseerde sub-controllers.
 * Ondersteunt ook de HotelTimeEngine voor pauze en snelheidsregeling.
 */
public class SimulationController implements HotelEventListener {

    // HET GEHEUGEN: Gedeelde data (kamers, gasten, lift) voor alle controllers.
    private final SimulationData data;
    private final HotelTimeEngine hte;
    private int lastHteInterval = -1;
    int timer = 0;

    // DE ONDERAANNEMERS (Sub-controllers):
    private final ElevatorController elevatorController;          // Bepaalt waar de lift heen gaat.
    private final ReceptionistController receptionistController;  // Regelt kamer- en sleuteltoewijzing.
    private final GuestController guestController;                // Bepaalt bewegingen en acties van gasten.
    private final GuestActivityController guestActivityController;// Regelt groepsactiviteiten (bioscoop, fitness).
    private final CleanerController cleanerController;            // Stuurt de pool van schoonmakers aan.
    private final GodzillaController godzillaController;          // Beheert de Godzilla-aanval.

    // DE COMMUNICATIEMIDDELEN:
    private final LogPanel logPanel;              // Tekstvak op het scherm voor meldingen.
    private final HotelEventManager eventManager; // Motor die hotel-events genereert.
    private final SoundManager soundManager;      // Centraal opgeslagen voor brandalarm-muziek

    /**
     * DE OPSTART-FASERING (Constructor):
     * Maakt alle sub-controllers aan, verbindt ze, en start het gekozen scenario.
     */
    public SimulationController(SimulationData data, LogPanel logPanel, int selectedScenario, HotelTimeEngine hte, SoundManager soundManager) {
        this.data = data;
        this.logPanel = logPanel;
        this.hte = hte;
        this.soundManager = soundManager; // Sla de manager op

        // Iedere sub-controller krijgt toegang tot de data en het logpaneel.
        this.elevatorController = new ElevatorController(data, logPanel);
        this.receptionistController = new ReceptionistController(data, logPanel);
        this.guestController = new GuestController(data, logPanel, this.receptionistController);
        this.guestActivityController = new GuestActivityController(data, receptionistController, logPanel);
        this.cleanerController = new CleanerController(data, logPanel);

        // De soundManager wordt doorgegeven aan de GodzillaController!
        this.godzillaController = new GodzillaController(data, logPanel, new model.FireDestruction(3), soundManager);

        // Koppel deze controller aan de EventManager en stel het HTE-interval in.
        this.eventManager = new HotelEventManager();
        this.eventManager.register(this);
        eventManager.setHte(hte.getHteInterval());
        lastHteInterval = hte.getHteInterval();

        // Start het simulatiescenario.
        System.out.println("Gestart scenario: " + selectedScenario);
        this.eventManager.start(selectedScenario);
    }

    /**
     * HET MELDINGENCENTRUM (Event Listener):
     * Wordt automatisch aangeroepen door de HotelEventManager.
     * Stuurt het inkomende event door naar het juiste onderdeel.
     */
    @Override
    public void notify(HotelEvent event) {
        // Negeer events als de simulatie gepauzeerd is.
        if (hte.isPaused()) return;

        timer += 1;
        data.hteTicks = timer;

        switch (event.getEventType()) {

            // NIEUWE GAST: Geef het gast-ID en data door aan de GuestController.
            case CHECK_IN:
                guestController.processCheckIn(event.getGuestId(), event.getData());
                break;

            // VERTREKKENDE GAST: Verander status en stuur gast naar de Lobby-uitgang.
            case CHECK_OUT:
                Guest leavingGuest = data.guests.get(event.getGuestId());
                if (leavingGuest != null) {
                    leavingGuest.isCheckingOut = true;

                    // Zoek de Lobby-coördinaten voor de uitgang.
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

            // GAST WIL ETEN: (Restaurantlogica kan hier uitgebreid worden)
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

            // SCHOONMAAK-NOODGEVAL: Geef kamer-ID door aan de CleanerController.
            case CLEANING_EMERGENCY:
                int roomId = event.getData();
                cleanerController.handleCleaningEmergency(roomId);
                if (logPanel != null) logPanel.addLog("🧹 Cleaning emergency in kamer " + roomId + "!");
                break;

            // EVACUATIE VIA EVENT ENGINE:
            case EVACUATE:
                triggerEvacuate(); // Gebruikt nu de centrale methode inclusief muziekwissel
                if (logPanel != null) logPanel.addLog("🚨 EVACUATIE! Alle gasten verlaten het hotel.");
                break;

            // GODZILLA AANVAL:
            case GODZILLA:
                godzillaController.activate();
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
     * DE HARTSLAG (Game Loop Tick):
     * Aangeroepen elke frame. Deelt een tikje uit aan alle actieve onderdelen
     * en synchroniseert het HTE-interval als dat via het TimeControlPanel is gewijzigd.
     */
    public void updateTick() {
        // Pas eventManager aan als het interval gewijzigd is.
        int currentInterval = hte.getHteInterval();
        if (currentInterval != lastHteInterval) {
            eventManager.setHte(currentInterval);
            lastHteInterval = currentInterval;
        }

        guestController.update();                // Laat gasten een stapje lopen.
        elevatorController.update();             // Laat de lift een stukje stijgen/dalen.
        guestActivityController.updateActivities(); // Update hoe lang activiteiten nog duren.
        cleanerController.update();              // Laat de schoonmakers hun werk doen.
        godzillaController.update();             // Laat Godzilla het hotel verwoesten.
    }

    /** Activeer Godzilla direct (voor de test-knop). */
    public void triggerGodzilla() {
        godzillaController.activate();
    }

    /** Geeft true als Godzilla klaar is met het hotel verwoesten. */
    public boolean isGodzillaDone() {
        return godzillaController.isFinished();
    }

    /** Activeer brandevacuatie en start de alarmmuziek. */
    public void triggerEvacuate() {
        guestActivityController.evacuateAllGuests();
        cleanerController.evacuateAllCleaners();

        // ✅ GEFIXT: Wissel direct naar de alarmmuziek!
        if (soundManager != null) {
            soundManager.stopMusic();
            soundManager.playBackgroundMusic("/music/evacuate.wav");
        }
    }

    /**
     * DOORGEEFLUIKEN (Getters):
     * Zorgen dat andere klassen (bijv. de Renderer) bij de sub-controllers
     * kunnen om informatie op te vragen.
     */
    public CleanerController getCleanerController() {
        return this.cleanerController;
    }

    public GodzillaController getGodzillaController() {
        return this.godzillaController;
    }
}