package controller;

import factory.PersonFactory;
import model.*;
import view.LogPanel;

/**
 * Beheert de basishandelingen van alle gasten, zoals het aanmaken via de factory,
 * het invoeren van nieuwe gasten in de simulatie (spawnen) en het aansturen van hun frame-by-frame bewegingen.
 */
public class GuestController {

    private final SimulationData data;
    private final LogPanel logPanel;
    private final GuestMover guestMover;
    private final ReceptionistController receptionistController; // Toegevoegd om de incheckregie te voeren

    // Constructor aangepast om ook de ReceptionistController te ontvangen
    public GuestController(
            SimulationData data,
            LogPanel logPanel,
            ReceptionistController receptionistController
    ) {
        this.data = data;
        this.logPanel = logPanel;
        this.receptionistController = receptionistController;

        // Maak de bewegingsmotor aan en geef hem direct de trappendata mee
        this.guestMover =
                new GuestMover(
                        data,
                        new StairModel(data.areas)
                );
    }

    /**
     * BEHEERT DE INCHECK-LOGICA (Architectuur aanpassing docent)
     * Deze methode vangt de kale data op uit de SimulationController,
     * maakt zelf de gast aan via de factory en delegeert de administratie.
     */
    public void processCheckIn(int guestId, int preferredRoomId) {
        // 1. Maak de gast dynamisch aan via de PersonFactory
        Guest guest = PersonFactory.createGuest(PersonType.GUEST, guestId, 0, 0);

        // 2. Zet de gast fysiek op zijn startpositie in het hotel
        this.spawnGuest(guest);

        // 3. Stuur de receptionist aan om de administratieve kamerreservering te verwerken
        receptionistController.handleCheckIn(guestId, preferredRoomId);

        // 4. Schrijf de melding naar het logpaneel
        if (logPanel != null) {
            logPanel.addLog("👤 Gast " + guest.id + " is ingecheckt.");
        }
    }

    /**
     * Update alle actieve gasten in het hotel.
     * Deze methode wordt elke tick (frame) aangeroepen vanuit de hoofdloop.
     */
    public void update() {

        for (Guest g : data.guests.values()) {

            // Statuswissel: Als een gast net de lift uitstapt, mag hij nu weer gewoon gaan lopen
            if (g.state == GuestState.EXITING_LIFT) {
                g.state = GuestState.WALKING;
            }

            // Bereken en verplaats de gast een stapje dichter bij zijn huidige targetX en targetY
            guestMover.moveGuest(g);
        }
    }

    /**
     * Voegt een gast fysiek toe aan het hotel aan de linkerkant van de lobby.
     * Berekent direct het loopdoel naar de receptie.
     */
    public void spawnGuest(Guest guest) {

        // 1. Zoek eerst de lobby op om te bepalen op welke hoogte (Y-as) de gast moet starten
        data.areas.stream()
                .filter(a ->
                        a.AreaType.equalsIgnoreCase("LOBBY")
                )
                .findFirst()
                .ifPresent(lobby -> {

                    // Bereken het verticale middelpunt van de lobby
                    double lobbyY =
                            (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;

                    // Zet de startpositie helemaal links bij de denkbeeldige ingang
                    guest.x = 20.0;
                    guest.y = lobbyY;

                    // Zet de gast direct in de wandelmodus
                    guest.state = GuestState.WALKING;

                    // 2. Zoek de receptie op, want daar moet de gast nu als eerste naartoe lopen
                    data.areas.stream()
                            .filter(a -> a.AreaType.equalsIgnoreCase("RECEPTION"))
                            .findFirst()
                            .ifPresent(reception -> {

                                // Bereken het horizontale middelpunt van de receptiebalie
                                double receptionX = (reception.getPos()[0] * data.tileSize)
                                        + ((reception.getDim()[0] * data.tileSize) / 2.0);

                                // Stel het eerste doel (target) in: loop horizontaal door de lobby naar de receptie
                                guest.setTarget(receptionX, lobbyY);
                            });

                    // 3. Registreer de gast officieel in de centrale simulatiedatabase
                    data.guests.put(guest.id, guest);
                });
    }
}