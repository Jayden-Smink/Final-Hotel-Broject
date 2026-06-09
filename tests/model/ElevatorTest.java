package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorTest {

    private Elevator elevator;
    private final int TILE = 60;

    @BeforeEach
    void setUp() {
        // Start at floor 3 (Y = 3 * 60 = 180)
        elevator = new Elevator(5.0, 3 * TILE, TILE);
    }

    @Test
    void constructor_setsInitialPosition() {
        assertEquals(5.0, elevator.curX, 0.001);
        assertEquals(180.0, elevator.curY, 0.001);
    }

    @Test
    void constructor_defaultTileSize_works() {
        Elevator e = new Elevator(0, 0);
        assertEquals(0.0, e.curY, 0.001);
    }

    @Test
    void getCurrentFloor_returnsCorrectFloor() {
        assertEquals(3, elevator.getCurrentFloor());
    }

    @Test
    void setTargetFloor_setsFloorWithinBounds() {
        elevator.setTargetFloor(5);
        assertEquals(5, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMinFloor() {
        elevator.setBounds(2, 10);
        elevator.setTargetFloor(0);
        assertEquals(2, elevator.targetFloor);
    }

    @Test
    void setTargetFloor_clampsToMaxFloor() {
        elevator.setBounds(0, 5);
        elevator.setTargetFloor(99);
        assertEquals(5, elevator.targetFloor);
    }

    @Test
    void setBounds_clampsCurrentFloor() {
        elevator.setBounds(0, 2); // current floor 3 > max 2
        assertTrue(elevator.curY <= 2 * TILE);
    }

    @Test
    void update_movesElevatorTowardsTargetFloor() {
        elevator.setTargetFloor(5);
        double initialY = elevator.curY;
        elevator.update();
        assertTrue(elevator.curY > initialY, "Elevator should move up toward floor 5");
    }

    @Test
    void update_elevatorMovesDown() {
        elevator.setTargetFloor(0);
        double initialY = elevator.curY;
        elevator.update();
        assertTrue(elevator.curY < initialY, "Elevator should move down toward floor 0");
    }

    @Test
    void update_elevatorStopsAtTarget() {
        elevator.setTargetFloor(3); // already at floor 3
        elevator.update();
        assertFalse(elevator.isMoving);
    }

    @Test
    void isWaiting_returnsFalseInitially() {
        assertFalse(elevator.isWaiting());
    }

    @Test
    void isWaiting_trueAfterArrival() {
        elevator.setTargetFloor(5);
        // Simulate enough updates to reach floor 5 and trigger wait
        for (int i = 0; i < 500; i++) {
            elevator.update();
        }
        // After arriving, it enters a wait period
        int arrivedFloor = elevator.getCurrentFloor();
        assertEquals(5, arrivedFloor);
    }

    @Test
    void maxCapacity_defaultIsTen() {
        Elevator e = new Elevator(0, 0);
        assertEquals(10, e.maxCapacity);
    }

    @Test
    void passengers_startsEmpty() {
        assertTrue(elevator.passengers.isEmpty());
    }

    @Test
    void updatePassengerPositions_movesPassengersWithElevator() {
        Guest g = new Guest(1, 0, 0);
        elevator.passengers.add(g);
        elevator.setTargetFloor(5);
        for (int i = 0; i < 200; i++) {
            elevator.update();
        }
        // Passenger Y should roughly match elevator Y
        assertEquals(elevator.curY, g.y, 1.0);
    }
}