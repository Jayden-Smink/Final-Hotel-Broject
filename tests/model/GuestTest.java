package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuestTest {

    @Test
    void constructor_setsIdAndPosition() {
        Guest g = new Guest(5, 100.0, 200.0);
        assertEquals(5, g.id);
        assertEquals(100.0, g.x, 0.001);
        assertEquals(200.0, g.y, 0.001);
    }

    @Test
    void constructor_initialStateIsWalking() {
        Guest g = new Guest(1, 0, 0);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void constructor_initialActivityIsRoom() {
        Guest g = new Guest(1, 0, 0);
        assertEquals("ROOM", g.currentActivity);
    }

    @Test
    void constructor_assignedRoomIdIsMinusOne() {
        Guest g = new Guest(1, 0, 0);
        assertEquals(-1, g.assignedRoomId);
    }

    @Test
    void constructor_isNotCheckingOut() {
        Guest g = new Guest(1, 0, 0);
        assertFalse(g.isCheckingOut);
    }

    @Test
    void constructor_isNotInRoom() {
        Guest g = new Guest(1, 0, 0);
        assertFalse(g.isInRoom);
    }

    @Test
    void setTarget_updatesTargetCoordinates() {
        Guest g = new Guest(1, 0, 0);
        g.setTarget(50.0, 80.0);
        assertEquals(50.0, g.targetX, 0.001);
        assertEquals(80.0, g.targetY, 0.001);
    }

    @Test
    void setTarget_setsStateToWalking() {
        Guest g = new Guest(1, 0, 0);
        g.state = GuestState.IDLE;
        g.setTarget(10, 20);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void activityTimer_startsAtZero() {
        Guest g = new Guest(1, 0, 0);
        assertEquals(0, g.activityTimer);
    }
}
