package model;

import java.util.List;

public class StairModel {
    private final Area stairArea;
    private final double climbSpeedPerFloor = 100; // frames per floor, slower than elevator

    public StairModel(List<Area> areas) {
        this.stairArea = areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("TRAP"))
                .findFirst()
                .orElse(null);
    }

    public double getStairX(int horizontalOffset, int tileSize) {
        if (stairArea == null) return -1;
        return (stairArea.getPos()[0] * tileSize) + horizontalOffset;
    }

    public double calculateTravelTime(double guestX, int currentFloor, int targetFloor, int tileSize, int horizontalOffset) {
        if (stairArea == null) return Double.MAX_VALUE;

        double stairX = getStairX(horizontalOffset, tileSize);

        // Tijd om naar de trap te lopen
        double walkToStairs = Math.abs(guestX - stairX) / 2.0; // divide by guest speed

        // Tijd om naar vloer te gaan
        double floorDifference = Math.abs(targetFloor - currentFloor);
        double climbTime = floorDifference * climbSpeedPerFloor;

        return walkToStairs + climbTime;
    }
}