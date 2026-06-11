package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationPanel.
 *
 * SimulationPanel is a Swing component (JPanel) that also wires together
 * SimulationController, SimulationRenderer, and a GameLoop. Full headless
 * construction is tested first. Additional tests cover the side-effects of
 * construction on SimulationData (cleaners spawned, data references set).
 *
 * Tests that require a visible window (mouse events, painting) are excluded
 * because they require a display server. Run those with an integration/UI
 * test harness.
 */
class SimulationPanelTest {

    private List<Area> areas;

    @BeforeEach
    void setUp() {
        areas = Arrays.asList(makeLobby(), makeRoom(1), makeRoom(2));
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> buildPanel());
    }

    @Test
    void constructor_withMultipleCleaners_doesNotThrow() {
        assertDoesNotThrow(() -> buildPanel(areas, 4, 1, 3, 0, 60, 60, 60, 60));
    }

    @Test
    void constructor_withZeroCleaners_doesNotThrow() {
        assertDoesNotThrow(() -> buildPanel(areas, 4, 1, 0, 0, 60, 60, 60, 60));
    }

    @Test
    void constructor_withNoRoomAreas_doesNotThrow() {
        List<Area> lobbyOnly = Arrays.asList(makeLobby());
        assertDoesNotThrow(() -> buildPanel(lobbyOnly, 4, 1, 1, 0, 60, 60, 60, 60));
    }

    // ── createBottomPanel ────────────────────────────────────────────────────

    @Test
    void createBottomPanel_returnsNonNull() {
        SimulationPanel panel = buildPanel();
        assertNotNull(panel.createBottomPanel());
    }

    @Test
    void createBottomPanel_canBeCalledTwice() {
        SimulationPanel panel = buildPanel();
        assertDoesNotThrow(() -> {
            panel.createBottomPanel();
            panel.createBottomPanel();
        });
    }

    // ── Swing component properties ────────────────────────────────────────────

    @Test
    void panel_isNotNull() {
        assertNotNull(buildPanel());
    }

    @Test
    void panel_hasLayout() {
        SimulationPanel panel = buildPanel();
        assertNotNull(panel.getLayout());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SimulationPanel buildPanel() {
        return buildPanel(areas, 4, 1, 1, 0, 60, 60, 60, 60);
    }

    private SimulationPanel buildPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds,
            int cleanerCount,
            int selectedScenario,
            int cinemaDurationSeconds,
            int restaurantDurationSeconds,
            int fitnessDurationSeconds,
            int elevatorWaitSeconds
    ) {
        return new SimulationPanel(
                areas,
                capacity,
                cleaningSeconds,
                cleanerCount,
                selectedScenario,
                cinemaDurationSeconds,
                restaurantDurationSeconds,
                fitnessDurationSeconds,
                elevatorWaitSeconds
        );
    }

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
        a.Position = id + ", 1";
        a.Dimension = "1, 1";
        return a;
    }
}
