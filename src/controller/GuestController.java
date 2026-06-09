package controller;

import factory.PersonFactory;
import model.*;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Beheert de basishandelingen van alle gasten, zoals het aanmaken via de factory,
 * het invoeren van nieuwe gasten in de simulatie (spawnen) en het aansturen van hun frame-by-frame bewegingen.
 */
public class GuestController {

    private final SimulationData data;
    private final LogPanel logPanel;
    private final GuestMover guestMover;
    private final ReceptionistController receptionistController;

    public GuestController(
            SimulationData data,
            LogPanel logPanel,
            ReceptionistController receptionistController
    ) {
        this.data = data;
        this.logPanel = logPanel;
        this.receptionistController = receptionistController;

        this.guestMover =
                new GuestMover(
                        data,
                        new StairModel(data.areas)
                );
    }

    /**
     * BEHEERT DE INCHECK-LOGICA.
     *
     * Deze methode vangt de kale data op uit de SimulationController,
     * maakt zelf de gast aan via de factory en delegeert de administratie.
     *
     * Extra bescherming:
     * - ongeldige guestId wordt genegeerd
     * - dubbele check-in met dezelfde guestId wordt genegeerd
     * - gast wordt alleen aangemaakt als LOBBY en RECEPTION bestaan
     */
    public void processCheckIn(int guestId, int preferredRoomId) {

        if (guestId <= 0) {
            if (logPanel != null) {
                logPanel.addLog("⚠️ Ongeldige guestId genegeerd: " + guestId);
            }
            return;
        }

        if (data.guests.containsKey(guestId)) {
            if (logPanel != null) {
                logPanel.addLog("⚠️ Dubbele check-in genegeerd voor gast " + guestId + ".");
            }
            return;
        }

        if (!hasArea("LOBBY")) {
            if (logPanel != null) {
                logPanel.addLog("⚠️ Gast " + guestId + " kan niet spawnen: lobby ontbreekt.");
            }
            return;
        }

        if (!hasArea("RECEPTION")) {
            if (logPanel != null) {
                logPanel.addLog("⚠️ Gast " + guestId + " kan niet spawnen: receptie ontbreekt.");
            }
            return;
        }

        Guest guest = (Guest) PersonFactory.createPerson(
                PersonType.GUEST,
                guestId,
                0,
                0
        );

        boolean spawned = spawnGuest(guest);

        if (!spawned) {
            if (logPanel != null) {
                logPanel.addLog("⚠️ Gast " + guestId + " kon niet correct gespawned worden.");
            }
            return;
        }

        receptionistController.handleCheckIn(guestId, preferredRoomId);

        if (logPanel != null) {
            logPanel.addLog("👤 Gast " + guest.id + " is ingecheckt.");
        }
    }

    /**
     * Update alle actieve gasten in het hotel.
     * Deze methode wordt elke tick/frame aangeroepen vanuit de hoofdloop.
     */
    public void update() {

        List<Guest> guestList = new ArrayList<>(data.guests.values());
        for (int i = 0; i < guestList.size(); i++) {
            Guest guest = guestList.get(i);

            if (guest == null) {
                continue;
            }

            if (guest.state == GuestState.EXITING_LIFT) {
                guest.state = GuestState.WALKING;
            }

            guestMover.moveGuest(guest);
        }
    }

    /**
     * Voegt een gast fysiek toe aan het hotel aan de linkerkant van de lobby.
     * Berekent direct het loopdoel naar de receptie.
     *
     * Return:
     * true  = gast is succesvol gespawned
     * false = gast kon niet gespawned worden
     */
    public boolean spawnGuest(Guest guest) {

        if (guest == null) {
            return false;
        }

        Area lobby = findAreaByType("LOBBY");
        Area reception = findAreaByType("RECEPTION");

        if (lobby == null || reception == null) {
            return false;
        }

        double lobbyY =
                (lobby.getPos()[1] * data.tileSize)
                        + data.tileSize / 2.0;

        guest.x = 20.0;
        guest.y = lobbyY;

        guest.state = GuestState.WALKING;
        guest.isInRoom = false;
        guest.isCheckingOut = false;

        /*
         * Belangrijk:
         * Deze status voorkomt dat dezelfde gast steeds opnieuw door receptie-logica wordt verwerkt
         * wanneer hij toevallig nog binnen de RECEPTION-bounds staat.
         */
        guest.currentActivity = "WALKING_TO_RECEPTION";
        guest.currentFacility = "";
        guest.activityTimer = 0;

        double receptionX =
                (reception.getPos()[0] * data.tileSize)
                        + ((reception.getDim()[0] * data.tileSize) / 2.0);

        guest.setTarget(receptionX, lobbyY);

        data.guests.put(guest.id, guest);

        return true;
    }

    private boolean hasArea(String type) {
        return findAreaByType(type) != null;
    }

    private Area findAreaByType(String type) {
        if (data.areas == null) {
            return null;
        }

        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            if (area.AreaType.equalsIgnoreCase(type)) {
                return area;
            }
        }

        return null;
    }
}