package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class GodzillaControllerTest {

    /**
     * Handmatige nep-implementatie van IDestructionStrategy.
     * Houdt bij welke areas vernietigd of geüpdatet zijn.
     */
    static class FakeDestructionStrategy implements IDestructionStrategy {
        Area lastDestroyed = null;
        Area lastUpdated = null;
        int updateCallCount = 0;

        @Override
        public void destroy(Area area) {
            lastDestroyed = area;
            area.isDestroyed = true;
        }

        @Override
        public void update(Area area) {
            lastUpdated = area;
            updateCallCount++;
        }
    }

    private GodzillaController controller;
    private SimulationData testData;
    private FakeDestructionStrategy fakeStrategy;
    private Area targetArea;

    @BeforeEach
    void setUp() {
        targetArea = new Area();
        targetArea.AreaType = "ROOM";
        targetArea.Position = "0, 2";
        targetArea.Dimension = "2, 2";

        ArrayList<Area> areas = new ArrayList<>();
        areas.add(targetArea);

        // SimulationData vereist een volledige constructor — dummy waarden voor irrelevante velden
        testData = new SimulationData(areas, 4, 1, 60, 60, 60, 60);
        testData.guests = new ConcurrentHashMap<>();

        fakeStrategy = new FakeDestructionStrategy();

        // LogPanel is null-safe in de controller
        controller = new GodzillaController(testData, null, fakeStrategy);
    }

    @Test
    void testUpdate_CallsStrategyUpdateForBurningAreas() {
        controller.activate();
        targetArea.isOnFire = true;

        controller.update();

        assertEquals(targetArea, fakeStrategy.lastUpdated, "Strategy.update() moet aangeroepen worden voor brandende areas");
        assertEquals(1, fakeStrategy.updateCallCount);
    }

    @Test
    void testUpdate_DoesNothingWhenNotActive() {
        targetArea.isOnFire = true;

        controller.update(); // niet geactiveerd

        assertEquals(0, fakeStrategy.updateCallCount, "Geen updates als Godzilla niet actief is");
    }

    @Test
    void testHandleGuestsInDestroyedAreas_KillsGuests() {
        controller.activate();

        Guest guest = new Guest(7, 20.0, 80.0);
        testData.guests.put(guest.id, guest);

        targetArea.isDestroyed = true;

        controller.update();

        assertTrue(guest.isDead, "Gast in een verwoest gebied moet dood zijn");
    }

    @Test
    void testHandleGuestsInDestroyedAreas_DoesNotKillLivingGuestOutsideArea() {
        controller.activate();

        // Gast ver buiten het targetArea (position 0,2 met tileSize 60 → area op x=0..120, y=120..240)
        Guest guest = new Guest(3, 999.0, 999.0);
        testData.guests.put(guest.id, guest);

        targetArea.isDestroyed = true;

        controller.update();

        assertFalse(guest.isDead, "Gast buiten verwoest gebied mag niet doodgaan");
    }
}