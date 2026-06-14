package controller;

import model.Cleaner;
import model.CleanerState;
import model.SimulationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.LogPanel;

import java.util.HashMap;

import static org.mockito.Mockito.*;

class EmergencyHandlerTest {

    private EmergencyHandler handler;
    private SimulationData data;
    private CleanerAssigner mockAssigner;
    private LogPanel mockLog;

    @BeforeEach
    void setUp() {
        data = new SimulationData(null, 4, 1, 60, 60, 60, 60);
        data.cleaners = new HashMap<>(); // Adjust if this is a List/other type

        mockAssigner = mock(CleanerAssigner.class);
        mockLog = mock(LogPanel.class);

        handler = new EmergencyHandler(data, mockLog, mockAssigner);
    }

    @Test
    void testHandle_AssignsIdleCleanerWhenAvailable() {
        // FIX: Match the constructor of your Cleaner class here
        Cleaner idleCleaner = new Cleaner(1, 1, 1);
        idleCleaner.state = CleanerState.IDLE;
        data.cleaners.put(1, idleCleaner);

        when(mockAssigner.findFirstRoomId()).thenReturn(101);

        handler.handle(101);

        verify(mockAssigner).assignToRoom(idleCleaner, 101);
    }

    @Test
    void testHandle_QueuesRoomForBusyCleanerWhenNoIdleAvailable() {
        // FIX: Match the constructor of your Cleaner class here
        Cleaner busyCleaner = new Cleaner(1, 1, 1);
        busyCleaner.state = CleanerState.CLEANING;
        data.cleaners.put(1, busyCleaner);

        when(mockAssigner.findFirstRoomId()).thenReturn(202);

        handler.handle(202);

        // Verification
        assert(busyCleaner.dirtyRooms.contains(202));
        verify(mockAssigner, never()).assignToRoom(any(), anyInt());
    }
}