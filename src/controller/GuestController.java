package controller;

import model.*;
import view.LogPanel;

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

        this.guestMover =
                new GuestMover(
                        data,
                        new StairModel(data.areas)
                );
    }

    /**
     * Update alle gasten.
     * Deze methode wordt elke tick aangeroepen.
     */
    public void update() {

        for (Guest g : data.guests.values()) {

            // Gast komt uit lift
            if (g.state == GuestState.EXITING_LIFT) {
                g.state = GuestState.WALKING;
            }

            // Gast bewegen
            guestMover.moveGuest(g);
        }
    }

    /**
     * Nieuwe gast toevoegen aan simulatie.
     * Wordt aangeroepen vanuit notify().
     */
    public void spawnGuest(Guest guest) {

        // Zoek lobby
        data.areas.stream()
                .filter(a ->
                        a.AreaType.equalsIgnoreCase("LOBBY")
                )
                .findFirst()
                .ifPresent(lobby -> {

                    double lobbyY =
                            (lobby.getPos()[1] * data.tileSize) + data.tileSize/2.0;

                    // Startpositie links
                    guest.x = 20.0;
                    guest.y = lobbyY;

                    guest.state = GuestState.WALKING;

                    // Zoek receptie
                    data.areas.stream()
                            .filter(a -> a.AreaType.equalsIgnoreCase("RECEPTION"))
                            .findFirst()
                            .ifPresent(reception -> {

                                // No + 60 here — renderer adds the offset at draw time
                                double receptionX = (reception.getPos()[0] * data.tileSize)
                                        + ((reception.getDim()[0] * data.tileSize) / 2.0);

                                guest.setTarget(receptionX, lobbyY);
                            });

                    // Gast toevoegen aan simulatie
                    data.guests.put(guest.id, guest);

                    // Log bericht
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