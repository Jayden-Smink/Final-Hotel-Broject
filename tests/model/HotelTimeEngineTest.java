package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HotelTimeEngineTest {

    private HotelTimeEngine engine;

    @BeforeEach
    void setUp() {
        engine = new HotelTimeEngine();
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void isPaused_falseInitially() {
        assertFalse(engine.isPaused());
    }

    @Test
    void getSpeed_oneInitially() {
        assertEquals(1, engine.getSpeed());
    }

    // ── togglePause ───────────────────────────────────────────────────────────

    @Test
    void togglePause_pausesWhenRunning() {
        engine.togglePause();
        assertTrue(engine.isPaused());
    }

    @Test
    void togglePause_resumesWhenPaused() {
        engine.togglePause();
        engine.togglePause();
        assertFalse(engine.isPaused());
    }

    @Test
    void togglePause_threeTimesLeavesItPaused() {
        engine.togglePause();
        engine.togglePause();
        engine.togglePause();
        assertTrue(engine.isPaused());
    }

    @Test
    void togglePause_doesNotAffectSpeed() {
        engine.setSpeed(4);
        engine.togglePause();
        assertEquals(4, engine.getSpeed());
    }

    // ── setSpeed ──────────────────────────────────────────────────────────────

    @Test
    void setSpeed_setsValidValue() {
        engine.setSpeed(3);
        assertEquals(3, engine.getSpeed());
    }

    @Test
    void setSpeed_clampsZeroToOne() {
        engine.setSpeed(0);
        assertEquals(1, engine.getSpeed());
    }

    @Test
    void setSpeed_clampsNegativeToOne() {
        engine.setSpeed(-5);
        assertEquals(1, engine.getSpeed());
    }

    @Test
    void setSpeed_acceptsOne() {
        engine.setSpeed(1);
        assertEquals(1, engine.getSpeed());
    }

    @Test
    void setSpeed_acceptsLargeValue() {
        engine.setSpeed(100);
        assertEquals(100, engine.getSpeed());
    }

    @Test
    void setSpeed_doesNotAffectPauseState() {
        engine.togglePause();
        engine.setSpeed(4);
        assertTrue(engine.isPaused());
    }

    // ── combined ──────────────────────────────────────────────────────────────

    @Test
    void combinedState_speedAndPauseAreIndependent() {
        engine.setSpeed(5);
        engine.togglePause();
        assertEquals(5, engine.getSpeed());
        assertTrue(engine.isPaused());

        engine.togglePause();
        assertEquals(5, engine.getSpeed());
        assertFalse(engine.isPaused());
    }
}
