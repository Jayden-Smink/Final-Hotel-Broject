package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StairModelTest {

    private static final int TILE = 60;
    private static final int OFFSET = 60;

    private Area makeTrap(String pos) {
        Area a = new Area();
        a.id = 99;
        a.AreaType = "TRAP";
        a.Position = pos;
        a.Dimension = "1,1";
        a.Capacity = 100;
        return a;
    }

    private StairModel modelWith(Area... areas) {
        List<Area> list = new ArrayList<>();
        for (Area a : areas) list.add(a);
        return new StairModel(list);
    }

    // ── getStairX ─────────────────────────────────────────────────────────────

    @Test
    void getStairX_correctForTileX4() {
        StairModel m = modelWith(makeTrap("4,0"));
        assertEquals(4 * TILE + OFFSET, m.getStairX(OFFSET, TILE), 0.001);
    }

    @Test
    void getStairX_correctForTileX0() {
        StairModel m = modelWith(makeTrap("0,0"));
        assertEquals(OFFSET, m.getStairX(OFFSET, TILE), 0.001);
    }

    @Test
    void getStairX_returnsMinusOneWhenNoStair() {
        StairModel m = modelWith(); // empty
        assertEquals(-1.0, m.getStairX(OFFSET, TILE), 0.001);
    }

    @Test
    void getStairX_usesFirstTrapIfMultipleAreas() {
        Area other = new Area();
        other.AreaType = "ROOM";
        other.Position = "1,1";
        other.Dimension = "2,1";
        StairModel m = modelWith(other, makeTrap("3,0"));
        assertEquals(3 * TILE + OFFSET, m.getStairX(OFFSET, TILE), 0.001);
    }

    @Test
    void getStairX_isCaseInsensitive() {
        Area a = makeTrap("2,0");
        a.AreaType = "trap"; // lowercase
        StairModel m = modelWith(a);
        assertEquals(2 * TILE + OFFSET, m.getStairX(OFFSET, TILE), 0.001);
    }

    // ── calculateTravelTime ───────────────────────────────────────────────────

    @Test
    void calculateTravelTime_returnsMaxDoubleWhenNoStair() {
        StairModel m = modelWith();
        assertEquals(Double.MAX_VALUE, m.calculateTravelTime(0, 0, 3, TILE, OFFSET));
    }

    @Test
    void calculateTravelTime_returnsPositive() {
        StairModel m = modelWith(makeTrap("3,0"));
        assertTrue(m.calculateTravelTime(500, 0, 2, TILE, OFFSET) > 0);
    }

    @Test
    void calculateTravelTime_sameFloorOnlyWalkComponent() {
        StairModel m = modelWith(makeTrap("3,0"));
        double stairX = m.getStairX(OFFSET, TILE);
        // standing exactly at the stair, same floor → walk=0, climb=0
        assertEquals(0.0, m.calculateTravelTime(stairX, 2, 2, TILE, OFFSET), 0.001);
    }

    @Test
    void calculateTravelTime_increasesWithFloorDifference() {
        StairModel m = modelWith(makeTrap("3,0"));
        double one = m.calculateTravelTime(0, 0, 1, TILE, OFFSET);
        double two = m.calculateTravelTime(0, 0, 2, TILE, OFFSET);
        double three = m.calculateTravelTime(0, 0, 3, TILE, OFFSET);
        assertTrue(one < two && two < three);
    }

    @Test
    void calculateTravelTime_higherWalkDistanceIncreasesTime() {
        StairModel m = modelWith(makeTrap("3,0"));
        double near = m.calculateTravelTime(0, 0, 1, TILE, OFFSET);
        double far = m.calculateTravelTime(600, 0, 1, TILE, OFFSET);
        assertTrue(far > near);
    }

    @Test
    void calculateTravelTime_symmetricForGoingUpOrDown() {
        StairModel m = modelWith(makeTrap("3,0"));
        double up = m.calculateTravelTime(0, 0, 3, TILE, OFFSET);
        double down = m.calculateTravelTime(0, 3, 0, TILE, OFFSET);
        assertEquals(up, down, 0.001);
    }
}
