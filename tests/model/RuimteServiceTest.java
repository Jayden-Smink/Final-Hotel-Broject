package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RuimteServiceTest {

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
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        data = new SimulationData(areas, 10, 30, 30, 10, 15);
    }

    // ── reserveerVrijeKamer ───────────────────────────────────────────────────

    @Test
    void reserveer_assignsPreferredRoomWhenFree() {
        data.areas.add(makeArea(10, "ROOM", "1,2", 2));
        Optional<Area> result = RuimteService.reserveerVrijeKamer(data, 10, 42);
        assertTrue(result.isPresent());
        assertEquals(10, result.get().id);
        assertTrue(result.get().currentOccupants.contains(42));
    }

    @Test
    void reserveer_fallsBackWhenPreferredIsFull() {
        Area preferred = makeArea(10, "ROOM", "1,2", 1);
        preferred.currentOccupants.add(1);
        Area other = makeArea(11, "ROOM", "2,2", 1);
        data.areas.add(preferred);
        data.areas.add(other);
        Optional<Area> result = RuimteService.reserveerVrijeKamer(data, 10, 42);
        assertTrue(result.isPresent());
        assertEquals(11, result.get().id);
    }

    @Test
    void reserveer_returnsEmptyWhenAllRoomsFull() {
        Area r = makeArea(10, "ROOM", "1,2", 1);
        r.currentOccupants.add(1);
        data.areas.add(r);
        assertFalse(RuimteService.reserveerVrijeKamer(data, 10, 42).isPresent());
    }

    @Test
    void reserveer_doesNotPickNonRoomArea() {
        data.areas.add(makeArea(20, "CINEMA", "1,2", 10));
        assertFalse(RuimteService.reserveerVrijeKamer(data, 20, 42).isPresent());
    }

    @Test
    void reserveer_addsGuestToOccupants() {
        Area r = makeArea(10, "ROOM", "1,2", 5);
        data.areas.add(r);
        RuimteService.reserveerVrijeKamer(data, 10, 99);
        assertTrue(r.currentOccupants.contains(99));
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
        RuimteService.maakGastVrij(data, 7);
        assertFalse(r1.currentOccupants.contains(7));
        assertFalse(r2.currentOccupants.contains(7));
    }

    @Test
    void maakGastVrij_doesNotRemoveOtherGuests() {
        Area r = makeArea(10, "ROOM", "1,2", 5);
        r.currentOccupants.add(7);
        r.currentOccupants.add(8);
        data.areas.add(r);
        RuimteService.maakGastVrij(data, 7);
        assertTrue(r.currentOccupants.contains(8));
    }

    @Test
    void maakGastVrij_noExceptionWhenGuestNotPresent() {
        assertDoesNotThrow(() -> RuimteService.maakGastVrij(data, 999));
    }

    // ── vindVrijeActiviteit ───────────────────────────────────────────────────

    @Test
    void vindVrijeActiviteit_findsFreeArea() {
        data.areas.add(makeArea(20, "CINEMA", "1,3", 5));
        Optional<Area> result = RuimteService.vindVrijeActiviteit(data, "CINEMA", 1);
        assertTrue(result.isPresent());
    }

    @Test
    void vindVrijeActiviteit_returnsEmptyWhenFull() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 1);
        cinema.currentOccupants.add(5);
        data.areas.add(cinema);
        assertFalse(RuimteService.vindVrijeActiviteit(data, "CINEMA", 1).isPresent());
    }

    @Test
    void vindVrijeActiviteit_isCaseInsensitive() {
        data.areas.add(makeArea(20, "FITNESS", "1,3", 5));
        assertTrue(RuimteService.vindVrijeActiviteit(data, "fitness", 1).isPresent());
    }

    @Test
    void vindVrijeActiviteit_freesGuestFromOtherAreaFirst() {
        Area cinema = makeArea(20, "CINEMA", "1,3", 5);
        cinema.currentOccupants.add(7);
        data.areas.add(cinema);
        data.areas.add(makeArea(21, "FITNESS", "1,4", 5));
        RuimteService.vindVrijeActiviteit(data, "FITNESS", 7);
        assertFalse(cinema.currentOccupants.contains(7));
    }

    @Test
    void vindVrijeActiviteit_returnsEmptyWhenNoSuchType() {
        assertFalse(RuimteService.vindVrijeActiviteit(data, "SPA", 1).isPresent());
    }
}
