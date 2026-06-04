package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationDataTest {

    private Area makeArea(int id, String type, String pos) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.capacity = 10;
        return a;
    }

    private SimulationData makeData() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        return new SimulationData(areas, 10, 30);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_elevatorIsNotNull() {
        assertNotNull(makeData().elevator);
    }

    @Test
    void constructor_guestsMapIsEmpty() {
        assertTrue(makeData().guests.isEmpty());
    }

    @Test
    void constructor_cleanerSettingsStoresSeconds() {
        SimulationData d = makeData();
        assertEquals(30, d.cleanerSettings.getCleaningDurationSeconds());
    }

    @Test
    void constructor_elevatorMaxCapacityMatchesArg() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        SimulationData d = new SimulationData(areas, 25, 30);
        assertEquals(25, d.elevator.maxCapacity);
    }

    @Test
    void constructor_elevatorStartsAtBottomFloor() {
        // Bottom floor = highest Y tile value in area list
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        areas.add(makeArea(1, "ROOM", "2,3"));
        SimulationData d = new SimulationData(areas, 10, 30);
        // bottomFloor = 5, elevator Y = 5 * 60 = 300
        assertEquals(5 * 60, d.elevator.curY, 0.001);
    }

    @Test
    void constructor_cleanerSpawnedWhenLobbyPresent() {
        assertNotNull(makeData().cleaner);
    }

    @Test
    void constructor_cleanerIsNullWhenNoLobby() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(1, "ROOM", "2,3"));
        SimulationData d = new SimulationData(areas, 10, 30);
        assertNull(d.cleaner);
    }

    @Test
    void constructor_floorQueueCreatedForEachUniqueFloorY() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        areas.add(makeArea(1, "ROOM", "2,3"));
        areas.add(makeArea(2, "ROOM", "4,3")); // same Y as above
        SimulationData d = new SimulationData(areas, 10, 30);
        // floorQueues should contain keys 5 and 3 (unique Y values)
        assertTrue(d.floorQueues.containsKey(5));
        assertTrue(d.floorQueues.containsKey(3));
    }

    @Test
    void constructor_defaultTileSizeIs60() {
        assertEquals(60, makeData().tileSize);
    }

    @Test
    void constructor_defaultHorizontalOffsetIs60() {
        assertEquals(60, makeData().horizontalOffset);
    }
}
