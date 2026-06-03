package controller;

import model.*;
import view.LogPanel;

/**
 * Beheert de basishandelingen van alle gasten, zoals het invoeren van nieuwe gasten
 * in de simulatie (spawnen) en het aansturen van hun frame-by-frame bewegingen.
 */
public class GuestController {

    private final SimulationData data;
    private final LogPanel logPanel;
    private final GuestMover guestMover;

    public GuestController(
            SimulationData data,
            LogPanel logPanel
    ) {
        this.data = data;
        this.logPanel = logPanel;

        // Maak de bewegingsmotor aan en geef hem direct de trappendata mee
        this.guestMover =
                new GuestMover(
                        data,
                        new StairModel(data.areas)
                );
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
     * Voegt een gloednieuwe gast toe aan het hotel aan de linkerkant van de lobby.
     * Wordt getriggerd wanneer de SimulationController een CHECK_IN event ontvangt.
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

                    if (logPanel != null) {
                        logPanel.addLog(
                                "🧍 Gast "
                                        + guest.id
                                        + " komt het hotel binnen."
                        );
                    }
                });
    }
}