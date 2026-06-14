package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import view.LogPanel;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CleanerStateHandlerTest {

    private CleanerStateHandler handler;
    private SimulationData data;
    private CleanerAssigner mockAssigner;
    private LogPanel mockLog;
    private Cleaner worker;

    @BeforeEach
    void setUp() {
        data = new SimulationData(new ArrayList<>(), 4, 1, 60, 60, 60, 60);
        // <-- ADJUST: Ensure these exist or instantiate correctly for your model
        data.cleanerSettings = new CleanerSettings(10); // Example setting for cleaning duration

        mockAssigner = mock(CleanerAssigner.class);
        mockLog = mock(LogPanel.class);

        handler = new CleanerStateHandler(data, mockLog, mockAssigner);

        // Instantiate the worker
        worker = new Cleaner(1, 1, 1); // <-- ADJUST if Cleaner requires more arguments
        worker.dirtyRooms = new ArrayList<>();
    }

    @Test
    void testUpdate_IncrementsTimerWhenCleaning() {
        // Arrange
        worker.state = CleanerState.CLEANING;
        worker.cleaningTimer = 0;
        when(data.cleanerSettings.getCleaningDurationFrames()).thenReturn(10);

        // Act
        handler.update(worker);

        // Assert
        assertEquals(1, worker.cleaningTimer, "Timer should increment each update frame");
    }

    @Test
    void testUpdate_FinishCleaningAssignsNextRoom() {
        // Arrange
        worker.state = CleanerState.CLEANING;
        worker.cleaningTimer = 9; // One tick away from finishing
        when(data.cleanerSettings.getCleaningDurationFrames()).thenReturn(10);
        worker.dirtyRooms.add(101);

        // Act
        handler.update(worker);

        // Assert
        verify(mockAssigner).assignToRoom(worker, 101);
        assertEquals(0, worker.cleaningTimer, "Timer should reset to 0 after finished");
    }

    @Test
    void testUpdate_HandlesArrivalAtRoom() {
        // Arrange
        worker.state = CleanerState.WALKING_TO_ROOM;
        worker.x = 0; worker.y = 0;
        worker.targetX = 2; worker.targetY = 2; // Inside threshold (< 5)

        // Act
        handler.update(worker);

        // Assert
        assertEquals(CleanerState.CLEANING, worker.state, "Arrival at target should trigger CLEANING state");
    }

    @Test
    void testUpdate_ProcessIdleQueue() {
        // Arrange
        worker.state = CleanerState.IDLE;
        worker.dirtyRooms.add(505);

        // Act
        handler.update(worker);

        // Assert
        verify(mockAssigner).assignToRoom(worker, 505);
        assertTrue(worker.dirtyRooms.isEmpty(), "Room should be removed from queue after assignment");
    }
}