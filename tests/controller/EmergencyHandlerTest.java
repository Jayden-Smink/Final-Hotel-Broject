package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyHandlerTest {

    /**
     * Handmatige nep-implementatie van CleanerAssigner.
     * Zelfde patroon als in CleanerStateHandlerTest.
     */
    static class FakeCleanerAssigner extends CleanerAssigner {
        String lastCalledMethod = null;
        Cleaner lastCleaner = null;
        int lastRoomId = -1;
        int firstRoomIdToReturn = -1;

        FakeCleanerAssigner(int firstRoomIdToReturn) {
            super(null, null);
            this.firstRoomIdToReturn = firstRoomIdToReturn;
        }

        @Override
        public void assignToRoom(Cleaner cleaner, int roomId) {
            lastCalledMethod = "assignToRoom";
            lastCleaner = cleaner;
            lastRoomId = roomId;
        }

        @Override
        public void sendToLobby(Cleaner cleaner) {
            lastCalledMethod = "sendToLobby";
            lastCleaner = cleaner;
        }

        @Override
        public int findFirstRoomId() {
            return firstRoomIdToReturn;
        }
    }

    private EmergencyHandler handler;
    private SimulationData data;

    @BeforeEach
    void setUp() {
        ArrayList<Area> areas = new ArrayList<>();
        data = new SimulationData(areas, 4, 1, 60, 60, 60, 60);
        data.cleaners = new ConcurrentHashMap<>();
    }

    @Test
    void testHandle_AssignsIdleCleanerWhenAvailable() {
        Cleaner idleCleaner = new Cleaner(1, 1, 1);
        idleCleaner.state = CleanerState.IDLE;
        data.cleaners.put(1, idleCleaner);

        FakeCleanerAssigner fakeAssigner = new FakeCleanerAssigner(101);
        handler = new EmergencyHandler(data, null, fakeAssigner);

        handler.handle(101);

        assertEquals("assignToRoom", fakeAssigner.lastCalledMethod);
        assertEquals(idleCleaner, fakeAssigner.lastCleaner);
        assertEquals(101, fakeAssigner.lastRoomId);
    }

    @Test
    void testHandle_QueuesRoomForBusyCleanerWhenNoIdleAvailable() {
        Cleaner busyCleaner = new Cleaner(1, 1, 1);
        busyCleaner.state = CleanerState.CLEANING;
        data.cleaners.put(1, busyCleaner);

        FakeCleanerAssigner fakeAssigner = new FakeCleanerAssigner(202);
        handler = new EmergencyHandler(data, null, fakeAssigner);

        handler.handle(202);

        assertTrue(busyCleaner.dirtyRooms.contains(202), "Kamer moet in de wachtrij van de bezette schoonmaker staan");
        assertNull(fakeAssigner.lastCalledMethod, "assignToRoom mag niet aangeroepen worden als er geen idle schoonmaker is");
    }

    @Test
    void testHandle_DoesNothingWhenNoCleanersAvailable() {
        FakeCleanerAssigner fakeAssigner = new FakeCleanerAssigner(101);
        handler = new EmergencyHandler(data, null, fakeAssigner);

        handler.handle(101); // geen schoonmakers in data.cleaners

        assertNull(fakeAssigner.lastCalledMethod, "Geen actie als er geen schoonmakers zijn");
    }

    @Test
    void testHandle_DoesNothingWhenNoRoomsAvailable() {
        Cleaner idleCleaner = new Cleaner(1, 1, 1);
        idleCleaner.state = CleanerState.IDLE;
        data.cleaners.put(1, idleCleaner);

        FakeCleanerAssigner fakeAssigner = new FakeCleanerAssigner(-1); // geen kamer gevonden
        handler = new EmergencyHandler(data, null, fakeAssigner);

        handler.handle(999);

        assertNull(fakeAssigner.lastCalledMethod, "Geen actie als findFirstRoomId -1 teruggeeft");
    }
}