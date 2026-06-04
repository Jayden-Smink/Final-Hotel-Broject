package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CleanerTest {

    @Test
    void constructor_setsIdAndPosition() {
        Cleaner c = new Cleaner(3, 120.0, 240.0);
        assertEquals(3, c.id);
        assertEquals(120.0, c.x, 0.001);
        assertEquals(240.0, c.y, 0.001);
    }

    @Test
    void constructor_initialStateIsIdle() {
        assertEquals(CleanerState.IDLE, new Cleaner(1, 0, 0).state);
    }

    @Test
    void constructor_assignedRoomIdIsMinusOne() {
        assertEquals(-1, new Cleaner(1, 0, 0).assignedRoomId);
    }

    @Test
    void constructor_homeRoomIdIsMinusOne() {
        assertEquals(-1, new Cleaner(1, 0, 0).homeRoomId);
    }

    @Test
    void constructor_cleaningTimerIsZero() {
        assertEquals(0, new Cleaner(1, 0, 0).cleaningTimer);
    }

    @Test
    void constructor_dirtyRoomsIsEmpty() {
        assertTrue(new Cleaner(1, 0, 0).dirtyRooms.isEmpty());
    }

    @Test
    void setTarget_updatesTargetXAndY() {
        Cleaner c = new Cleaner(1, 0, 0);
        c.setTarget(77.0, 88.0);
        assertEquals(77.0, c.targetX, 0.001);
        assertEquals(88.0, c.targetY, 0.001);
    }

    @Test
    void dirtyRooms_canAddAndRemove() {
        Cleaner c = new Cleaner(1, 0, 0);
        c.dirtyRooms.add(10);
        c.dirtyRooms.add(11);
        assertEquals(2, c.dirtyRooms.size());
        c.dirtyRooms.remove(0);
        assertEquals(11, (int) c.dirtyRooms.get(0));
    }
}
