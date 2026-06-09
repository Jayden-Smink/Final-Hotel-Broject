package controller;

import model.*;
import view.LogPanel;

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
        for (Guest g : data.guests.values()) {

            if (g.state == GuestState.IDLE) {

                if (g.isCheckingOut) {
                    navigator.sendGuestToExit(g);
                    continue;
                }

                g.activityTimer++;

                int duration = getFacilityDuration(g);
                if (g.activityTimer >= duration) {

                    if (g.currentActivity.equals("ROOM")) {
                        navigator.sendGuestToRandomFacility(g);
                    } else if (g.currentActivity.equals("USING_FACILITY")) {
                        navigator.returnGuestToRoom(g);
                    }

                    g.activityTimer = 0;
                }
            }
        }
    }

    private int getFacilityDuration(Guest g) {
        if (g.currentActivity.equals("USING_FACILITY")) {
            switch (g.currentFacility) {
                case "CINEMA":   return data.facilitySettings.getCinemaDurationFrames();
                case "RESTAURANT": return data.facilitySettings.getRestaurantDurationFrames();
                case "FITNESS":  return data.facilitySettings.getFitnessDurationFrames();
                default:         return 300;
            }
        }
        return 300;
    }
}