package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReceptionistTest {

    private SimulationData data;
    private Receptionist receptionist;

    private Area makeArea(int id, String type, String pos, int cap) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.capacity = cap;
        return a;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        data = new SimulationData(areas, 10, 30);
        receptionist = new Receptionist(data, new RoomController());
    }

    // ── wijsKamerToe ──────────────────────────────────────────────────────────

    @Test
    void wijsKamerToe_returnsRoomWhenAvailable() {
        data.areas.add(makeArea(10, "ROOM", "1,2", 2));
        Optional<Area> result = receptionist.wijsKamerToe(10, 42);
        assertTrue(result.isPresent());
    }

    @Test
    void wijsKamerToe_returnsEmptyWhenNoRooms() {
        assertFalse(receptionist.wijsKamerToe(99, 1).isPresent());
    }

    @Test
    void wijsKamerToe_registersGuestInRoom() {
        Area r = makeArea(10, "ROOM", "1,2", 2);
        data.areas.add(r);
        receptionist.wijsKamerToe(10, 55);
        assertTrue(r.currentOccupants.contains(55));
    }

    // ── checkOut ──────────────────────────────────────────────────────────────

    @Test
    void checkOut_removesGuestFromRoom() {
        Area r = makeArea(10, "ROOM", "1,2", 5);
        r.currentOccupants.add(7);
        data.areas.add(r);
        receptionist.checkOut(7);
        assertFalse(r.currentOccupants.contains(7));
    }

    @Test
    void checkOut_noExceptionForUnknownGuest() {
        assertDoesNotThrow(() -> receptionist.checkOut(999));
    }

    @Test
    void checkOut_doesNotAffectOtherGuests() {
        Area r = makeArea(10, "ROOM", "1,2", 5);
        r.currentOccupants.add(7);
        r.currentOccupants.add(8);
        data.areas.add(r);
        receptionist.checkOut(7);
        assertTrue(r.currentOccupants.contains(8));
    }
}
