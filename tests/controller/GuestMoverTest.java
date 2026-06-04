package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestMoverTest {

    private static final int TILE = 60;
    private SimulationData data;
    private GuestMover mover;

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
        areas.add(makeArea(1, "TRAP", "3,0"));
        data = new SimulationData(areas, 10, 30);
        data.tileSize = TILE;
        data.horizontalOffset = TILE;
        mover = new GuestMover(data, new StairModel(data.areas));
    }

    private Guest guestAt(int id, double x, double y) {
        Guest g = new Guest(id, x, y);
        g.state = GuestState.WALKING;
        return g;
    }

    // ── moveGuest: state guard ────────────────────────────────────────────────

    @Test
    void moveGuest_doesNothingWhenIdle() {
        Guest g = guestAt(1, 100, 300);
        g.state = GuestState.IDLE;
        double originalX = g.x;
        mover.moveGuest(g);
        assertEquals(originalX, g.x, 0.001);
    }

    @Test
    void moveGuest_doesNothingWhenInLift() {
        Guest g = guestAt(1, 100, 300);
        g.state = GuestState.IN_LIFT;
        double originalX = g.x;
        mover.moveGuest(g);
        assertEquals(originalX, g.x, 0.001);
    }

    @Test
    void moveGuest_doesNothingWhenInQueue() {
        Guest g = guestAt(1, 100, 300);
        g.state = GuestState.IN_QUEUE;
        double originalX = g.x;
        mover.moveGuest(g);
        assertEquals(originalX, g.x, 0.001);
    }

    // ── moveGuest: same floor horizontal movement ─────────────────────────────

    @Test
    void moveGuest_movesRightWhenTargetXIsLarger() {
        // Same floor: currentFloorY == targetFloorY
        double floorY = 5 * TILE;
        Guest g = guestAt(1, 100, floorY + 10);
        g.setTarget(200, floorY + 10);
        double before = g.x;
        mover.moveGuest(g);
        assertTrue(g.x > before);
    }

    @Test
    void moveGuest_movesLeftWhenTargetXIsSmaller() {
        double floorY = 5 * TILE;
        Guest g = guestAt(1, 300, floorY + 10);
        g.setTarget(100, floorY + 10);
        double before = g.x;
        mover.moveGuest(g);
        assertTrue(g.x < before);
    }

    @Test
    void moveGuest_setsAtDestinationWhenTargetReached() {
        double floorY = 5 * TILE;
        Guest g = guestAt(1, 99.5, floorY);
        g.setTarget(100.0, floorY);
        // Run enough ticks to arrive
        for (int i = 0; i < 200; i++) {
            mover.moveGuest(g);
            if (g.state == GuestState.AT_DESTINATION) break;
        }
        assertEquals(GuestState.AT_DESTINATION, g.state);
    }

    // ── moveGuest: different floor → join lift queue ──────────────────────────

    @Test
    void moveGuest_differentFloor_eventuallyJoinsFloorQueue() {
        // Guest at floor 5, target at floor 3 (stairs are at tile-x=3 → x=240, elevator nearby)
        // Force elevator to be faster by placing guest far from stairs
        double floorY = 5 * TILE;
        Guest g = guestAt(1, 5000, floorY + 10); // far from stairs → elevator preferred
        g.setTarget(200, 3 * TILE + 10);
        data.floorQueues.put(5, new java.util.LinkedList<>());

        for (int i = 0; i < 5000; i++) {
            mover.moveGuest(g);
            if (g.state == GuestState.IN_QUEUE) break;
        }

        assertTrue(g.state == GuestState.IN_QUEUE
                        || data.floorQueues.values().stream().anyMatch(q -> q.contains(g)),
                "Guest should be queuing for elevator");
    }

    // ── moveCleaner ───────────────────────────────────────────────────────────

    @Test
    void moveCleaner_movesRightOnSameFloor() {
        Cleaner c = new Cleaner(0, 100, 5 * TILE + 10);
        c.setTarget(200, 5 * TILE + 10);
        double before = c.x;
        mover.moveCleaner(c);
        assertTrue(c.x > before);
    }

    @Test
    void moveCleaner_movesLeftOnSameFloor() {
        Cleaner c = new Cleaner(0, 300, 5 * TILE + 10);
        c.setTarget(100, 5 * TILE + 10);
        double before = c.x;
        mover.moveCleaner(c);
        assertTrue(c.x < before);
    }

    @Test
    void moveCleaner_doesNotUseElevator_headsToStairsOnDifferentFloor() {
        // Cleaner at floor 5, target floor 3
        double stairX = 3 * TILE + TILE; // 240
        Cleaner c = new Cleaner(0, 500, 5 * TILE + 10);
        c.setTarget(stairX, 3 * TILE + 10);

        mover.moveCleaner(c);

        // On different floors, cleaner should be moving toward the stair X
        boolean movingTowardStairs = (c.x < 500); // was moving left toward stairX=240
        assertTrue(movingTowardStairs);
    }

    @Test
    void moveCleaner_snapsToDestinationWhenClose() {
        Cleaner c = new Cleaner(0, 100.5, 5 * TILE + 0.5);
        c.setTarget(100.0, 5 * TILE + 0.0);
        for (int i = 0; i < 100; i++) mover.moveCleaner(c);
        assertEquals(100.0, c.x, 0.001);
        assertEquals(5 * TILE, c.y, 0.001);
    }
}
