package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestMoverTest {

    private static final int TILE = 60;
    private static final int OFFSET = 60;

    private SimulationData data;
    private GuestMover mover;

    private Area makeArea(int id, String type, String pos) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = 10;
        return a;
    }

    private Guest makeWalkingGuest(int id, double x, double y, double tx, double ty) {
        Guest g = new Guest(id, x, y);
        g.targetX = tx;
        g.targetY = ty;
        g.state = GuestState.WALKING;
        g.speed = 2.0;
        return g;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        areas.add(makeArea(1, "TRAP", "3,0"));
        data = new SimulationData(areas, 4, 30, 30, 10, 15);
        data.tileSize = TILE;
        data.horizontalOffset = OFFSET;
        mover = new GuestMover(data, new StairModel(data.areas));
    }

    // ── moveGuest — niet actieve states ──────────────────────────────────────

    @Test
    void moveGuest_idleState_doesNotMove() {
        Guest g = makeWalkingGuest(1, 100, 100, 300, 100);
        g.state = GuestState.IDLE;
        mover.moveGuest(g);
        assertEquals(100.0, g.x, 0.001, "IDLE gast mag niet bewegen");
    }

    @Test
    void moveGuest_inLiftState_doesNotMove() {
        Guest g = makeWalkingGuest(1, 100, 100, 300, 100);
        g.state = GuestState.IN_LIFT;
        mover.moveGuest(g);
        assertEquals(100.0, g.x, 0.001, "IN_LIFT gast mag niet bewegen via mover");
    }

    @Test
    void moveGuest_inQueueState_doesNotMove() {
        Guest g = makeWalkingGuest(1, 100, 100, 300, 100);
        g.state = GuestState.IN_QUEUE;
        mover.moveGuest(g);
        assertEquals(100.0, g.x, 0.001);
    }

    // ── moveGuest — zelfde verdieping ─────────────────────────────────────────

    @Test
    void moveGuest_sameFloor_movesHorizontallyTowardTarget() {
        // Beide op verdieping 0 (y ≈ 0)
        Guest g = makeWalkingGuest(1, 0, 30, 200, 30);
        double xBefore = g.x;
        mover.moveGuest(g);
        assertTrue(g.x > xBefore, "Gast moet richting target bewegen (x toeneemt)");
    }

    @Test
    void moveGuest_sameFloor_reachesTarget_becomesAtDestination() {
        // Gast vlak bij de target
        Guest g = makeWalkingGuest(1, 198, 30, 200, 30);
        // Loop genoeg stappen totdat hij er is
        for (int i = 0; i < 50; i++) {
            if (g.state == GuestState.AT_DESTINATION) break;
            mover.moveGuest(g);
        }
        assertEquals(GuestState.AT_DESTINATION, g.state,
                "Gast op target moet AT_DESTINATION worden");
    }

    @Test
    void moveGuest_atDestination_doesNotOvershoot() {
        Guest g = makeWalkingGuest(1, 199, 30, 200, 30);
        for (int i = 0; i < 100; i++) mover.moveGuest(g);
        assertEquals(200.0, g.x, 0.001, "Gast mag niet voorbij de target schieten");
    }

    // ── moveGuest — andere verdieping via lift ────────────────────────────────

    @Test
    void moveGuest_differentFloor_notForceStairs_movesTowardElevator() {
        // Lift staat op x=5 (data.horizontalOffset + 10 ≈ 70 + personalOffset)
        // Gast staat ver van lift op verdieping 5, wil naar verdieping 0
        Guest g = makeWalkingGuest(1, 800, 5 * TILE + 30, 200, 0);
        g.forceStairs = false;
        double xBefore = g.x;
        mover.moveGuest(g);
        // Gast beweegt richting lift (x daalt)
        assertTrue(g.x < xBefore, "Gast moet naar de lift lopen (x daalt) als hij een andere verdieping wil");
    }

    @Test
    void moveGuest_forceStairs_movesHorizontallyTowardStairs() {
        // Trap X = 3*60 + 60 = 240
        Guest g = makeWalkingGuest(1, 800, 5 * TILE + 30, 200, 0);
        g.forceStairs = true;
        double xBefore = g.x;
        mover.moveGuest(g);
        assertTrue(g.x < xBefore, "ForcedStairs gast moet naar de trap lopen (x daalt)");
    }

    // ── moveGuest — EXITING_LIFT behandeling ─────────────────────────────────

    @Test
    void moveGuest_exitingLift_treatedAsSameFloor() {
        // EXITING_LIFT mag wél bewegen, net als WALKING op de juiste verdieping
        Guest g = makeWalkingGuest(1, 0, 30, 200, 30);
        g.state = GuestState.EXITING_LIFT;
        double xBefore = g.x;
        mover.moveGuest(g);
        assertTrue(g.x > xBefore, "EXITING_LIFT gast moet horizontaal kunnen bewegen");
    }

    // ── moveGuest — gast gaat in liftrijwachtrij ──────────────────────────────

    @Test
    void moveGuest_differentFloor_arrivedAtElevatorX_entersQueue() {
        // Zet de gast precies op de liftlocatie en op de verkeerde verdieping
        double elevatorX = data.horizontalOffset + 10; // minimale offset
        Guest g = makeWalkingGuest(1, elevatorX, 5 * TILE + 30, 200, 0);
        g.personalOffset = 0; // deterministische offset
        g.forceStairs = false;
        g.speed = 2.0;

        // Beweeg totdat hij in de rij staat of de state is veranderd
        for (int i = 0; i < 200; i++) {
            if (g.state == GuestState.IN_QUEUE) break;
            mover.moveGuest(g);
        }

        assertEquals(GuestState.IN_QUEUE, g.state,
                "Gast die de liftlocatie bereikt, moet IN_QUEUE worden");
        assertTrue(data.elevator.waitingGuests.contains(g),
                "Gast moet aan de wachtrij van de lift worden toegevoegd");
    }
}
