package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuestTest {

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_setsId() {
        Guest g = new Guest(7, 0, 0);
        assertEquals(7, g.id);
    }

    @Test
    void constructor_setsXAndY() {
        Guest g = new Guest(1, 150.0, 300.0);
        assertEquals(150.0, g.x, 0.001);
        assertEquals(300.0, g.y, 0.001);
    }

    @Test
    void constructor_targetMatchesInitialPosition() {
        Guest g = new Guest(1, 50.0, 90.0);
        assertEquals(50.0, g.targetX, 0.001);
        assertEquals(90.0, g.targetY, 0.001);
    }

    @Test
    void constructor_stateIsWalking() {
        assertEquals(GuestState.WALKING, new Guest(1, 0, 0).state);
    }

    @Test
    void constructor_currentActivityIsRoom() {
        assertEquals("ROOM", new Guest(1, 0, 0).currentActivity);
    }

    @Test
    void constructor_assignedRoomIdIsMinusOne() {
        assertEquals(-1, new Guest(1, 0, 0).assignedRoomId);
    }

    @Test
    void constructor_isNotCheckingOut() {
        assertFalse(new Guest(1, 0, 0).isCheckingOut);
    }

    @Test
    void constructor_isNotInRoom() {
        assertFalse(new Guest(1, 0, 0).isInRoom);
    }

    @Test
    void constructor_activityTimerIsZero() {
        assertEquals(0, new Guest(1, 0, 0).activityTimer);
    }

    @Test
    void constructor_speedIsTwo() {
        assertEquals(2.0, new Guest(1, 0, 0).speed, 0.001);
    }

    // ── setTarget (overridden) ────────────────────────────────────────────────

    @Test
    void setTarget_updatesTargetX() {
        Guest g = new Guest(1, 0, 0);
        g.setTarget(100.0, 200.0);
        assertEquals(100.0, g.targetX, 0.001);
    }

    @Test
    void setTarget_updatesTargetY() {
        Guest g = new Guest(1, 0, 0);
        g.setTarget(100.0, 200.0);
        assertEquals(200.0, g.targetY, 0.001);
    }

    @Test
    void setTarget_setsStateToWalking() {
        Guest g = new Guest(1, 0, 0);
        g.state = GuestState.IDLE;
        g.setTarget(50, 60);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void setTarget_doesNotChangeCurrentPosition() {
        Guest g = new Guest(1, 10.0, 20.0);
        g.setTarget(999.0, 999.0);
        assertEquals(10.0, g.x, 0.001);
        assertEquals(20.0, g.y, 0.001);
    }

    @Test
    void setTarget_calledFromIdleStillSetsWalking() {
        Guest g = new Guest(1, 0, 0);
        g.state = GuestState.AT_DESTINATION;
        g.setTarget(1, 1);
        assertEquals(GuestState.WALKING, g.state);
    }

    // ── personalOffset ────────────────────────────────────────────────────────

    @Test
    void personalOffset_isWithinExpectedRange() {
        for (int i = 0; i < 20; i++) {
            Guest g = new Guest(i, 0, 0);
            assertTrue(g.personalOffset >= -25 && g.personalOffset <= 25,
                    "personalOffset out of range: " + g.personalOffset);
        }
    }
}
