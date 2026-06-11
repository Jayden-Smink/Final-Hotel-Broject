package view;

import controller.CleanerController;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationRendererTest {

    private SimulationData data;
    private CleanerController cleanerController;
    private Graphics2D g2;

    @BeforeEach
    void setUp() {
        List<Area> areas = Arrays.asList(
                makeLobby(), makeRoom(1), makeRoom(2),
                makeArea("CINEMA", 3, 0),
                makeArea("RESTAURANT", 4, 1),
                makeArea("FITNESS", 5, 2),
                makeArea("LIFTSCHACHT", 0, 0),
                makeArea("TRAP", 6, 0)
        );
        data = new SimulationData(areas, 4, 1, 60, 60, 60, 60);
        data.cleaners.put(1, new Cleaner(1, 60, 25));
        cleanerController = new CleanerController(data, null);

        // Offscreen canvas — no display required
        BufferedImage canvas = new BufferedImage(1000, 2000, BufferedImage.TYPE_INT_ARGB);
        g2 = canvas.createGraphics();
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new SimulationRenderer(data, cleanerController));
    }

    @Test
    void constructor_withNullCleanerController_doesNotThrow() {
        assertDoesNotThrow(() -> new SimulationRenderer(data, null));
    }

    // ── render ────────────────────────────────────────────────────────────────

    @Test
    void render_emptyGuestsAndCleaners_doesNotThrow() {
        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withGuestWalking_doesNotThrow() {
        Guest guest = new Guest(1, 120, 180);
        guest.state = GuestState.WALKING;
        data.guests.put(1, guest);

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withGuestInLift_doesNotThrow() {
        Guest guest = new Guest(2, 60, 60);
        guest.state = GuestState.IN_LIFT;
        data.guests.put(2, guest);

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withGuestCheckingOut_doesNotThrow() {
        Guest guest = new Guest(3, 80, 300);
        guest.state = GuestState.WALKING;
        guest.isCheckingOut = true;
        data.guests.put(3, guest);

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withCleanerWalking_doesNotThrow() {
        Cleaner cleaner = data.cleaners.get(1);
        cleaner.state = CleanerState.WALKING_TO_ROOM;

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withCleanerCleaning_doesNotThrow() {
        Cleaner cleaner = data.cleaners.get(1);
        cleaner.state = CleanerState.CLEANING;
        cleaner.assignedRoomId = 1;

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withOccupiedRoom_doesNotThrow() {
        data.areas.get(1).currentOccupants.add(1);
        Guest guest = new Guest(1, 60, 60);
        guest.state = GuestState.IDLE;
        data.guests.put(1, guest);

        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withNullElevator_doesNotThrow() {
        data.elevator = null;
        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withNullGuestsMap_doesNotThrow() {
        data.guests = null;
        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_withNullAreasList_doesNotThrow() {
        data.areas = null;
        SimulationRenderer renderer = new SimulationRenderer(data, null);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    @Test
    void render_canBeCalledMultipleTimes_doesNotThrow() {
        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) renderer.render(g2, data);
        });
    }

    @Test
    void render_withMultipleGuests_doesNotThrow() {
        for (int i = 1; i <= 5; i++) {
            Guest g = new Guest(i, i * 30, i * 40);
            g.state = GuestState.WALKING;
            data.guests.put(i, g);
        }
        SimulationRenderer renderer = new SimulationRenderer(data, cleanerController);
        assertDoesNotThrow(() -> renderer.render(g2, data));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Area makeLobby() {
        Area a = new Area();
        a.id = 0;
        a.AreaType = "LOBBY";
        a.Position = "0, 8";
        a.Dimension = "6, 1";
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

    private Area makeArea(String type, int id, int row) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = id + ", " + row;
        a.Dimension = "1, 1";
        return a;
    }
}
