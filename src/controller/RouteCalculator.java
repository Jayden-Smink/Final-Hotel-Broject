package controller;

import model.*;

public class RouteCalculator {
    private final SimulationData data;
    private final StairModel stairModel;

    public RouteCalculator(SimulationData data, StairModel stairModel) {
        this.data = data;
        this.stairModel = stairModel;
    }

    public double calculateElevatorTime(double guestX, int currentFloor, int targetFloor) {
        Elevator elevator = data.elevator;
        if (elevator == null) return Double.MAX_VALUE;

        double walkToElevator = Math.abs(guestX - elevator.curX) / 2.0;
        double elevatorCurrentFloor = elevator.curY / data.tileSize;
        double waitTime = Math.abs(elevatorCurrentFloor - currentFloor) * 30;
        double rideTime = Math.abs(targetFloor - currentFloor) * 30;

        return walkToElevator + waitTime + rideTime;
    }

    public double calculateStairTime(double guestX, int currentFloor, int targetFloor) {
        return stairModel.calculateTravelTime(guestX, currentFloor, targetFloor, data.tileSize, data.horizontalOffset);
    }

    public boolean isFasterByStairs(double guestX, int currentFloor, int targetFloor) {
        return calculateStairTime(guestX, currentFloor, targetFloor)
                < calculateElevatorTime(guestX, currentFloor, targetFloor);
    }
    public double getStairX() {
        return stairModel.getStairX(data.horizontalOffset, data.tileSize);
    }
}