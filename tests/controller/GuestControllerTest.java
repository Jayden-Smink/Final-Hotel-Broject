package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestControllerTest {

    private SimulationData data;
    private GuestController controller;
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
        data = new SimulationData(areas, 4, 30, 30, 10, 15, 60);
        receptionistController = new ReceptionistController(data, null);
        controller = new GuestController(data, null, receptionistController);
    }

    // ── processCheckIn ────────────────────────────────────────────────────────

    @Test
    void processCheckIn_validGuest_addsGuestToSimulation() {
        controller.processCheckIn(1, 10);
        assertTrue(data.guests.containsKey(1), "Gast moet in data.guests zitten na check-in");
    }

    @Test
    void processCheckIn_invalidGuestId_zero_doesNotAdd() {
        controller.processCheckIn(0, 10);
        assertFalse(data.guests.containsKey(0));
    }

    @Test
    void processCheckIn_invalidGuestId_negative_doesNotAdd() {
        controller.processCheckIn(-5, 10);
        assertTrue(data.guests.isEmpty(), "Negatieve guestId mag niet worden toegevoegd");
    }

    @Test
    void processCheckIn_duplicateGuestId_ignoresSecondCheckin() {
        controller.processCheckIn(1, 10);
        int sizeAfterFirst = data.guests.size();
        controller.processCheckIn(1, 10);
        assertEquals(sizeAfterFirst, data.guests.size(), "Dubbele check-in mag geen extra gast aanmaken");
    }

    @Test
    void processCheckIn_noLobby_doesNotSpawnGuest() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("LOBBY"));
        controller.processCheckIn(1, 10);
        assertFalse(data.guests.containsKey(1), "Zonder lobby mag gast niet gespawned worden");
    }

    @Test
    void processCheckIn_noReception_doesNotSpawnGuest() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("RECEPTION"));
        controller.processCheckIn(1, 10);
        assertFalse(data.guests.containsKey(1), "Zonder receptie mag gast niet gespawned worden");
    }

    @Test
    void processCheckIn_guestSpawnedAtLobbyY() {
        controller.processCheckIn(1, 10);
        Guest g = data.guests.get(1);
        assertNotNull(g);
        assertEquals(5 * data.tileSize + data.tileSize / 2.0, g.y, 0.001);
    }

    @Test
    void processCheckIn_guestInitialStateIsWalking() {
        controller.processCheckIn(1, 10);
        Guest g = data.guests.get(1);
        assertNotNull(g);
        assertEquals(GuestState.WALKING, g.state);
    }

    // ── spawnGuest ────────────────────────────────────────────────────────────

    @Test
    void spawnGuest_nullGuest_returnsFalse() {
        assertFalse(controller.spawnGuest(null));
    }

    @Test
    void spawnGuest_noLobby_returnsFalse() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("LOBBY"));
        Guest g = new Guest(99, 0, 0);
        assertFalse(controller.spawnGuest(g));
    }

    @Test
    void spawnGuest_noReception_returnsFalse() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("RECEPTION"));
        Guest g = new Guest(99, 0, 0);
        assertFalse(controller.spawnGuest(g));
    }

    @Test
    void spawnGuest_validSetup_returnsTrue() {
        Guest g = new Guest(99, 0, 0);
        assertTrue(controller.spawnGuest(g));
    }

    @Test
    void spawnGuest_addsGuestToDataGuests() {
        Guest g = new Guest(99, 0, 0);
        controller.spawnGuest(g);
        assertTrue(data.guests.containsKey(99));
    }

    @Test
    void spawnGuest_setsActivityToWalkingToReception() {
        Guest g = new Guest(99, 0, 0);
        controller.spawnGuest(g);
        assertEquals("WALKING_TO_RECEPTION", g.currentActivity);
    }

    @Test
    void spawnGuest_setsIsInRoomFalse() {
        Guest g = new Guest(99, 0, 0);
        controller.spawnGuest(g);
        assertFalse(g.isInRoom);
    }

    @Test
    void spawnGuest_setsIsCheckingOutFalse() {
        Guest g = new Guest(99, 0, 0);
        controller.spawnGuest(g);
        assertFalse(g.isCheckingOut);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_exitingLiftGuest_becomesWalking() {
        // Gast op verdieping 0, target op een andere X zodat GuestMover hem niet
        // meteen als AT_DESTINATION markeert na de state-wissel naar WALKING.
        Guest g = new Guest(1, 100, 30);
        g.targetX = 500; // ver weg zodat hij niet meteen aankomt
        g.targetY = 30;
        g.state = GuestState.EXITING_LIFT;
        data.guests.put(1, g);

        controller.update();

        assertEquals(GuestState.WALKING, g.state,
                "EXITING_LIFT moet overgaan naar WALKING in de update-loop");
    }

    @Test
    void update_noGuestsInSimulation_doesNotThrow() {
        data.guests.clear();
        assertDoesNotThrow(() -> controller.update());
    }

    @Test
    void update_idleGuestNotMoving_remainsIdle() {
        Guest g = new Guest(1, 100, 100);
        g.state = GuestState.IDLE;
        g.targetX = 100;
        g.targetY = 100;
        data.guests.put(1, g);

        controller.update();

        assertEquals(GuestState.IDLE, g.state, "IDLE gast die al op target staat mag niet van state wisselen");
    }
}