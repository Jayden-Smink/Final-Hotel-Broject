package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorTest {

    private static final int TILE = 60;
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        // Start at floor 3 → y = 3 * 60 = 180
        elevator = new Elevator(5.0, 3 * TILE, TILE);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_setsStartX() {
        assertEquals(5.0, elevator.curX, 0.001);
    }

    @Test
    void constructor_setsStartY() {
        assertEquals(180.0, elevator.curY, 0.001);
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
    void constructor_defaultMaxCapacityTen() {
        assertEquals(10, elevator.maxCapacity);
    }

    @Test
    void constructor_targetFloorMatchesStartY() {
        assertEquals(3, elevator.targetFloor);
    }

    @Test
    void defaultConstructor_usesTileSize60() {
        Elevator e = new Elevator(0, 120.0); // floor 2
        assertEquals(2, e.getCurrentFloor());
    }

    // ── getCurrentFloor ───────────────────────────────────────────────────────

    @Test
    void getCurrentFloor_correctForFloor3() {
        assertEquals(3, elevator.getCurrentFloor());
    }

    @Test
    void getCurrentFloor_correctForFloor0() {
        elevator.curY = 0;
        assertEquals(0, elevator.getCurrentFloor());
    }

    @Test
    void getCurrentFloor_roundsHalfUp() {
        elevator.curY = 3 * TILE + 30; // exactly halfway to floor 4 → rounds to 4
        assertEquals(4, elevator.getCurrentFloor());
    }

    @Test
    void getCurrentFloor_justBeforeHalfwayStaysOnSameFloor() {
        elevator.curY = 3 * TILE + 29;
        assertEquals(3, elevator.getCurrentFloor());
    }

    // ── setTargetFloor ────────────────────────────────────────────────────────

    @Test
    void setTargetFloor_setsFloorWithinBounds() {
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(7);
        assertEquals(7, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMinWhenBelowMin() {
        elevator.setBounds(2, 10);
        elevator.setTargetFloor(0);
        assertEquals(2, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMaxWhenAboveMax() {
        elevator.setBounds(0, 5);
        elevator.setTargetFloor(9);
        assertEquals(5, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_exactMinIsAccepted() {
        elevator.setBounds(1, 10);
        elevator.setTargetFloor(1);
        assertEquals(1, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_exactMaxIsAccepted() {
        elevator.setBounds(0, 8);
        elevator.setTargetFloor(8);
        assertEquals(8, elevator.targetFloor);
    }

    // ── setBounds ─────────────────────────────────────────────────────────────

    @Test
    void setBounds_clampsTargetFloorAboveMax() {
        elevator.targetFloor = 20;
        elevator.setBounds(0, 5);
        assertTrue(elevator.targetFloor <= 5);
    }

    @Test
    void setBounds_clampsCurrentYBelowMin() {
        elevator.curY = 0; // below new min
        elevator.setBounds(2, 10);
        assertTrue(elevator.curY >= 2 * TILE - 0.001);
    }

    @Test
    void setBounds_clampsCurrentYAboveMax() {
        elevator.curY = 1000; // above max
        elevator.setBounds(0, 5);
        assertTrue(elevator.curY <= 5 * TILE + 0.001);
    }

    // ── isWaiting ─────────────────────────────────────────────────────────────

    @Test
    void isWaiting_falseRightAfterConstruction() {
        assertFalse(elevator.isWaiting());
    }

    @Test
    void isWaiting_trueAfterArrivingAtNewFloor() {
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(0);

        // Run updates until elevator arrives at floor 0
        for (int i = 0; i < 1000; i++) {
            elevator.update();
            if (elevator.getCurrentFloor() == 0 && elevator.isWaiting()) break;
        }
        assertTrue(elevator.isWaiting());
    }

    // ── update / movement ─────────────────────────────────────────────────────

    @Test
    void update_elevatorNotMovingWhenAlreadyAtTarget() {
        elevator.setBounds(0, 10);
        elevator.targetFloor = 3; // already here
        elevator.update();
        assertFalse(elevator.isMoving);
    }

    @Test
    void update_elevatorEventuallyReachesTargetFloor() {
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(0);

        for (int i = 0; i < 5000; i++) elevator.update();

        assertEquals(0, elevator.getCurrentFloor());
    }

    @Test
    void update_passengersYFollowsElevator() {
        Guest g = new Guest(1, 5.0, 180.0);
        elevator.passengers.add(g);
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(0);
        elevator.update(); // first update tick
        assertEquals(elevator.curY, g.y, 0.001);
    }

    @Test
    void update_passengersXIsElevatorXPlusFive() {
        Guest g = new Guest(1, 5.0, 180.0);
        elevator.passengers.add(g);
        elevator.update();
        assertEquals(elevator.curX + 5, g.x, 0.001);
    }

    @Test
    void update_doesNotMoveDuringWaitTicks() {
        elevator.setBounds(0, 10);
        elevator.setTargetFloor(0);

        // Drive to floor 0
        for (int i = 0; i < 1000; i++) elevator.update();

        double yAtArrival = elevator.curY;
        // One more update: should be in wait phase
        elevator.update();
        assertEquals(yAtArrival, elevator.curY, 0.001);
    }

    // ── clamping ──────────────────────────────────────────────────────────────

    @Test
    void update_currentYNeverExceedsMaxBound() {
        elevator.setBounds(0, 3);
        elevator.setTargetFloor(10); // beyond max
        for (int i = 0; i < 1000; i++) elevator.update();
        assertTrue(elevator.curY <= 3 * TILE + 0.001);
    }

    @Test
    void update_currentYNeverGoesBelowMinBound() {
        elevator.setBounds(2, 10);
        elevator.setTargetFloor(0); // below min
        for (int i = 0; i < 1000; i++) elevator.update();
        assertTrue(elevator.curY >= 2 * TILE - 0.001);
    }
}
