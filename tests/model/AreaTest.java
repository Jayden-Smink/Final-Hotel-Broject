package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaTest {

    private Area area;

    @BeforeEach
    void setUp() {
        area = new Area();
        area.id = 5;
        area.AreaType = "ROOM";
        area.Position = "3,2";
        area.Dimension = "2,1";
        area.Capacity = 3;
    }

    // ── getPos ────────────────────────────────────────────────────────────────

    @Test
    void getPos_returnsCorrectXAndY() {
        int[] pos = area.getPos();
        assertArrayEquals(new int[]{3, 2}, pos);
    }

    @Test
    void getPos_handlesWhitespaceAroundComma() {
        area.Position = " 5 , 10 ";
        assertArrayEquals(new int[]{5, 10}, area.getPos());
    }

    @Test
    void getPos_handlesZeroCoordinates() {
        area.Position = "0,0";
        assertArrayEquals(new int[]{0, 0}, area.getPos());
    }

    @Test
    void getPos_handlesLargeValues() {
        area.Position = "100,200";
        assertArrayEquals(new int[]{100, 200}, area.getPos());
    }

    // ── getDim ────────────────────────────────────────────────────────────────

    @Test
    void getDim_returnsCorrectWidthAndHeight() {
        assertArrayEquals(new int[]{2, 1}, area.getDim());
    }

    @Test
    void getDim_handlesWhitespace() {
        area.Dimension = " 4 , 3 ";
        assertArrayEquals(new int[]{4, 3}, area.getDim());
    }

    @Test
    void getDim_handlesLargeValues() {
        area.Dimension = "10,5";
        assertArrayEquals(new int[]{10, 5}, area.getDim());
    }

    // ── isFull ────────────────────────────────────────────────────────────────

    @Test
    void isFull_falseWhenEmpty() {
        assertFalse(area.isFull());
    }

    @Test
    void isFull_falseWhenBelowCapacity() {
        area.currentOccupants.add(1);
        area.currentOccupants.add(2);
        assertFalse(area.isFull());
    }

    @Test
    void isFull_trueWhenAtCapacity() {
        area.currentOccupants.add(1);
        area.currentOccupants.add(2);
        area.currentOccupants.add(3);
        assertTrue(area.isFull());
    }

    @Test
    void isFull_trueWhenOverCapacity() {
        area.currentOccupants.add(1);
        area.currentOccupants.add(2);
        area.currentOccupants.add(3);
        area.currentOccupants.add(4);
        assertTrue(area.isFull());
    }

    @Test
    void isFull_capacityOneBecomesFullAfterOneOccupant() {
        area.Capacity = 1;
        area.currentOccupants.add(99);
        assertTrue(area.isFull());
    }

    // ── defaults ──────────────────────────────────────────────────────────────

    @Test
    void defaultCapacityIsOne() {
        assertEquals(1, new Area().Capacity);
    }

    @Test
    void currentOccupantsInitiallyEmpty() {
        assertTrue(new Area().currentOccupants.isEmpty());
    }

    @Test
    void classificationIsNullByDefault() {
        assertNull(new Area().classification);
    }
}
