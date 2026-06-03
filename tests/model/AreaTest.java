package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaTest {

    private Area area;

    @BeforeEach
    void setUp() {
        area = new Area();
        area.id = 1;
        area.AreaType = "ROOM";
        area.Position = "3,2";
        area.Dimension = "2,1";
        area.capacity = 2;
    }

    @Test
    void getPos_returnsCorrectCoordinates() {
        int[] pos = area.getPos();
        assertEquals(3, pos[0]);
        assertEquals(2, pos[1]);
    }

    @Test
    void getDim_returnsCorrectDimensions() {
        int[] dim = area.getDim();
        assertEquals(2, dim[0]);
        assertEquals(1, dim[1]);
    }

    @Test
    void getPos_handlesWhitespace() {
        area.Position = " 5 , 10 ";
        int[] pos = area.getPos();
        assertEquals(5, pos[0]);
        assertEquals(10, pos[1]);
    }

    @Test
    void isFull_returnsFalseWhenEmpty() {
        assertFalse(area.isFull());
    }

    @Test
    void isFull_returnsFalseWhenPartiallyFilled() {
        area.currentOccupants.add(42);
        assertFalse(area.isFull());
    }

    @Test
    void isFull_returnsTrueWhenAtCapacity() {
        area.currentOccupants.add(1);
        area.currentOccupants.add(2);
        assertTrue(area.isFull());
    }

    @Test
    void isFull_returnsTrueWhenOverCapacity() {
        area.currentOccupants.add(1);
        area.currentOccupants.add(2);
        area.currentOccupants.add(3);
        assertTrue(area.isFull());
    }

    @Test
    void defaultCapacityIsOne() {
        Area fresh = new Area();
        assertEquals(1, fresh.capacity);
    }

    @Test
    void currentOccupantsStartsEmpty() {
        Area fresh = new Area();
        assertTrue(fresh.currentOccupants.isEmpty());
    }
}
