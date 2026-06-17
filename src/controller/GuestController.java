package controller;

import model.Guest;
import model.GuestState;
import model.SimulationData;
import model.StairModel;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

public class GuestController {
    private final SimulationData data;
    private final LogPanel logPanel;
    private final GuestMover guestMover;
    private final ReceptionistController receptionistController;
    private final GuestCheckInValidator validator;
    private final GuestCreator creator;
    private final GuestSpawner spawner;

    public GuestController(SimulationData data, LogPanel logPanel,
                           ReceptionistController receptionistController) {
        this.data = data;
        this.logPanel = logPanel;
        this.receptionistController = receptionistController;
        this.guestMover = new GuestMover(data, new StairModel(data.areas));
        this.validator = new GuestCheckInValidator(data, logPanel);
        this.creator = new GuestCreator();
        this.spawner = new GuestSpawner(data, logPanel);
    }

    public void processCheckIn(int guestId, int preferredRoomId) {
        if (!validator.validate(guestId)) return;

        Guest guest = creator.create(guestId);

        if (!spawner.spawn(guest)) {
            if (logPanel != null) logPanel.addLog("⚠️ Gast " + guestId + " kon niet correct gespawned worden.");
            return;
        }

        receptionistController.handleCheckIn(guestId, preferredRoomId);
    }

    public void update() {
        List<Guest> guestList = new ArrayList<>(data.guests.values());
        for (int i = 0; i < guestList.size(); i++) {
            Guest guest = guestList.get(i);
            if (guest == null) continue;
            if (guest.state == GuestState.DEAD) continue;
            if (guest.state == GuestState.EXITING_LIFT) guest.state = GuestState.WALKING;
            guestMover.moveGuest(guest);
        }
    }
}