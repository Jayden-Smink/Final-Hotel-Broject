package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationController.
 *
 * Note: SimulationController.notify() accepts hotelevents.HotelEvent, whose
 * source is not shipped with this project (only its compiled .class files are
 * present in out/). These tests therefore focus on the parts of the public
 * contract that are accessible without that dependency:
 *   - construction (no crash)
 *   - updateTick() (state advances without throwing)
 *   - getCleanerController() (returns a valid, consistent instance)
 *
 * If hotelevents sources are ever added, extend this class with notify() tests
 * using HotelEvent(HotelEventType, int, int).
 */
class SimulationControllerTest {

    private SimulationData data;
    private SimulationController controller;

    @BeforeEach
    void setUp() {
        List<Area> areas = Arrays.asList(makeLobby(), makeRoom(1), makeRoom(2));
        data = new SimulationData(areas, 4, 1, 60, 60, 60);
        data.cleaners.put(1, new Cleaner(1, 60, 25));

        // Scenario 0 → no automatic events during unit tests
        controller = new SimulationController(data, null, 0);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        List<Area> areas = Arrays.asList(makeLobby(), makeRoom(1));
        SimulationData d = new SimulationData(areas, 4, 1, 60, 60, 60);
        d.cleaners.put(1, new Cleaner(1, 60, 25));

        assertDoesNotThrow(() -> new SimulationController(d, null, 0));
    }

    @Test
    void constructor_withLogPanel_doesNotThrow() {
        List<Area> areas = Arrays.asList(makeLobby(), makeRoom(1));
        SimulationData d = new SimulationData(areas, 4, 1, 60, 60, 60);
        d.cleaners.put(1, new Cleaner(1, 60, 25));
        view.LogPanel log = new view.LogPanel();

        assertDoesNotThrow(() -> new SimulationController(d, log, 0));
    }

    // ── getCleanerController ──────────────────────────────────────────────────

    @Test
    void getCleanerController_returnsNonNull() {
        assertNotNull(controller.getCleanerController());
    }

    @Test
    void getCleanerController_sameInstanceOnMultipleCalls() {
        CleanerController first  = controller.getCleanerController();
        CleanerController second = controller.getCleanerController();
        assertSame(first, second,
                "getCleanerController() must return the same instance each time");
    }

    @Test
    void getCleanerController_isInstanceOfCleanerController() {
        assertTrue(controller.getCleanerController() instanceof CleanerController);
    }

    // ── updateTick ────────────────────────────────────────────────────────────

    @Test
    void updateTick_doesNotThrowWithNoGuests() {
        assertDoesNotThrow(() -> controller.updateTick());
    }

    @Test
    void updateTick_doesNotThrowWithMultipleCleaners() {
        data.cleaners.put(2, new Cleaner(2, 60, 25));
        assertDoesNotThrow(() -> controller.updateTick());
    }

    @Test
    void updateTick_canBeCalledManyTimesWithoutError() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 200; i++) {
                controller.updateTick();
            }
        });
    }

    @Test
    void updateTick_withIdleCleanerAndDirtyQueue_doesNotThrow() {
        Cleaner c = data.cleaners.get(1);
        c.state = CleanerState.IDLE;
        c.dirtyRooms.add(1);

        assertDoesNotThrow(() -> controller.updateTick());
    }

    @Test
    void updateTick_withGuestInData_doesNotThrow() {
        Guest guest = new Guest(1, 120.0, 300.0);
        guest.assignedRoomId = 1;
        data.guests.put(1, guest);

        assertDoesNotThrow(() -> controller.updateTick());
    }

    // ── cleanerController delegation ─────────────────────────────────────────

    @Test
    void cleanerController_knowsAboutCleanersInData() {
        assertEquals(
                data.cleaners.size(),
                controller.getCleanerController().getActiveCleaners().size(),
                "CleanerController should reflect the cleaners in SimulationData"
        );
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
        a.Position = id + ", 1";
        a.Dimension = "1, 1";
        return a;
    }
}
