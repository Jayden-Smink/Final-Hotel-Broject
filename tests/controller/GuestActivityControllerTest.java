package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestActivityControllerTest {

    private SimulationData data;
    private GuestActivityController controller;

    private Area makeArea(int id, String type, String pos, int cap) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = cap;
        return a;
    }

    private Guest makeIdleGuest(int id) {
        Guest g = new Guest(id, 0, 0);
        g.state = GuestState.IDLE;
        g.assignedRoomId = 10;
        g.isCheckingOut = false;
        g.currentActivity = "ROOM";
        g.activityTimer = 0;
        return g;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        areas.add(makeArea(1, "RECEPTION", "2,5", 10));
        areas.add(makeArea(10, "ROOM", "1,2", 5));
        areas.add(makeArea(20, "RESTAURANT", "3,3", 10));
        areas.add(makeArea(21, "CINEMA", "5,3", 10));
        areas.add(makeArea(22, "FITNESS", "7,3", 10));
        data = new SimulationData(areas, 4, 30, 30, 10, 15);
        ReceptionistController receptionistController = new ReceptionistController(data, null);
        controller = new GuestActivityController(data, receptionistController, null);
    }

    // ── updateActivities — uitchecken ─────────────────────────────────────────

    @Test
    void updateActivities_checkingOutGuest_getsDirectedToExit() {
        Guest g = makeIdleGuest(1);
        g.isCheckingOut = true;
        data.guests.put(1, g);

        controller.updateActivities();

        // Uitgang = x=20, ergens op lobby-rij
        assertEquals(20.0, g.targetX, 0.001,
                "Uitchecken guest moet naar de uitgang (x=20) worden gestuurd");
    }

    @Test
    void updateActivities_checkingOutGuest_stateBecomesWalking() {
        Guest g = makeIdleGuest(1);
        g.isCheckingOut = true;
        g.state = GuestState.IDLE;
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals(GuestState.WALKING, g.state,
                "Uitchecken gast moet in WALKING state komen");
    }

    // ── updateActivities — activiteitstimer ───────────────────────────────────

    @Test
    void updateActivities_idleInRoom_timerIncrementsEachCall() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "ROOM";
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals(1, g.activityTimer, "Timer moet met 1 worden opgehoogd elke update");
    }

    @Test
    void updateActivities_timerNotExpired_activityUnchanged() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "ROOM";
        g.activityTimer = 0;
        data.guests.put(1, g);

        controller.updateActivities(); // timer = 1, nog niet op 300

        assertEquals("ROOM", g.currentActivity, "Activiteit mag niet wijzigen voordat timer afloopt");
    }

    @Test
    void updateActivities_timerExpired_inRoom_sendsToFacility() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "ROOM";
        g.activityTimer = 299; // één stap voor de drempel van 300
        data.guests.put(1, g);

        controller.updateActivities(); // timer = 300 → expired

        // Na expiratie moet de activiteit veranderen naar richting faciliteit
        assertEquals("WALKING_TO_FACILITY", g.currentActivity,
                "Na 300 frames in ROOM moet gast naar een faciliteit gaan");
        assertEquals(0, g.activityTimer, "Timer moet resetten na activiteitswissel");
    }

    @Test
    void updateActivities_timerExpired_usingFacility_returnsToRoom() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "USING_FACILITY";
        g.currentFacility = "RESTAURANT";
        // Gebruik de restaurant-duratie
        g.activityTimer = data.facilitySettings.getRestaurantDurationFrames() - 1;
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals("WALKING_TO_ROOM", g.currentActivity,
                "Na restaurantbezoek moet gast terug naar kamer lopen");
        assertEquals(0, g.activityTimer, "Timer moet resetten");
    }

    @Test
    void updateActivities_cinemaTimer_usesCorrectDuration() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "USING_FACILITY";
        g.currentFacility = "CINEMA";
        int cinemaDuration = data.facilitySettings.getCinemaDurationFrames();
        g.activityTimer = cinemaDuration - 1;
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals("WALKING_TO_ROOM", g.currentActivity,
                "Na cinemabezoek moet gast terug naar kamer lopen");
    }

    @Test
    void updateActivities_fitnessTimer_usesCorrectDuration() {
        Guest g = makeIdleGuest(1);
        g.currentActivity = "USING_FACILITY";
        g.currentFacility = "FITNESS";
        int fitnessDuration = data.facilitySettings.getFitnessDurationFrames();
        g.activityTimer = fitnessDuration - 1;
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals("WALKING_TO_ROOM", g.currentActivity,
                "Na fitnessbezoek moet gast terug naar kamer lopen");
    }

    // ── updateActivities — niet-IDLE gasten ───────────────────────────────────

    @Test
    void updateActivities_walkingGuest_timerDoesNotIncrement() {
        Guest g = makeIdleGuest(1);
        g.state = GuestState.WALKING;
        g.activityTimer = 0;
        data.guests.put(1, g);

        controller.updateActivities();

        assertEquals(0, g.activityTimer, "Timer mag alleen lopen als de gast IDLE is");
    }

    // ── updateActivities — lege gastenlijst ───────────────────────────────────

    @Test
    void updateActivities_noGuests_doesNotThrow() {
        data.guests.clear();
        assertDoesNotThrow(() -> controller.updateActivities());
    }
}
