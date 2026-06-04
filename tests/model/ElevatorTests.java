package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorTests {

    private static final int TILE = 60;
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        // Start at floor 3 (y = 3 * 60 = 180)
        elevator = new Elevator(5.0, 3 * TILE, TILE);
    }

    // ── Construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_setsInitialPosition() {
        assertEquals(5.0, elevator.curX);
        assertEquals(3 * TILE, elevator.curY, 0.001);
    }

    @Test
    void constructor_isNotMovingInitially() {
        assertFalse(elevator.isMoving);
    }

    @Test
    void constructor_passengersListIsEmpty() {
        assertTrue(elevator.passengers.isEmpty());
    }

    @Test
    void constructor_defaultCapacityIsTen() {
        assertEquals(10, elevator.maxCapacity);
    }

    // ── getCurrentFloor ───────────────────────────────────────────────────────

    @Test
    void getCurrentFloor_returnsCorrectFloor() {
        assertEquals(3, elevator.getCurrentFloor());
    }

    @Test
    void getCurrentFloor_roundsCorrectly() {
        elevator.curY = 3 * TILE + 29; // just under halfway → still floor 3
        assertEquals(3, elevator.getCurrentFloor());
    }

    // ── setTargetFloor ────────────────────────────────────────────────────────

    @Test
    void setTargetFloor_setsFloorWithinBounds() {
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(5);
        assertEquals(5, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMinWhenTooLow() {
        elevator.setBounds(2, 10);
        elevator.setTargetFloor(0);
        assertEquals(2, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMaxWhenTooHigh() {
        elevator.setBounds(0, 5);
        elevator.setTargetFloor(9);
        assertEquals(5, elevator.targetFloor);
    }

    // ── setBounds ─────────────────────────────────────────────────────────────

    @Test
    void setBounds_clampsTargetFloor() {
        elevator.targetFloor = 20;
        elevator.setBounds(0, 5);
        assertTrue(elevator.targetFloor <= 5);
    }

    @Test
    void setBounds_clampsCurrentY() {
        elevator.curY = 0; // floor 0
        elevator.setBounds(2, 10); // min floor is now 2
        assertTrue(elevator.curY >= 2 * TILE - 0.001);
    }

    // ── update / movement ─────────────────────────────────────────────────────

    @Test
    void update_elevatorMovesDownTowardLowerTargetFloor() {
        // Start at floor 3, target floor 0
        elevator.setBounds(0, 10);
        elevator.targetFloor = 0;
        double initialY = elevator.curY;

        // Run enough updates to get past wait ticks, then one more to move
        for (int i = 0; i < 5; i++) elevator.update();

        // Should have moved downward (lower Y value, since higher floor = higher Y in this project)
        // Note: targetFloor 0 means y=0 which is LESS than current y=180
        assertTrue(elevator.curY < initialY || elevator.curY == 0);
    }

    @Test
    void update_elevatorStopsWhenTargetReached() {
        elevator.setBounds(0, 10);
        elevator.targetFloor = 3; // already on floor 3
        elevator.update();
        assertFalse(elevator.isMoving);
    }

    @Test
    void isWaiting_returnsFalseInitially() {
        // After construction, no wait ticks should be pending yet
        // (Wait only triggers on *arrival at a new floor*)
        assertFalse(elevator.isWaiting());
    }

    // ── passengers ────────────────────────────────────────────────────────────

    @Test
    void passengers_canBeAdded() {
        Guest g = new Guest(1, 5.0, 180.0);
        elevator.passengers.add(g);
        assertEquals(1, elevator.passengers.size());
    }

    @Test
    void update_passengersFollowElevatorY() {
        Guest g = new Guest(1, 5.0, 180.0);
        elevator.passengers.add(g);
        elevator.update();
        assertEquals(elevator.curY, g.y, 0.001);
    }
}
