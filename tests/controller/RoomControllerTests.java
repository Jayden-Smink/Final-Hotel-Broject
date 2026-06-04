package controller;

import model.Area;
import model.SimulationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoomControllerTests {

    private RoomController controller;
    private SimulationData data;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Area makeRoom(int id, String pos, int capacity) {
        Area a = new Area();
        a.id = id;
        a.AreaType = "ROOM";
        a.Position = pos;
        a.Dimension = "2,1";
        a.capacity = capacity;
        return a;
    }

    private Area makeArea(int id, String type, String pos, int capacity) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.capacity = capacity;
        return a;
    }

    @BeforeEach
    void setUp() {
        controller = new RoomController();
        List<Area> areas = new ArrayList<>();
        // Lobby required by SimulationData constructor
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        data = new SimulationData(areas, 10, 30);
    }

    // ── reserveerVrijeKamer ───────────────────────────────────────────────────

    @Test
    void reserveer_assignsPreferredRoomWhenFree() {
        data.areas.add(makeRoom(10, "1,2", 2));

        Optional<Area> result = controller.reserveerVrijeKamer(data, 10, 99);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().id);
        assertTrue(result.get().currentOccupants.contains(99));
    }

    @Test
    void reserveer_fallsBackToAnyFreeRoomWhenPreferredIsFull() {
        Area preferred = makeRoom(10, "1,2", 1);
        preferred.currentOccupants.add(1); // full
        Area other = makeRoom(11, "2,2", 1);
        data.areas.add(preferred);
        data.areas.add(other);

        Optional<Area> result = controller.reserveerVrijeKamer(data, 10, 99);

        assertTrue(result.isPresent());
        assertEquals(11, result.get().id);
        assertTrue(result.get().currentOccupants.contains(99));
    }

    @Test
    void reserveer_returnsEmptyWhenAllRoomsFull() {
        Area room = makeRoom(10, "1,2", 1);
        room.currentOccupants.add(1); // full
        data.areas.add(room);

        Optional<Area> result = controller.reserveerVrijeKamer(data, 10, 99);

        assertFalse(result.isPresent());
    }

    @Test
    void reserveer_returnsEmptyWhenNoRoomsExist() {
        // data only has LOBBY
        Optional<Area> result = controller.reserveerVrijeKamer(data, 99, 42);
        assertFalse(result.isPresent());
    }

    @Test
    void reserveer_doesNotAssignNonRoomAreaAsRoom() {
        data.areas.add(makeArea(20, "RESTAURANT", "1,2", 10));

        Optional<Area> result = controller.reserveerVrijeKamer(data, 20, 42);

        assertFalse(result.isPresent());
    }

    // ── maakGastVrij ──────────────────────────────────────────────────────────

    @Test
    void maakGastVrij_removesGuestFromAllAreas() {
        Area r1 = makeRoom(10, "1,2", 5);
        Area r2 = makeRoom(11, "2,2", 5);
        r1.currentOccupants.add(7);
        r2.currentOccupants.add(7);
        r2.currentOccupants.add(8);
        data.areas.add(r1);
        data.areas.add(r2);

        controller.maakGastVrij(data, 7);

        assertFalse(r1.currentOccupants.contains(7));
        assertFalse(r2.currentOccupants.contains(7));
        assertTrue(r2.currentOccupants.contains(8)); // other guest untouched
    }

    @Test
    void maakGastVrij_doesNothingWhenGuestNotPresent() {
        Area r = makeRoom(10, "1,2", 5);
        data.areas.add(r);

        assertDoesNotThrow(() -> controller.maakGastVrij(data, 999));
    }

    // ── vindVrijeActiviteit ───────────────────────────────────────────────────

    @Test
    void vindVrijeActiviteit_findsFreeActivityArea() {
        data.areas.add(makeArea(20, "CINEMA", "1,3", 5));

        Optional<Area> result = controller.vindVrijeActiviteit(data, "CINEMA", 1);

        assertTrue(result.isPresent());
        assertEquals("CINEMA", result.get().AreaType);
    }

    @Test
    void vindVrijeActiviteit_returnsEmptyWhenAreaFull() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 1);
        cinema.currentOccupants.add(5);
        data.areas.add(cinema);

        Optional<Area> result = controller.vindVrijeActiviteit(data, "CINEMA", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void vindVrijeActiviteit_isCaseInsensitive() {
        data.areas.add(makeArea(20, "FITNESS", "1,3", 5));

        Optional<Area> result = controller.vindVrijeActiviteit(data, "fitness", 1);

        assertTrue(result.isPresent());
    }

    @Test
    void vindVrijeActiviteit_freesGuestFromPreviousAreaFirst() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 5);
        cinema.currentOccupants.add(7);
        data.areas.add(cinema);
        data.areas.add(makeArea(21, "FITNESS", "1,4", 5));

        controller.vindVrijeActiviteit(data, "FITNESS", 7);

        assertFalse(cinema.currentOccupants.contains(7));
    }
}
