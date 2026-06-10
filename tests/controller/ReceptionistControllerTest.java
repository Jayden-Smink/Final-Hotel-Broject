package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReceptionistControllerTest {

    private SimulationData data;
    private ReceptionistController controller;

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
        data = new SimulationData(areas, 10, 30, 30, 10, 15);
        controller = new ReceptionistController(data, null); // null logPanel is fine
    }

    private Guest spawnGuest(int id) {
        Guest g = new Guest(id, 20.0, 5 * data.tileSize + 30.0);
        data.guests.put(id, g);
        return g;
    }

    // ── handleCheckIn ─────────────────────────────────────────────────────────

    @Test
    void handleCheckIn_assignsRoomToGuest() {
        Guest g = spawnGuest(1);
        controller.handleCheckIn(1, 10);
        assertEquals(10, g.assignedRoomId);
    }

    @Test
    void handleCheckIn_setsGuestStateToWalking() {
        Guest g = spawnGuest(1);
        g.state = GuestState.IDLE;
        controller.handleCheckIn(1, 10);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void handleCheckIn_noExceptionForUnknownGuest() {
        // Guest not in data.guests, room exists
        assertDoesNotThrow(() -> controller.handleCheckIn(999, 10));
    }

    @Test
    void handleCheckIn_doesNothingWhenNoRoomsAvailable() {
        // Room 10 is full
        data.areas.stream().filter(a -> a.id == 10).findFirst()
                .ifPresent(r -> { for (int i = 1; i <= 5; i++) r.currentOccupants.add(i); });
        Guest g = spawnGuest(1);
        int originalRoomId = g.assignedRoomId;
        controller.handleCheckIn(1, 10);
        assertEquals(originalRoomId, g.assignedRoomId); // unchanged
    }

    // ── sendToReception ───────────────────────────────────────────────────────

    @Test
    void sendToReception_setsTargetWithinReceptionBounds() {
        Guest g = spawnGuest(1);
        controller.sendToReception(g);
        // Reception at pos 2,5 dim 2,1 tile 60 → x between 120 and 240
        assertTrue(g.targetX >= 2 * data.tileSize && g.targetX <= 4 * data.tileSize,
                "targetX " + g.targetX + " should be within reception X range");
    }

    @Test
    void sendToReception_setsStateToWalking() {
        Guest g = spawnGuest(1);
        g.state = GuestState.IDLE;
        controller.sendToReception(g);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void sendToReception_doesNothingWhenNoReceptionArea() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("RECEPTION"));
        Guest g = spawnGuest(1);
        double originalTargetX = g.targetX;
        controller.sendToReception(g);
        assertEquals(originalTargetX, g.targetX, 0.001); // unchanged
    }

    // ── sendToRoom ────────────────────────────────────────────────────────────

    @Test
    void sendToRoom_setsTargetTowardAssignedRoom() {
        Guest g = spawnGuest(1);
        g.assignedRoomId = 10;
        controller.sendToRoom(g);
        // Room 10 at pos 1,2 dim 2,1 tile 60 → targetX = (1*60) + (2*60/2) = 120
        assertEquals(120.0, g.targetX, 0.001);
    }

    @Test
    void sendToRoom_doesNothingWhenAssignedRoomIdIsMinusOne() {
        Guest g = spawnGuest(1);
        g.assignedRoomId = -1;
        double originalTargetX = g.targetX;
        controller.sendToRoom(g);
        assertEquals(originalTargetX, g.targetX, 0.001);
    }

    @Test
    void sendToRoom_setsStateToWalking() {
        Guest g = spawnGuest(1);
        g.assignedRoomId = 10;
        g.state = GuestState.IDLE;
        controller.sendToRoom(g);
        assertEquals(GuestState.WALKING, g.state);
    }
}
