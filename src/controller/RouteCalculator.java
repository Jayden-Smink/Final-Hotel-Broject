package controller;

import model.*;

/**
 * Verantwoordelijk voor het berekenen en vergelijken van routes.
 * Bepaalt of een gast sneller via de trap of via de lift op zijn doelverdieping aankomt.
 */
public class RouteCalculator {
    private final SimulationData data;
    private final StairModel stairModel;

    public RouteCalculator(SimulationData data, StairModel stairModel) {
        this.data = data;
        this.stairModel = stairModel;
    }

    /**
     * Schat de totale tijd (in frames/ticks) die nodig is om via de lift te reizen.
     * Rekentijd = (lopen naar liftschacht) + (wachten tot de lift er is) + (de liftrit zelf).
     */
    public double calculateElevatorTime(double guestX, int currentFloor, int targetFloor) {
        Elevator elevator = data.elevator;
        if (elevator == null) return Double.MAX_VALUE; // Geen lift? Dan is de reistijd oneindig hoog

        // 1. Loop-tijd naar de lift toe (op basis van de X-as)
        double walkToElevator = Math.abs(guestX - elevator.curX) / 2.0;

        // 2. Wachttijd: bereken waar de lift nu staat en hoe lang hij erover doet om naar de gast te komen
        double elevatorCurrentFloor = elevator.curY / data.tileSize;
        double waitTime = Math.abs(elevatorCurrentFloor - currentFloor) * 30; // 30 frames per verdieping

        // 3. Rittijd: hoe lang duurt de rit van de huidige verdieping naar de doelverdieping
        double rideTime = Math.abs(targetFloor - currentFloor) * 30;

        return walkToElevator + waitTime + rideTime;
    }

    /**
     * Vraagt aan het StairModel hoe lang het duurt om via de trap te lopen.
     */
    public double calculateStairTime(double guestX, int currentFloor, int targetFloor) {
        return stairModel.calculateTravelTime(guestX, currentFloor, targetFloor, data.tileSize, data.horizontalOffset);
    }

    /**
     * Vergelijkt de berekende tijden en geeft 'true' terug als de trap sneller is dan de lift.
     */
    public boolean isFasterByStairs(double guestX, int currentFloor, int targetFloor) {
        return calculateStairTime(guestX, currentFloor, targetFloor)
                < calculateElevatorTime(guestX, currentFloor, targetFloor);
    }

    /**
     * Haalt de exacte horizontale (X) coördinaat van de trap op uit het StairModel.
     */
    public double getStairX() {
        return stairModel.getStairX(data.horizontalOffset, data.tileSize);
    }
}