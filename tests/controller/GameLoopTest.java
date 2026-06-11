package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameLoop.
 *
 * GameLoop wraps a javax.swing.Timer that fires every 16 ms (~60 fps).
 * Pure unit tests that need no wall-clock time cover construction and the
 * start/stop contract. A small set of timing tests (marked with a short
 * Thread.sleep) verify that the timer callback is actually invoked.
 */
class GameLoopTest {

    private HotelTimeEngine hte;
    private SimulationData data;
    private SimulationController controller;

    @BeforeEach
    void setUp() {
        List<Area> areas = Arrays.asList(makeLobby(), makeRoom(1));
        data = new SimulationData(areas, 4, 1, 60, 60, 60, 60);
        data.cleaners.put(1, new Cleaner(1, 60, 25));
        hte = new HotelTimeEngine();
        controller = new SimulationController(data, null, 0);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new GameLoop(controller, hte, () -> {}));
    }

    @Test
    void constructor_withDifferentSpeedSettings_doesNotThrow() {
        hte.setSpeed(4);
        assertDoesNotThrow(() -> new GameLoop(controller, hte, () -> {}));
    }

    // ── start / stop ──────────────────────────────────────────────────────────

    @Test
    void start_doesNotThrow() {
        GameLoop loop = new GameLoop(controller, hte, () -> {});
        assertDoesNotThrow(loop::start);
        loop.stop(); // cleanup
    }

    @Test
    void stop_beforeStart_doesNotThrow() {
        GameLoop loop = new GameLoop(controller, hte, () -> {});
        assertDoesNotThrow(loop::stop);
    }

    @Test
    void stop_afterStart_doesNotThrow() {
        GameLoop loop = new GameLoop(controller, hte, () -> {});
        loop.start();
        assertDoesNotThrow(loop::stop);
    }

    @Test
    void startAndStop_canBeCalledMultipleTimes() {
        GameLoop loop = new GameLoop(controller, hte, () -> {});
        assertDoesNotThrow(() -> {
            loop.start();
            loop.stop();
            loop.start();
            loop.stop();
        });
    }

    // ── onTick callback is fired when running ─────────────────────────────────

    @Test
    void onTick_isCalledAfterStart() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        GameLoop loop = new GameLoop(controller, hte, callCount::incrementAndGet);

        loop.start();
        Thread.sleep(150); // wait long enough for several 16-ms ticks
        loop.stop();

        assertTrue(callCount.get() > 0,
                "onTick callback must be called at least once after start()");
    }

    @Test
    void onTick_isNotCalledAfterStop() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        GameLoop loop = new GameLoop(controller, hte, callCount::incrementAndGet);

        loop.start();
        Thread.sleep(80);
        loop.stop();

        int countAtStop = callCount.get();
        Thread.sleep(80); // extra wait; counter must not increase

        assertEquals(countAtStop, callCount.get(),
                "onTick must not fire after stop()");
    }

    @Test
    void onTick_callCountIncreasesOverTime() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        GameLoop loop = new GameLoop(controller, hte, callCount::incrementAndGet);

        loop.start();
        Thread.sleep(50);
        int after50ms = callCount.get();
        Thread.sleep(50);
        int after100ms = callCount.get();
        loop.stop();

        assertTrue(after100ms > after50ms,
                "More ticks should accumulate over more elapsed time");
    }

    // ── paused state ──────────────────────────────────────────────────────────

    @Test
    void onTick_stillFiredWhenPaused() throws InterruptedException {
        hte.togglePause(); // paused = true
        AtomicInteger callCount = new AtomicInteger(0);
        GameLoop loop = new GameLoop(controller, hte, callCount::incrementAndGet);

        loop.start();
        Thread.sleep(150);
        loop.stop();

        // repaint/onTick still fires even when paused (controller.updateTick is skipped)
        assertTrue(callCount.get() > 0,
                "onTick (repaint) must still be called even while the simulation is paused");
    }

    // ── speed setting ─────────────────────────────────────────────────────────

    @Test
    void speed4x_doesNotCauseErrorsDuringRun() throws InterruptedException {
        hte.setSpeed(4);
        GameLoop loop = new GameLoop(controller, hte, () -> {});
        assertDoesNotThrow(() -> {
            loop.start();
            Thread.sleep(80);
            loop.stop();
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Area makeLobby() {
        Area a = new Area();
        a.id = 0;
        a.AreaType = "LOBBY";
        a.Position = "0, 5";
        a.Dimension = "3, 1";
        return a;
    }

    private Area makeRoom(int id) {
        Area a = new Area();
        a.id = id;
        a.AreaType = "ROOM";
        a.Position = "1, 1";
        a.Dimension = "1, 1";
        return a;
    }
}
