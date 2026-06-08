package controller;

import model.*;
import view.LogPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Orkestrator: roept per frame de locatie- en activiteitenlogica aan.
 * Heeft zelf geen kennis van navigatie of locatiedetectie (SRP).
 */
public class GuestActivityController {

    private final SimulationData data;
    private final GuestLocationHandler locationHandler;
    private final GuestNavigator navigator;
    private final LogPanel logPanel;

    public GuestActivityController(SimulationData data, ReceptionistController receptionistController, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
        this.navigator = new GuestNavigator(data, logPanel);
        this.locationHandler = new GuestLocationHandler(data, receptionistController, navigator, logPanel);
    }

    /**
     * Hoofd-update loop voor de gastenactiviteiten. Wordt elke frame aangeroepen.
     */
    public void updateActivities() {
        locationHandler.processLocationLogic();
        handleDynamicGuestActivities();
    }

    /**
     * Regelt de timers van stilstaande (IDLE) gasten en stuurt ze na verloop van tijd ergens anders heen.
     */
    private void handleDynamicGuestActivities() {
        List<Guest> snapshot = new ArrayList<>(data.guests.values());

        for (int i = 0; i < snapshot.size(); i++) {
            Guest guest = snapshot.get(i);

            if (guest.state == GuestState.IDLE) {

                if (guest.isCheckingOut) {
                    navigator.sendGuestToExit(guest);
                    continue;
                }

                guest.activityTimer++;

                int duration = getFacilityDuration(guest);
                if (guest.activityTimer >= duration) {

                    if (guest.currentActivity.equals("ROOM")) {
                        navigator.sendGuestToRandomFacility(guest);
                    } else if (guest.currentActivity.equals("USING_FACILITY")) {
                        navigator.returnGuestToRoom(guest);
                    }

                    guest.activityTimer = 0;
                }
            }
        }
    }

    private int getFacilityDuration(Guest guest) {
        if (guest.currentActivity.equals("USING_FACILITY")) {
            switch (guest.currentFacility) {
                case "CINEMA":     return data.facilitySettings.getCinemaDurationFrames();
                case "RESTAURANT": return data.facilitySettings.getRestaurantDurationFrames();
                case "FITNESS":    return data.facilitySettings.getFitnessDurationFrames();
                default:           return 300;
            }
        }
        return 300;
    }
}