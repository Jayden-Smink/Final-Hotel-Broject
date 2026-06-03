package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StairModelTest {

    private static final int TILE = 60;
    private static final int H_OFFSET = 60;

    private Area makeTrap(String pos) {
        Area a = new Area();
        a.id = 1;
        a.AreaType = "TRAP";
        a.Position = pos;
        a.Dimension = "1,1";
        a.capacity = 100;
        return a;
    }

    private StairModel modelWith(Area... areas) {
        List<Area> list = new ArrayList<>();
        for (Area a : areas) list.add(a);
        return new StairModel(list);
    }

    // ── getStairX ─────────────────────────────────────────────────────────────

    @Test
    void getStairX_calculatesCorrectXWithOffset() {
        StairModel model = modelWith(makeTrap("4,0"));
        // Expected: 4 * 60 + 60 = 300
        assertEquals(300.0, model.getStairX(H_OFFSET, TILE), 0.001);
    }

    @Test
    void getStairX_returnsMinusOneWhenNoStairArea() {
        StairModel model = modelWith(); // empty list
        assertEquals(-1.0, model.getStairX(H_OFFSET, TILE), 0.001);
    }

    // ── calculateTravelTime ───────────────────────────────────────────────────

    @Test
    void calculateTravelTime_returnsMaxDoubleWhenNoStairs() {
        StairModel model = modelWith();
        double time = model.calculateTravelTime(100, 0, 3, TILE, H_OFFSET);
        assertEquals(Double.MAX_VALUE, time);
    }

    @Test
    void calculateTravelTime_returnsPositiveTimeWithStairs() {
        StairModel model = modelWith(makeTrap("3,0"));
        double time = model.calculateTravelTime(500, 0, 2, TILE, H_OFFSET);
        assertTrue(time > 0);
    }

    @Test
    void calculateTravelTime_increasesWithMoreFloors() {
        StairModel model = modelWith(makeTrap("3,0"));
        double oneFloor = model.calculateTravelTime(0, 0, 1, TILE, H_OFFSET);
        double twoFloors = model.calculateTravelTime(0, 0, 2, TILE, H_OFFSET);
        assertTrue(twoFloors > oneFloor);
    }

    @Test
    void calculateTravelTime_sameFloorShorterThanDifferentFloor() {
        StairModel model = modelWith(makeTrap("3,0"));
        double stairX = model.getStairX(H_OFFSET, TILE);
        double sameFloor = model.calculateTravelTime(stairX, 2, 2, TILE, H_OFFSET);
        double differentFloor = model.calculateTravelTime(stairX, 2, 5, TILE, H_OFFSET);
        assertTrue(sameFloor < differentFloor);
    }

    @Test
    void calculateTravelTime_guestAtStairXMinimisesWalkComponent() {
        StairModel model = modelWith(makeTrap("3,0"));
        double stairX = model.getStairX(H_OFFSET, TILE);

        // Guest right at the stair → walk time ≈ 0, cost is only climb time
        double atStair = model.calculateTravelTime(stairX, 0, 1, TILE, H_OFFSET);
        double awayFromStair = model.calculateTravelTime(stairX + 200, 0, 1, TILE, H_OFFSET);
        assertTrue(atStair < awayFromStair);
    }
}
