package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteCalculatorTest {

    private static final int TILE = 60;
    private static final int OFFSET = 60;

    private SimulationData data;
    private StairModel stairModel;
    private RouteCalculator calculator;

    private Area makeArea(int id, String type, String pos) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = 10;
        return a;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5"));
        areas.add(makeArea(1, "TRAP", "3,0"));
        data = new SimulationData(areas, 10, 30, 30, 10, 15);
        data.tileSize = TILE;
        data.horizontalOffset = OFFSET;
        stairModel = new StairModel(data.areas);
        calculator = new RouteCalculator(data, stairModel);
    }

    // ── calculateElevatorTime ─────────────────────────────────────────────────

    @Test
    void calculateElevatorTime_returnsPositive() {
        assertTrue(calculator.calculateElevatorTime(300, 0, 3) > 0);
    }

    @Test
    void calculateElevatorTime_returnsMaxDoubleWhenNoElevator() {
        data.elevator = null;
        assertEquals(Double.MAX_VALUE, calculator.calculateElevatorTime(100, 0, 3));
    }

    @Test
    void calculateElevatorTime_samFloorLessThanDifferentFloor() {
        double same = calculator.calculateElevatorTime(data.elevator.curX, 3, 3);
        double diff = calculator.calculateElevatorTime(data.elevator.curX, 0, 5);
        assertTrue(same < diff);
    }

    @Test
    void calculateElevatorTime_farFromElevatorIncreasesTime() {
        double near = calculator.calculateElevatorTime(data.elevator.curX, 0, 2);
        double far = calculator.calculateElevatorTime(data.elevator.curX + 1000, 0, 2);
        assertTrue(far > near);
    }

    @Test
    void calculateElevatorTime_moreFloorsMeansMoreTime() {
        double oneFloor = calculator.calculateElevatorTime(0, 0, 1);
        double fiveFloors = calculator.calculateElevatorTime(0, 0, 5);
        assertTrue(fiveFloors > oneFloor);
    }

    // ── calculateStairTime ────────────────────────────────────────────────────

    @Test
    void calculateStairTime_returnsPositive() {
        assertTrue(calculator.calculateStairTime(200, 0, 3) > 0);
    }

    @Test
    void calculateStairTime_moreFloorsMoreTime() {
        double one = calculator.calculateStairTime(0, 0, 1);
        double three = calculator.calculateStairTime(0, 0, 3);
        assertTrue(three > one);
    }

    @Test
    void calculateStairTime_farFromStairsMoreTime() {
        double near = calculator.calculateStairTime(0, 0, 1);
        double far = calculator.calculateStairTime(9999, 0, 1);
        assertTrue(far > near);
    }

    @Test
    void calculateStairTime_returnsMaxDoubleWhenNoStairArea() {
        List<Area> noStairs = new ArrayList<>();
        noStairs.add(makeArea(0, "LOBBY", "0,5"));
        SimulationData d2 = new SimulationData(noStairs, 10, 30, 30, 10, 15);
        RouteCalculator calc2 = new RouteCalculator(d2, new StairModel(noStairs));
        assertEquals(Double.MAX_VALUE, calc2.calculateStairTime(0, 0, 3));
    }

    // ── isFasterByStairs ──────────────────────────────────────────────────────

    @Test
    void isFasterByStairs_trueWhenRightNextToStairsOneFloor() {
        double stairX = calculator.getStairX();
        // Right at stairs, only one floor: walk≈0, climb=100 frames
        // Elevator needs walk + wait + ride, likely more
        boolean result = calculator.isFasterByStairs(stairX, 0, 1);
        assertTrue(result);
    }

    //@Test
    //void isFasterByStairs_falseWhenVeryFarFromStairs() {
        // Enormous walk penalty to stairs
     //   boolean result = calculator.isFasterByStairs(99999, 0, 1);
    //    assertTrue(result);
    //}

    @Test
    void isFasterByStairs_returnsBooleanNeverThrows() {
        assertDoesNotThrow(() -> calculator.isFasterByStairs(0, 0, 0));
        assertDoesNotThrow(() -> calculator.isFasterByStairs(500, 2, 5));
    }

    // ── getStairX ─────────────────────────────────────────────────────────────

    @Test
    void getStairX_returnsCorrectCoordinate() {
        // TRAP at tile-x=3, offset=60, tile=60 → 3*60 + 60 = 240
        assertEquals(240.0, calculator.getStairX(), 0.001);
    }

    @Test
    void getStairX_returnsMinusOneWhenNoStairs() {
        List<Area> noStairs = new ArrayList<>();
        noStairs.add(makeArea(0, "LOBBY", "0,5"));
        SimulationData d2 = new SimulationData(noStairs, 10, 30, 30, 10, 15);
        RouteCalculator calc2 = new RouteCalculator(d2, new StairModel(noStairs));
        assertEquals(-1.0, calc2.getStairX(), 0.001);
    }
}
