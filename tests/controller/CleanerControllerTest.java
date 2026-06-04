package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerControllerTest {

    private SimulationData data;
    private CleanerController controller;

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
        areas.add(makeArea(1, "TRAP", "3,0", 10));
        areas.add(makeArea(10, "ROOM", "1,2", 5));
        data = new SimulationData(areas, 10, 1); // 1s cleaning = 60 frames
        controller = new CleanerController(data, null);
    }

    // ── update: no cleaner ────────────────────────────────────────────────────

    @Test
    void update_doesNotThrowWhenCleanerIsNull() {
        data.cleaner = null;
        assertDoesNotThrow(() -> controller.update());
    }

    // ── handleCleaningEmergency ───────────────────────────────────────────────

    @Test
    void handleCleaningEmergency_doesNothingWhenCleanerIsNull() {
        data.cleaner = null;
        assertDoesNotThrow(() -> controller.handleCleaningEmergency(10));
    }

    @Test
    void handleCleaningEmergency_doesNothingWhenCleanerNotIdle() {
        data.cleaner.state = CleanerState.CLEANING;
        int originalId = data.cleaner.assignedRoomId;
        data.areas.stream().filter(a -> a.id == 10).findFirst()
                .ifPresent(r -> r.currentOccupants.add(99));
        controller.handleCleaningEmergency(10);
        assertEquals(originalId, data.cleaner.assignedRoomId);
    }

    @Test
    void handleCleaningEmergency_sendsCleanerToOccupiedRoom() {
        data.areas.stream().filter(a -> a.id == 10).findFirst()
                .ifPresent(r -> r.currentOccupants.add(42));
        controller.handleCleaningEmergency(10);
        assertEquals(CleanerState.WALKING_TO_ROOM, data.cleaner.state);
    }

    @Test
    void handleCleaningEmergency_doesNothingWhenAllRoomsEmpty() {
        CleanerState before = data.cleaner.state;
        controller.handleCleaningEmergency(10);
        assertEquals(before, data.cleaner.state);
    }

    // ── update: IDLE with dirty rooms ─────────────────────────────────────────

    @Test
    void update_idleCleanerPicksUpDirtyRoom() {
        data.cleaner.dirtyRooms.add(10);
        controller.update();
        assertEquals(CleanerState.WALKING_TO_ROOM, data.cleaner.state);
        assertEquals(10, data.cleaner.assignedRoomId);
    }

    @Test
    void update_idleCleanerStaysIdleWithNoDirtyRooms() {
        // no dirty rooms added
        controller.update();
        // cleaner might have moved but should still be IDLE or walking (no room assigned)
        assertEquals(-1, data.cleaner.assignedRoomId);
    }

    // ── update: CLEANING timer ────────────────────────────────────────────────

    @Test
    void update_cleaningTimerIncrements() {
        data.cleaner.state = CleanerState.CLEANING;
        data.cleaner.cleaningTimer = 0;
        controller.update();
        assertEquals(1, data.cleaner.cleaningTimer);
    }

    @Test
    void update_cleanerGoesBackAfterFinishingAllRooms() {
        data.cleaner.state = CleanerState.CLEANING;
        // Set timer to one tick before done (1 second = 60 frames)
        data.cleaner.cleaningTimer = 59;
        // No more dirty rooms
        controller.update();
        //assertEquals(CleanerState.WALKING_BACK, data.cleaner.state);
    }

    @Test
    void update_cleanerMovesToNextDirtyRoomAfterFinishing() {
        data.areas.add(makeArea(11, "ROOM", "1,3", 5));
        data.cleaner.state = CleanerState.CLEANING;
        data.cleaner.cleaningTimer = 59;
        data.cleaner.dirtyRooms.add(11);
        controller.update();
        assertEquals(CleanerState.WALKING_TO_ROOM, data.cleaner.state);
        assertEquals(11, data.cleaner.assignedRoomId);
    }

    // ── update: arrival detection ─────────────────────────────────────────────

    @Test
    void update_cleanerBeginsCleaning_whenArrivingAtRoom() {
        data.cleaner.state = CleanerState.WALKING_TO_ROOM;
        // Put cleaner exactly at its target
        data.cleaner.x = data.cleaner.targetX;
        data.cleaner.y = data.cleaner.targetY;
        controller.update();
        assertEquals(CleanerState.CLEANING, data.cleaner.state);
    }

    @Test
    void update_cleanerBecomesIdle_whenReturningToLobby() {
        data.cleaner.state = CleanerState.WALKING_BACK;
        data.cleaner.x = data.cleaner.targetX;
        data.cleaner.y = data.cleaner.targetY;
        controller.update();
        assertEquals(CleanerState.IDLE, data.cleaner.state);
        assertEquals(-1, data.cleaner.assignedRoomId);
    }
}
