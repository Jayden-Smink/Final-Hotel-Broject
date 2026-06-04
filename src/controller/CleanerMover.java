package controller;

import model.Cleaner;
import model.SimulationData;
import model.StairModel;

/**
 * Verantwoordelijk voor de fysieke pixel-verplaatsing van de schoonmaker via de trappen.
 */
public class CleanerMover {
    private final SimulationData data;
    private final RouteCalculator routeCalculator;

    public CleanerMover(SimulationData data, StairModel stairModel) {
        this.data = data;
        this.routeCalculator = new RouteCalculator(data, stairModel);
    }

    /**
     * Regelt de stapsgewijze verplaatsing van de schoonmaker.
     * NB: De schoonmaker gebruikt NOOIT de lift, altijd de trap.
     */
    public void moveCleaner(Cleaner cleaner) {
        int currentFloorY = (int) ((cleaner.y + 10) / data.tileSize);
        int targetFloorY  = (int) ((cleaner.targetY + 10) / data.tileSize);

        if (currentFloorY != targetFloorY) {
            double stairX = routeCalculator.getStairX();

            if (Math.abs(cleaner.x - stairX) > cleaner.speed) {
                cleaner.x += (cleaner.x < stairX) ? cleaner.speed : -cleaner.speed;
            } else {
                cleaner.x = stairX;
                double stairSpeed = cleaner.speed * 0.5;
                cleaner.y += (cleaner.y < cleaner.targetY) ? stairSpeed : -stairSpeed;
            }
        } else {
            if (Math.abs(cleaner.x - cleaner.targetX) > cleaner.speed) {
                cleaner.x += (cleaner.x < cleaner.targetX) ? cleaner.speed : -cleaner.speed;
            } else if (Math.abs(cleaner.y - cleaner.targetY) > cleaner.speed) {
                cleaner.y += (cleaner.y < cleaner.targetY) ? cleaner.speed : -cleaner.speed;
            } else {
                cleaner.x = cleaner.targetX;
                cleaner.y = cleaner.targetY;
            }
        }
    }
}