package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerControllerTest {

    private SimulationData data;
    private CleanerController controller;

    private Area makeRoom(int id, int col, int row) {
        Area a = new Area();
        a.id = id;
        a.AreaType = "ROOM";
        a.Position = col + ", " + row;
        a.Dimension = "1, 1";
        return a;
    }

    private Area makeLobby(int col, int row) {
        Area a = new Area();
        a.id = 0;
        a.AreaType = "LOBBY";
        a.Position = col + ", " + row;
        a.Dimension = "2, 1";
        return a;
    }

    private Cleaner makeCleaner(int id) {
        return new Cleaner(id, 60, 25);
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = Arrays.asList(makeLobby(0, 0), makeRoom(1, 2, 1), makeRoom(2, 4, 2));

        // Use the real constructor: (areas, capacity, cleaningSeconds, cinema, restaurant, fitness)
        data = new SimulationData(areas, 4, 1, 60, 60, 60);

        data.cleaners.put(1, makeCleaner(1));
        data.cleaners.put(2, makeCleaner(2));

        controller = new CleanerController(data, null);
    }

    // ── handleCleaningEmergency ───────────────────────────────────────────────

    @Test
    void emergency_assignsToIdleCleaner_setsStateToWalking() {
        controller.handleCleaningEmergency(1);

        boolean anyAssigned = data.cleaners.values().stream()
                .anyMatch(c -> c.state == CleanerState.WALKING_TO_ROOM || c.dirtyRooms.contains(1));
        assertTrue(anyAssigned, "Room 1 should be assigned after emergency");
    }

    @Test
    void emergency_withNoCleaners_doesNotThrow() {
        data.cleaners.clear();
        assertDoesNotThrow(() -> controller.handleCleaningEmergency(1));
    }

    @Test
    void emergency_assignsToCleanerWithShortestQueue() {
        Cleaner c1 = data.cleaners.get(1);
        Cleaner c2 = data.cleaners.get(2);

        // Both busy so queue length decides; give c2 more items so c1 is strictly shortest
        c1.state = CleanerState.CLEANING;
        c2.state = CleanerState.CLEANING;
        c2.dirtyRooms.add(91);
        c2.dirtyRooms.add(92);
        // c1 queue is empty → c1 must win regardless of HashMap iteration order

        controller.handleCleaningEmergency(1);

        assertTrue(c1.dirtyRooms.contains(1),
                "Cleaner with shorter queue (c1) should receive the emergency room");
    }

    @Test
    void emergency_cleanerBusy_addsToQueue() {
        data.cleaners.get(1).state = CleanerState.CLEANING;
        data.cleaners.get(2).state = CleanerState.CLEANING;

        controller.handleCleaningEmergency(1);

        boolean queued = data.cleaners.values().stream().anyMatch(c -> c.dirtyRooms.contains(1));
        assertTrue(queued, "Busy cleaner should have room 1 in their queue");
    }

    @Test
    void emergency_unknownRoomId_doesNotCrash() {
        assertDoesNotThrow(() -> controller.handleCleaningEmergency(999));
    }

    // ── getActiveCleaners ─────────────────────────────────────────────────────

    @Test
    void getActiveCleaners_returnsAllCleaners() {
        assertEquals(2, controller.getActiveCleaners().size());
    }

    @Test
    void getActiveCleaners_returnsDefensiveCopy() {
        controller.getActiveCleaners().clear();
        assertEquals(2, controller.getActiveCleaners().size(),
                "Modifying returned list must not affect internal state");
    }

    // ── update — IDLE pickup ──────────────────────────────────────────────────

    @Test
    void update_idleCleanerWithQueuedRoom_startsWalking() {
        Cleaner c1 = data.cleaners.get(1);
        c1.dirtyRooms.add(1);

        controller.update();

        assertEquals(CleanerState.WALKING_TO_ROOM, c1.state,
                "IDLE cleaner with queued room should start walking");
    }

    @Test
    void update_idleCleanerWithEmptyQueue_staysIdle() {
        controller.update();
        assertEquals(CleanerState.IDLE, data.cleaners.get(1).state);
    }

    // ── update — cleaning timer ───────────────────────────────────────────────

    @Test
    void update_cleaningTimerFull_noQueue_setsWalkingBack() {
        Cleaner c1 = data.cleaners.get(1);
        c1.state = CleanerState.CLEANING;
        c1.cleaningTimer = data.cleanerSettings.getCleaningDurationFrames() - 1;

        controller.update();

        assertEquals(CleanerState.WALKING_BACK, c1.state,
                "Finished cleaning with empty queue → should walk back");
        assertEquals(0, c1.cleaningTimer, "Timer should reset to 0");
    }

    @Test
    void update_cleaningTimerFull_withQueue_assignsNextRoom() {
        Cleaner c1 = data.cleaners.get(1);
        c1.state = CleanerState.CLEANING;
        c1.cleaningTimer = data.cleanerSettings.getCleaningDurationFrames() - 1;
        c1.dirtyRooms.add(2);

        controller.update();

        assertEquals(CleanerState.WALKING_TO_ROOM, c1.state,
                "Should walk to next queued room after finishing");
        assertTrue(c1.dirtyRooms.isEmpty(), "Queue should be consumed");
    }

    @Test
    void update_cleaningTimerNotFull_remainsCleaning() {
        Cleaner c1 = data.cleaners.get(1);
        c1.state = CleanerState.CLEANING;
        c1.cleaningTimer = 3;

        controller.update();

        assertEquals(CleanerState.CLEANING, c1.state);
        assertEquals(4, c1.cleaningTimer, "Timer should increment by 1");
    }

    // ── update — arrival detection ────────────────────────────────────────────

    @Test
    void update_walkingToRoom_arrivedAtTarget_startsCleaning() {
        Cleaner c1 = data.cleaners.get(1);
        c1.state = CleanerState.WALKING_TO_ROOM;
        c1.targetX = 200; c1.targetY = 85;
        c1.x = 200;       c1.y = 85;

        controller.update();

        assertEquals(CleanerState.CLEANING, c1.state,
                "Cleaner at target while WALKING_TO_ROOM should switch to CLEANING");
    }

    @Test
    void update_walkingBack_arrivedAtTarget_becomesIdle() {
        Cleaner c1 = data.cleaners.get(1);
        c1.state = CleanerState.WALKING_BACK;
        c1.assignedRoomId = 1;
        c1.targetX = 100; c1.targetY = 25;
        c1.x = 100;       c1.y = 25;

        controller.update();

        assertEquals(CleanerState.IDLE, c1.state,
                "Cleaner that reached lobby should become IDLE");
        assertEquals(-1, c1.assignedRoomId, "assignedRoomId should be cleared");
    }

    // ── multi-cleaner independence ────────────────────────────────────────────

    @Test
    void update_multipleCleaners_updatedIndependently() {
        Cleaner c1 = data.cleaners.get(1);
        Cleaner c2 = data.cleaners.get(2);
        c1.dirtyRooms.add(1);

        controller.update();

        assertEquals(CleanerState.WALKING_TO_ROOM, c1.state, "c1 should walk to room 1");
        assertEquals(CleanerState.IDLE, c2.state, "c2 should remain IDLE");
    }
}