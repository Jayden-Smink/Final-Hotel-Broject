package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorControllerTest {

    private static final int TILE = 60;
    private SimulationData data;
    private ElevatorController controller;

    private Area makeArea(int id, String type, String pos) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.capacity = 10;
        return a;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        data = new SimulationData(areas, 10, 30);
        data.tileSize = TILE;
        controller = new ElevatorController(data);
    }

    // ── update with no elevator ───────────────────────────────────────────────

    @Test
    void update_doesNotThrowWhenElevatorIsNull() {
        data.elevator = null;
        assertDoesNotThrow(() -> controller.update());
    }

    // ── boarding ──────────────────────────────────────────────────────────────

    @Test
    void update_guestBoardsElevatorFromQueue() {
        // elevator starts at bottomFloor = 5 (from LOBBY at "0,5")
        // ElevatorController uses (int)(elevator.curY / tileSize) as queue key → 5
        int elevatorFloor = data.elevator.getCurrentFloor(); // = 5
        Guest g = new Guest(1, 5.0, elevatorFloor * TILE);
        g.setTarget(5.0, 2 * TILE);
        data.guests.put(g.id, g);

        // Register queue under the tile-index key the controller will look up
        data.floorQueues.putIfAbsent(elevatorFloor, new java.util.LinkedList<>());
        data.floorQueues.get(elevatorFloor).add(g);

        for (int i = 0; i < 100; i++) controller.update();

        assertTrue(data.elevator.passengers.contains(g)
                        || g.state == GuestState.IN_LIFT
                        || g.state == GuestState.EXITING_LIFT
                        || !data.floorQueues.get(elevatorFloor).contains(g),
                "Guest should have left the queue");
    }

    @Test
    void update_guestStateBecomesInLiftAfterBoarding() {
        int floor = data.elevator.getCurrentFloor();
        Guest g = new Guest(1, 5.0, floor * TILE);
        g.setTarget(5.0, 2 * TILE);
        data.guests.put(g.id, g);
        data.floorQueues.putIfAbsent(floor, new java.util.LinkedList<>());
        data.floorQueues.get(floor).add(g);

        for (int i = 0; i < 100; i++) controller.update();

        assertTrue(g.state == GuestState.IN_LIFT
                        || g.state == GuestState.EXITING_LIFT
                        || data.elevator.passengers.contains(g),
                "Guest should be in lift");
    }

    // ── alighting ─────────────────────────────────────────────────────────────

    @Test
    void update_guestExitsElevatorAtTargetFloor() {
        int targetFloor = 2;
        Guest g = new Guest(1, 5.0, data.elevator.curY);
        g.setTarget(5.0, targetFloor * TILE);
        g.state = GuestState.IN_LIFT;
        data.elevator.passengers.add(g);
        data.elevator.setTargetFloor(targetFloor);
        data.elevator.setBounds(0, 10);

        for (int i = 0; i < 5000; i++) controller.update();

        assertFalse(data.elevator.passengers.contains(g), "Guest should have exited lift");
    }

    @Test
    void update_guestStateIsExitingLiftAfterArrival() {
        int targetFloor = 2;
        Guest g = new Guest(1, 5.0, data.elevator.curY);
        g.setTarget(5.0, targetFloor * TILE);
        g.state = GuestState.IN_LIFT;
        data.elevator.passengers.add(g);
        data.elevator.setTargetFloor(targetFloor);
        data.elevator.setBounds(0, 10);

        for (int i = 0; i < 5000; i++) controller.update();

        assertEquals(GuestState.EXITING_LIFT, g.state);
    }

    // ── capacity ──────────────────────────────────────────────────────────────

    @Test
    void update_elevatorDoesNotExceedMaxCapacity() {
        int floor = data.elevator.getCurrentFloor();
        data.elevator.maxCapacity = 2;

        data.floorQueues.putIfAbsent(floor, new java.util.LinkedList<>());
        for (int i = 1; i <= 5; i++) {
            Guest g = new Guest(i, 5.0, floor * TILE);
            g.setTarget(5.0, 2 * TILE);
            data.guests.put(i, g);
            data.floorQueues.get(floor).add(g);
        }

        for (int i = 0; i < 200; i++) controller.update();

        assertTrue(data.elevator.passengers.size() <= 2);
    }

    // ── target determination ──────────────────────────────────────────────────

    @Test
    void update_emptyElevatorMovesToFloorWithWaitingGuests() {
        int waitFloor = 2;
        Guest g = new Guest(1, 5.0, waitFloor * TILE);
        g.setTarget(5.0, 0);
        data.floorQueues.putIfAbsent(waitFloor, new java.util.LinkedList<>());
        data.floorQueues.get(waitFloor).add(g);

        for (int i = 0; i < 10; i++) controller.update();

        assertEquals(waitFloor, data.elevator.targetFloor);
    }
}