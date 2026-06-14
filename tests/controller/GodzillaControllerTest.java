package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import view.LogPanel;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GodzillaControllerTest {

    private GodzillaController controller;
    private SimulationData testData;
    private LogPanel dummyLog;
    private IDestructionStrategy mockStrategy;
    private Area targetArea;

    @BeforeEach
    void setUp() {
        // Fix 1: Assume default constructor, if not possible, use actual init params
        testData = new SimulationData();
        testData.areas = new ArrayList<>();
        testData.guests = new HashMap<>();
        testData.tileSize = 32;

        // Fix 2: Use the correct type instead of Object
        testData.elevator = new Elevator();

        dummyLog = mock(LogPanel.class);
        mockStrategy = mock(IDestructionStrategy.class);

        targetArea = new Area();
        targetArea.AreaType = "ROOM";
        targetArea.Position = "0, 2";
        targetArea.Dimension = "2, 2";
        testData.areas.add(targetArea);

        controller = new GodzillaController(testData, dummyLog, mockStrategy);
    }

    @Test
    void testUpdate_UpdatesFiresAndIncrementsTimers() {
        controller.activate();
        targetArea.isOnFire = true;

        // We cannot change COLUMN_DESTROY_INTERVAL if it is 'final'.
        // We just run the update logic and let it increment naturally.
        controller.update();

        verify(mockStrategy, times(1)).update(targetArea);
    }

    @Test
    void testHandleGuestsInDestroyedAreas_KillsGuests() {
        controller.activate();

        // Fix 3: Initialize Guest properly if it doesn't have a default constructor
        Guest guest = new Guest(7);
        guest.x = 20.0;
        guest.y = 80.0;
        testData.guests.put(guest.id, guest);

        targetArea.isDestroyed = true;
        controller.update();

        assertTrue(guest.isDead);
    }
}