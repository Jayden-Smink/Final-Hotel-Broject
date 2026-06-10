package controller;

import model.Cleaner;
import model.CleanerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerPoolTest {

    private CleanerPool pool;

    @BeforeEach
    void setUp() {
        pool = new CleanerPool();
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void newPool_hasNoWorkers() {
        assertTrue(pool.getWorkers().isEmpty());
    }

    // ── setupWorkers – null guard ─────────────────────────────────────────────

    @Test
    void setupWorkers_withNullBase_doesNotThrowAndKeepsEmpty() {
        assertDoesNotThrow(() -> pool.setupWorkers(null));
        assertTrue(pool.getWorkers().isEmpty());
    }

    // ── setupWorkers – normal flow ────────────────────────────────────────────

    @Test
    void setupWorkers_createsTwoWorkers() {
        Cleaner base = new Cleaner(99, 60.0, 25.0);
        base.speed = 2.0;
        pool.setupWorkers(base);

        assertEquals(2, pool.getWorkers().size());
    }

    @Test
    void setupWorkers_firstWorkerIdIsOne() {
        Cleaner base = new Cleaner(99, 60.0, 25.0);
        pool.setupWorkers(base);

        assertEquals(1, pool.getWorkers().get(0).id,
                "First worker's id should be set to 1 regardless of the original id");
    }

    @Test
    void setupWorkers_secondWorkerIdIsTwo() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        pool.setupWorkers(base);

        assertEquals(2, pool.getWorkers().get(1).id);
    }

    @Test
    void setupWorkers_extraWorkerInheritsSpeedFromBase() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        base.speed = 3.5;
        pool.setupWorkers(base);

        assertEquals(3.5, pool.getWorkers().get(1).speed, 0.001);
    }

    @Test
    void setupWorkers_extraWorkerStartsAtBaseCoordinates() {
        Cleaner base = new Cleaner(1, 120.0, 80.0);
        pool.setupWorkers(base);

        Cleaner extra = pool.getWorkers().get(1);
        assertEquals(120.0, extra.x, 0.001);
        assertEquals(80.0, extra.y, 0.001);
    }

    @Test
    void setupWorkers_extraWorkerAssignedRoomIdIsMinusOne() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        pool.setupWorkers(base);

        assertEquals(-1, pool.getWorkers().get(1).assignedRoomId);
    }

    @Test
    void setupWorkers_extraWorkerInheritsState() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        base.state = CleanerState.IDLE;
        pool.setupWorkers(base);

        assertEquals(CleanerState.IDLE, pool.getWorkers().get(1).state);
    }

    // ── idempotency ───────────────────────────────────────────────────────────

    @Test
    void setupWorkers_calledTwice_doesNotAddMoreWorkers() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        pool.setupWorkers(base);
        pool.setupWorkers(base);   // second call must be ignored

        assertEquals(2, pool.getWorkers().size(),
                "setupWorkers must be idempotent – calling twice must not double the pool");
    }

    // ── getWorkers returns the live list ──────────────────────────────────────

    @Test
    void getWorkers_returnsSameListInstance() {
        Cleaner base = new Cleaner(1, 60.0, 25.0);
        pool.setupWorkers(base);

        List<Cleaner> first  = pool.getWorkers();
        List<Cleaner> second = pool.getWorkers();
        assertSame(first, second, "getWorkers() should return the same underlying list");
    }

    @Test
    void getWorkers_beforeSetup_returnsEmptyList() {
        List<Cleaner> workers = pool.getWorkers();
        assertNotNull(workers);
        assertTrue(workers.isEmpty());
    }
}
