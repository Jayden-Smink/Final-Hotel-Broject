package view;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomOverviewPanelTest {

    private SimulationData data;

    @BeforeEach
    void setUp() {
        List<Area> areas = Arrays.asList(makeLobby(), makeRoom(1), makeRoom(2));
        data = new SimulationData(areas, 4, 1, 60, 60, 60, 60);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new RoomOverviewPanel(data));
    }

    @Test
    void constructor_withNoRooms_doesNotThrow() {
        List<Area> noRooms = Arrays.asList(makeLobby());
        SimulationData d = new SimulationData(noRooms, 4, 1, 60, 60, 60, 60);
        assertDoesNotThrow(() -> new RoomOverviewPanel(d));
    }

    @Test
    void constructor_withEmptyAreasList_doesNotThrow() {
        List<Area> mutableAreas = new java.util.ArrayList<>(Arrays.asList(makeLobby()));
        SimulationData d = new SimulationData(mutableAreas, 4, 1, 60, 60, 60, 60);
        d.areas.clear();
        assertDoesNotThrow(() -> new RoomOverviewPanel(d));
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    void refresh_doesNotThrowWithNoGuests() {
        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    @Test
    void refresh_doesNotThrowWithGuestInRoom() {
        Guest guest = new Guest(1, 60, 60);
        data.guests.put(1, guest);
        data.areas.get(1).currentOccupants.add(1); // Room id=1

        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    @Test
    void refresh_doesNotThrowWhenOccupantGuestIsMissing() {
        // Occupant id that has no matching guest in data.guests
        data.areas.get(1).currentOccupants.add(99);

        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    @Test
    void refresh_doesNotThrowWithRoomClassification() {
        data.areas.get(1).classification = "3-ster";
        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    @Test
    void refresh_doesNotThrowWithNullClassification() {
        data.areas.get(1).classification = null;
        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    @Test
    void refresh_canBeCalledMultipleTimes() {
        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) panel.refresh();
        });
    }

    @Test
    void refresh_afterGuestCheckout_doesNotThrow() {
        Guest guest = new Guest(1, 60, 60);
        guest.isCheckingOut = true;
        data.guests.put(1, guest);
        data.areas.get(1).currentOccupants.add(1);

        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertDoesNotThrow(panel::refresh);
    }

    // ── panel properties ──────────────────────────────────────────────────────

    @Test
    void panel_preferredSizeIsSet() {
        RoomOverviewPanel panel = new RoomOverviewPanel(data);
        assertNotNull(panel.getPreferredSize());
        assertTrue(panel.getPreferredSize().width > 0);
        assertTrue(panel.getPreferredSize().height > 0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Area makeLobby() {
        Area a = new Area();
        a.id = 0;
        a.AreaType = "LOBBY";
        a.Position = "0, 5";
        a.Dimension = "3, 1";
        return a;
    }

    private Area makeRoom(int id) {
        Area a = new Area();
        a.id = id;
        a.AreaType = "ROOM";
        a.Position = id + ", 1";
        a.Dimension = "1, 1";
        return a;
    }
}