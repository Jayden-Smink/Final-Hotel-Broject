package view;

import controller.HotelTimeEngine;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LogPanel.
 *
 * NOTE: These tests construct Swing components on the test thread.
 * If your CI requires headless mode, add: -Djava.awt.headless=true to the JVM args.
 */
class LogPanelTest {

    private LogPanel panel;

    @BeforeEach
    void setUp() {
        panel = new LogPanel();
    }

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new LogPanel());
    }

    @Test
    void addLog_doesNotThrow() {
        assertDoesNotThrow(() -> panel.addLog("Test message"));
    }

    @Test
    void addLog_acceptsEmptyString() {
        assertDoesNotThrow(() -> panel.addLog(""));
    }

    @Test
    void addLog_acceptsSpecialCharacters() {
        assertDoesNotThrow(() -> panel.addLog("🛏️ Kamer 5 toegevoegd."));
    }

    @Test
    void addLog_acceptsNullWithoutHardCrash() {
        // Depends on implementation; at minimum it should not leave panel unusable
        try {
            panel.addLog(null);
        } catch (NullPointerException e) {
            // acceptable — just document the behaviour
        }
    }

    @Test
    void addLog_multipleMessagesDoNotThrow() {
        for (int i = 0; i < 50; i++) {
            panel.addLog("Bericht " + i);
        }
    }
}

/**
 * Tests for TimeControlPanel.
 */
class TimeControlPanelTest {

    private HotelTimeEngine engine;
    private SimulationData data;
    private TimeControlPanel panel;

    private Area makeArea(String type, String pos) {
        Area a = new Area();
        a.id = 0;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = 50;
        return a;
    }

    @BeforeEach
    void setUp() {
        engine = new HotelTimeEngine();
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea("LOBBY", "0,5"));
        data = new SimulationData(areas, 10, 30, 30, 10, 15);
        panel = new TimeControlPanel(engine, data);
    }

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new TimeControlPanel(engine, data));
    }

    @Test
    void refresh_doesNotThrowWithNoGuests() {
        assertDoesNotThrow(() -> panel.refresh());
    }

    @Test
    void refresh_doesNotThrowWithGuests() {
        Guest g1 = new Guest(1, 0, 0);
        Guest g2 = new Guest(2, 0, 0);
        g2.isCheckingOut = true;
        data.guests.put(1, g1);
        data.guests.put(2, g2);
        assertDoesNotThrow(() -> panel.refresh());
    }

    @Test
    void refresh_doesNotThrowWhenPaused() {
        engine.togglePause();
        assertDoesNotThrow(() -> panel.refresh());
    }

    @Test
    void refresh_doesNotThrowAtHighSpeed() {
        engine.setSpeed(8);
        assertDoesNotThrow(() -> panel.refresh());
    }

    @Test
    void refresh_canBeCalledRepeatedly() {
        for (int i = 0; i < 20; i++) {
            assertDoesNotThrow(() -> panel.refresh());
        }
    }
}
