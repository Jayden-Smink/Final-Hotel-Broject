package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CleanerStateHandlerTest {

    /**
     * Handmatige nep-implementatie van CleanerAssigner.
     * Slaat op welke methode als laatste werd aangeroepen, zodat tests dit kunnen controleren.
     * Geen Mockito nodig.
     */
    static class FakeCleanerAssigner extends CleanerAssigner {
        String lastCalledMethod = null;
        Cleaner lastCleaner = null;
        int lastRoomId = -1;

        FakeCleanerAssigner() {
            super(null, null); // data en logPanel worden niet gebruikt in de fake
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
    }

    private CleanerStateHandler handler;
    private SimulationData data;
    private FakeCleanerAssigner fakeAssigner;
    private Cleaner worker;

    @BeforeEach
    void setUp() {
        // 1 seconde × 60fps = 60 frames per schoonmaakcyclus
        data = new SimulationData(new ArrayList<>(), 4, 1, 60, 60, 60, 60);

        fakeAssigner = new FakeCleanerAssigner();

        // LogPanel is null-safe in de handler
        handler = new CleanerStateHandler(data, null, fakeAssigner);

        worker = new Cleaner(1, 0, 0);
        worker.dirtyRooms = new ArrayList<>();
    }

    @Test
    void testUpdate_IncrementsTimerWhenCleaning() {
        worker.state = CleanerState.CLEANING;
        worker.cleaningTimer = 0;
        worker.targetX = 999;
        worker.targetY = 999;

        handler.update(worker);

        assertEquals(1, worker.cleaningTimer, "Timer moet elke frame met 1 ophogen");
    }

    @Test
    void testUpdate_FinishCleaningAssignsNextRoom() {
        // 60 frames is de drempel; timer op 59 zodat één tick hem afrondt
        worker.state = CleanerState.CLEANING;
        worker.cleaningTimer = 59;
        worker.targetX = 999;
        worker.targetY = 999;
        worker.dirtyRooms.add(101);

        handler.update(worker);

        assertEquals("assignToRoom", fakeAssigner.lastCalledMethod);
        assertEquals(101, fakeAssigner.lastRoomId);
        assertEquals(0, worker.cleaningTimer, "Timer moet resetten na afronding");
    }

    @Test
    void testUpdate_FinishCleaning_NoMoreRooms_SendsToLobby() {
        worker.state = CleanerState.CLEANING;
        worker.cleaningTimer = 59;
        worker.targetX = 999;
        worker.targetY = 999;

        handler.update(worker);

        assertEquals("sendToLobby", fakeAssigner.lastCalledMethod);
        assertEquals(CleanerState.WALKING_BACK, worker.state);
        assertEquals(0, worker.cleaningTimer);
    }

    @Test
    void testUpdate_HandlesArrivalAtRoom() {
        worker.state = CleanerState.WALKING_TO_ROOM;
        worker.targetX = 2;
        worker.targetY = 2;

        handler.update(worker);

        assertEquals(CleanerState.CLEANING, worker.state, "Aankomst bij kamer moet CLEANING starten");
    }

    @Test
    void testUpdate_HandlesArrivalAtLobby() {
        worker.state = CleanerState.WALKING_BACK;
        worker.assignedRoomId = 5;
        worker.targetX = 2;
        worker.targetY = 2;

        handler.update(worker);

        assertEquals(CleanerState.IDLE, worker.state, "Aankomst bij lobby moet state IDLE zetten");
        assertEquals(-1, worker.assignedRoomId, "assignedRoomId moet resetten naar -1");
    }

    @Test
    void testUpdate_IdleWithDirtyRooms_AssignsNext() {
        worker.state = CleanerState.IDLE;
        worker.targetX = 999;
        worker.targetY = 999;
        worker.dirtyRooms.add(505);

        handler.update(worker);

        assertEquals("assignToRoom", fakeAssigner.lastCalledMethod);
        assertEquals(505, fakeAssigner.lastRoomId);
        assertTrue(worker.dirtyRooms.isEmpty(), "Kamer moet uit de wachtrij verwijderd worden na toewijzing");
    }

    @Test
    void testUpdate_IdleWithNoRooms_DoesNothing() {
        worker.state = CleanerState.IDLE;
        worker.targetX = 999;
        worker.targetY = 999;

        handler.update(worker);

        assertNull(fakeAssigner.lastCalledMethod, "Geen methode mag aangeroepen worden als er geen vuile kamers zijn");
        assertEquals(CleanerState.IDLE, worker.state);
    }
}