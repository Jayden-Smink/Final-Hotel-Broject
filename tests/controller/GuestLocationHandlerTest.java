package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestLocationHandlerTest {

    private SimulationData data;
    private GuestLocationHandler handler;
    private ReceptionistController receptionistController;

    private Area makeArea(int id, String type, String pos, int cap) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = cap;
        return a;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        areas.add(makeArea(1, "RECEPTION", "2,5", 10));
        areas.add(makeArea(10, "ROOM", "1,2", 5));
        areas.add(makeArea(20, "RESTAURANT", "3,3", 10));
        data = new SimulationData(areas, 4, 30, 30, 10, 15, 60);

        receptionistController = new ReceptionistController(data, null);
        GuestNavigator navigator = new GuestNavigator(data, null);
        handler = new GuestLocationHandler(data, receptionistController, navigator, null);
    }

    private Guest makeGuestAtReception() {
        // Receptie staat op tile-x=2, tile-y=5
        // Midden = 2*60 + 60 = 180, y = 5*60 = 300
        Guest g = new Guest(1, 180, 300);
        g.state = GuestState.AT_DESTINATION;
        g.currentActivity = "WALKING_TO_RECEPTION";
        g.assignedRoomId = -1;
        g.isCheckingOut = false;
        data.guests.put(1, g);
        return g;
    }

    // ── processLocationLogic — receptie-aankomst ──────────────────────────────

    @Test
    void processLocationLogic_atReception_assignedRoom_activityChangesToWalkingToRoom() {
        // Reserveer een kamer zodat de receptionist er een kan toewijzen
        data.areas.stream().filter(a -> a.id == 10).findFirst()
                .ifPresent(r -> r.currentOccupants.clear()); // kamer vrij maken

        // Check-in zodat gast een kamer krijgt
        Guest g = makeGuestAtReception();
        receptionistController.handleCheckIn(1, 10);
        // Reset state zodat de handler hem op AT_DESTINATION behandelt
        g.state = GuestState.AT_DESTINATION;

        handler.processLocationLogic();

        assertEquals("WALKING_TO_ROOM", g.currentActivity,
                "Na receptie-aankomst met toegewezen kamer moet activiteit WALKING_TO_ROOM zijn");
    }

    @Test
    void processLocationLogic_atReception_noRoomAvailable_guestIsCheckingOut() {
        // Vul de kamer zodat er geen ruimte is
        data.areas.stream().filter(a -> a.id == 10).findFirst()
                .ifPresent(r -> { for (int i = 1; i <= 5; i++) r.currentOccupants.add(i); });

        Guest g = makeGuestAtReception();

        handler.processLocationLogic();

        assertTrue(g.isCheckingOut,
                "Gast zonder beschikbare kamer moet isCheckingOut=true krijgen");
    }

    // ── processLocationLogic — aankomst bij faciliteit ────────────────────────

    @Test
    void processLocationLogic_arrivedAtFacility_stateBecomesIdle() {
        Guest g = new Guest(2, 200, 200);
        g.state = GuestState.AT_DESTINATION;
        g.currentActivity = "WALKING_TO_FACILITY";
        g.isCheckingOut = false;
        data.guests.put(2, g);

        handler.processLocationLogic();

        assertEquals(GuestState.IDLE, g.state,
                "Gast die faciliteit bereikt moet IDLE worden");
        assertEquals("USING_FACILITY", g.currentActivity);
        assertEquals(0, g.activityTimer, "Timer moet resetten");
        assertFalse(g.isInRoom);
    }

    // ── processLocationLogic — aankomst bij kamer ─────────────────────────────

    @Test
    void processLocationLogic_arrivedAtAssignedRoom_stateBecomesIdle() {
        Guest g = new Guest(3, 0, 0);
        // Kamer 10 staat op pos 1,2 → target = (1*60) + (2*60/2) = 120, 2*60 + 30 = 150
        double roomTargetX = 120.0;
        double roomTargetY = 150.0;
        g.x = roomTargetX;
        g.y = roomTargetY;
        g.targetX = roomTargetX;
        g.targetY = roomTargetY;
        g.state = GuestState.AT_DESTINATION;
        g.assignedRoomId = 10;
        g.currentActivity = "WALKING_TO_ROOM";
        g.isCheckingOut = false;
        data.guests.put(3, g);

        handler.processLocationLogic();

        assertEquals(GuestState.IDLE, g.state,
                "Gast die zijn kamer bereikt moet IDLE worden");
        assertTrue(g.isInRoom);
        assertEquals("ROOM", g.currentActivity);
        assertEquals(0, g.activityTimer);
    }

    // ── processLocationLogic — uitchecken bij uitgang ─────────────────────────

    @Test
    void processLocationLogic_checkingOutAtExit_removesGuestFromSimulation() {
        // Kamer 10 toevoegen aan de gast en aan een cleaner zodat er ook een schoonmaker is
        Cleaner cleaner = new Cleaner(1, 60, 300);
        data.cleaners.put(1, cleaner);

        Guest g = new Guest(5, 20.0, 5 * data.tileSize + 30.0);
        g.state = GuestState.AT_DESTINATION;
        g.isCheckingOut = true;
        g.assignedRoomId = 10;
        data.guests.put(5, g);

        handler.processLocationLogic();

        assertFalse(data.guests.containsKey(5), "Uitgecheckte gast moet verwijderd worden uit data.guests");
    }

    @Test
    void processLocationLogic_checkingOutAtExit_assignsRoomToCleaner() {
        Cleaner cleaner = new Cleaner(1, 60, 300);
        data.cleaners.put(1, cleaner);

        Guest g = new Guest(5, 20.0, 5 * data.tileSize + 30.0);
        g.state = GuestState.AT_DESTINATION;
        g.isCheckingOut = true;
        g.assignedRoomId = 10;
        data.guests.put(5, g);

        handler.processLocationLogic();

        assertTrue(cleaner.dirtyRooms.contains(10),
                "Na uitchecken moet de kamer aan de schoonmaker worden toewijderd");
    }

    // ── processLocationLogic — lege lijst ────────────────────────────────────

    @Test
    void processLocationLogic_noGuests_doesNotThrow() {
        data.guests.clear();
        assertDoesNotThrow(() -> handler.processLocationLogic());
    }

    // ── processLocationLogic — walking gast wordt genegeerd ──────────────────

    @Test
    void processLocationLogic_walkingGuest_notProcessed() {
        Guest g = new Guest(6, 100, 100);
        g.state = GuestState.WALKING;
        g.currentActivity = "WALKING_TO_ROOM";
        data.guests.put(6, g);

        handler.processLocationLogic();

        // State mag niet veranderen voor een lopende gast
        assertEquals(GuestState.WALKING, g.state);
    }
}
