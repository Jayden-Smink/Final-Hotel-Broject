package controller;

import model.Area;
import model.Guest;
import model.GuestState;
import model.SimulationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.LogPanel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestSpawnerTest {

    private SimulationData realData;
    private LogPanel stubLogPanel;
    private GuestSpawner spawner;
    private List<String> capturedLogs;

    @BeforeEach
    void setUp() {
        capturedLogs = new ArrayList<>();
        stubLogPanel = new LogPanel() {
            @Override
            public void addLog(String message) {
                capturedLogs.add(message);
            }
        };

        // Create configuration layout matching exact array checks inside findAreaByType
        List<Area> areas = new ArrayList<>();

        Area lobby = new Area();
        lobby.AreaType = "LOBBY";
        lobby.Position = "0,2"; // y-index is 2
        lobby.Dimension = "2,1";
        areas.add(lobby);

        Area reception = new Area();
        reception.AreaType = "RECEPTION";
        reception.Position = "4,0"; // x-index is 4
        reception.Dimension = "2,1"; // width is 2
        areas.add(reception);

        // Instantiating with a standard tileSize of 32 for clean math validation
        realData = new SimulationData(areas, 5, 10, 10, 10, 10, 10);
        realData.tileSize = 32;

        spawner = new GuestSpawner(realData, stubLogPanel);
    }

    @Test
    void testSpawn_Successful() {
        Guest guest = new Guest(7, 0.0, 0.0);

        boolean isSpawned = spawner.spawn(guest);

        assertTrue(isSpawned);

        // Math Verification:
        // Expected Y = (lobbyY_index * tileSize) + (tileSize / 2.0) = (2 * 32) + 16.0 = 80.0
        assertEquals(20.0, guest.x, "Initial x coordinate should be anchored to 20.0");
        assertEquals(80.0, guest.y, "Initial y coordinate must match calculated center of the lobby tile");

        // Flag checks:
        assertEquals(GuestState.WALKING, guest.state);
        assertFalse(guest.isInRoom);
        assertFalse(guest.isCheckingOut);
        assertEquals("WALKING_TO_RECEPTION", guest.currentActivity);
        assertEquals(0, guest.activityTimer);

        // Verification of data structures map tracking
        assertTrue(realData.guests.containsKey(7));
        assertEquals(guest, realData.guests.get(7));

        // Verify logs caught the transaction text
        assertTrue(capturedLogs.get(0).contains("Gast 7 is ingecheckt."));
    }

    @Test
    void testSpawn_ReturnsFalseOnNullGuest() {
        boolean isSpawned = spawner.spawn(null);
        assertFalse(isSpawned);
    }

    @Test
    void testSpawn_ReturnsFalseWhenInfrastructureLayoutIsMissing() {
        // Clear layout tracking out to force failures
        realData.areas = null;
        Guest guest = new Guest(12, 0.0, 0.0);

        boolean isSpawned = spawner.spawn(guest);

        assertFalse(isSpawned, "Spawner must reject operations if functional infrastructure maps are empty");
    }
}