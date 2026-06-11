package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoomControllerTest {

    private RoomController controller;
    private SimulationData data;

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
        controller = new RoomController();
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        data = new SimulationData(areas, 10, 30, 30, 10, 15, 60);
    }

    // ── reserveerVrijeKamer ───────────────────────────────────────────────────

    @Test
    void reserveer_assignsPreferredRoom() {
        data.areas.add(makeArea(10, "ROOM", "1,2", 2));
        Optional<Area> result = controller.reserveerVrijeKamer(data, 10, 42);
        assertTrue(result.isPresent());
        assertEquals(10, result.get().id);
    }

    @Test
    void reserveer_addsGuestToPreferredRoom() {
        data.areas.add(makeArea(10, "ROOM", "1,2", 2));
        controller.reserveerVrijeKamer(data, 10, 42);
        assertTrue(data.areas.stream()
                .filter(a -> a.id == 10)
                .findFirst().get()
                .currentOccupants.contains(42));
    }

    @Test
    void reserveer_fallsBackToFreeRoomWhenPreferredFull() {
        Area pref = makeArea(10, "ROOM", "1,2", 1);
        pref.currentOccupants.add(1);
        Area other = makeArea(11, "ROOM", "2,2", 1);
        data.areas.add(pref);
        data.areas.add(other);
        Optional<Area> result = controller.reserveerVrijeKamer(data, 10, 42);
        assertTrue(result.isPresent());
        assertEquals(11, result.get().id);
    }

    @Test
    void reserveer_returnsEmptyWhenAllRoomsFull() {
        Area r = makeArea(10, "ROOM", "1,2", 1);
        r.currentOccupants.add(1);
        data.areas.add(r);
        assertFalse(controller.reserveerVrijeKamer(data, 10, 42).isPresent());
    }

    @Test
    void reserveer_returnsEmptyWhenNoRoomsExist() {
        assertFalse(controller.reserveerVrijeKamer(data, 99, 1).isPresent());
    }

    @Test
    void reserveer_doesNotAssignNonRoomTypes() {
        data.areas.add(makeArea(20, "CINEMA", "1,2", 10));
        assertFalse(controller.reserveerVrijeKamer(data, 20, 1).isPresent());
    }

    @Test
    void reserveer_preferredRoomTypCheckIsCaseInsensitive() {
        Area r = makeArea(10, "room", "1,2", 5); // lowercase
        data.areas.add(r);
        assertTrue(controller.reserveerVrijeKamer(data, 10, 1).isPresent());
    }

    @Test
    void reserveer_multipleGuestsInSameRoom() {
        data.areas.add(makeArea(10, "ROOM", "1,2", 3));
        controller.reserveerVrijeKamer(data, 10, 1);
        controller.reserveerVrijeKamer(data, 10, 2);
        controller.reserveerVrijeKamer(data, 10, 3);
        Area r = data.areas.stream().filter(a -> a.id == 10).findFirst().get();
        assertTrue(r.isFull());
    }

    // ── maakGastVrij ──────────────────────────────────────────────────────────

    @Test
    void maakGastVrij_removesGuestFromAllAreas() {
        Area r1 = makeArea(10, "ROOM", "1,2", 5);
        Area r2 = makeArea(11, "ROOM", "2,2", 5);
        r1.currentOccupants.add(7);
        r2.currentOccupants.add(7);
        data.areas.add(r1);
        data.areas.add(r2);
        controller.maakGastVrij(data, 7);
        assertFalse(r1.currentOccupants.contains(7));
        assertFalse(r2.currentOccupants.contains(7));
    }

    @Test
    void maakGastVrij_leavesOtherGuestsIntact() {
        Area r = makeArea(10, "ROOM", "1,2", 5);
        r.currentOccupants.add(7);
        r.currentOccupants.add(8);
        data.areas.add(r);
        controller.maakGastVrij(data, 7);
        assertTrue(r.currentOccupants.contains(8));
    }

    @Test
    void maakGastVrij_noExceptionForAbsentGuest() {
        assertDoesNotThrow(() -> controller.maakGastVrij(data, 999));
    }

    // ── vindVrijeActiviteit ───────────────────────────────────────────────────

    @Test
    void vindVrijeActiviteit_findsFreeActivityArea() {
        data.areas.add(makeArea(20, "CINEMA", "1,3", 5));
        Optional<Area> r = controller.vindVrijeActiviteit(data, "CINEMA", 1);
        assertTrue(r.isPresent());
    }

    @Test
    void vindVrijeActiviteit_returnsEmptyWhenFull() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 1);
        cinema.currentOccupants.add(5);
        data.areas.add(cinema);
        assertFalse(controller.vindVrijeActiviteit(data, "CINEMA", 1).isPresent());
    }

    @Test
    void vindVrijeActiviteit_isCaseInsensitive() {
        data.areas.add(makeArea(20, "FITNESS", "1,3", 5));
        assertTrue(controller.vindVrijeActiviteit(data, "fitness", 1).isPresent());
    }

    @Test
    void vindVrijeActiviteit_removesGuestFromPreviousArea() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 5);
        cinema.currentOccupants.add(7);
        data.areas.add(cinema);
        data.areas.add(makeArea(21, "FITNESS", "1,4", 5));
        controller.vindVrijeActiviteit(data, "FITNESS", 7);
        assertFalse(cinema.currentOccupants.contains(7));
    }

    @Test
    void vindVrijeActiviteit_returnsEmptyForUnknownType() {
        assertFalse(controller.vindVrijeActiviteit(data, "SPA", 1).isPresent());
    }
}
