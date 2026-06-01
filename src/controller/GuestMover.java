package controller;

import model.*;
import model.Cleaner;

public class GuestMover {
    private final SimulationData data;
    private final RouteCalculator routeCalculator;

    public GuestMover(SimulationData data, StairModel stairModel) {
        this.data = data;
        this.routeCalculator = new RouteCalculator(data, stairModel);
    }

    public void moveGuest(Guest g) {
        if (g.state != GuestState.WALKING && g.state != GuestState.EXITING_LIFT) {
            return;
        }

        int currentFloorY = (int) ((g.y + 10) / data.tileSize);
        int targetFloorY = (int) ((g.targetY + 10) / data.tileSize);

        // SCENARIO A: Gast moet naar een andere verdieping
        if (currentFloorY != targetFloorY && g.state != GuestState.EXITING_LIFT) {

            if (routeCalculator.isFasterByStairs(g.x, currentFloorY, targetFloorY)) {
                // Take stairs
                double stairX = routeCalculator.getStairX();

                if (Math.abs(g.x - stairX) > g.speed) {
                    g.x += (g.x < stairX) ? g.speed : -g.speed;
                } else {
                    // At stairs, move up/down slowly
                    g.x = stairX;
                    double stairSpeed = g.speed * 0.5; // stairs are slower
                    g.y += (g.y < g.targetY) ? stairSpeed : -stairSpeed;

                    if (Math.abs(g.y - targetFloorY * data.tileSize) < stairSpeed) {
                        g.y = targetFloorY * data.tileSize;
                        g.state = GuestState.EXITING_LIFT; // reuse existing state
                    }
                }
            } else {
                // Take elevator (existing logic)
                double elevatorX = data.horizontalOffset + 10 + Math.abs(g.personalOffset % 20);

                if (Math.abs(g.x - elevatorX) > g.speed) {
                    g.x += (g.x < elevatorX) ? g.speed : -g.speed;
                } else {
                    g.x = elevatorX;
                    g.state = GuestState.IN_QUEUE;

                    if (data.floorQueues.containsKey(currentFloorY)) {
                        data.floorQueues.get(currentFloorY).add(g);
                    }
                }
            }

        } else {
            // SCENARIO B: Gast is op de juiste verdieping
            double finalTargetX = g.targetX;
            double finalTargetY = (targetFloorY * data.tileSize) + data.tileSize/2.0;

            if (Math.abs(g.x - finalTargetX) > g.speed) {
                g.x += (g.x < finalTargetX) ? g.speed : -g.speed;

                if (Math.abs(g.y - finalTargetY) > g.speed) {
                    g.y += (g.y < finalTargetY) ? g.speed : -g.speed;
                }
            } else if (Math.abs(g.y - g.targetY) > g.speed) {
                g.y += (g.y < g.targetY) ? g.speed : -g.speed;
            } else {
                g.x = finalTargetX;
                g.y = g.targetY;
                g.state = GuestState.AT_DESTINATION;
            }
        }
    }

    public void moveCleaner(Cleaner cleaner) {
        // Cleaner always uses stairs, never elevator
        int currentFloorY = (int) ((cleaner.y + 10) / data.tileSize);
        int targetFloorY = (int) ((cleaner.targetY + 10) / data.tileSize);

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
            // Same floor logic
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