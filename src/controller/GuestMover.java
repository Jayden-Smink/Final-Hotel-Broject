package controller;

import model.*;


/**
 * De bewegingsmotor van de simulatie. Berekenen frame-by-frame de X- en Y-verschuivingen
 * voor zowel gasten als schoonmakers op basis van hun snelheid, trappen en liften.
 */
public class GuestMover {
    private final SimulationData data;
    private final RouteCalculator routeCalculator;

    public GuestMover(SimulationData data, StairModel stairModel) {
        this.data = data;
        this.routeCalculator = new RouteCalculator(data, stairModel);
    }

    /**
     * Regelt de stapsgewijze verplaatsing van een hotelgast.
     */
    public void moveGuest(Guest g) {
        if (g.state != GuestState.WALKING && g.state != GuestState.EXITING_LIFT) {
            return;
        }

        int currentFloorY = (int) ((g.y + 10) / data.tileSize);
        int targetFloorY  = (int) ((g.targetY + 10) / data.tileSize);

        // SCENARIO A: De gast bevindt zich nog NIET op de juiste verdieping
        if (currentFloorY != targetFloorY && g.state != GuestState.EXITING_LIFT) {

            if (g.forceStairs || routeCalculator.isFasterByStairs(g.x, currentFloorY, targetFloorY)) {

                // --- OPTIE 1: De trap nemen ---
                double stairX = routeCalculator.getStairX();

                if (Math.abs(g.x - stairX) > g.speed) {
                    g.x += (g.x < stairX) ? g.speed : -g.speed;
                } else {
                    g.x = stairX;
                    double stairSpeed = g.speed * 0.5;
                    g.y += (g.y < g.targetY) ? stairSpeed : -stairSpeed;

                    if (Math.abs(g.y - targetFloorY * data.tileSize) < stairSpeed) {
                        g.y = targetFloorY * data.tileSize;
                        g.forceStairs = false; // reset zodra gast via trap op de goede verdieping staat
                        g.state = GuestState.EXITING_LIFT;
                    }
                }

            } else {

                // --- OPTIE 2: De lift nemen ---
                double elevatorX = data.horizontalOffset + 10 + Math.abs(g.personalOffset % 20);

                if (Math.abs(g.x - elevatorX) > g.speed) {
                    g.x += (g.x < elevatorX) ? g.speed : -g.speed;
                } else {
                    g.x = elevatorX;
                    g.state = GuestState.IN_QUEUE;
                    g.elevatorWaitTimer = 0;
                    g.waitingOnFloor = currentFloorY; // sla verdieping op bij het instappen van de wachtrij
                    data.elevator.waitingGuests.add(g);
                }
            }

        } else {
            // SCENARIO B: De gast is op de JUISTE verdieping, loop naar de kamer/faciliteit
            double finalTargetX = g.targetX;
            double finalTargetY = (targetFloorY * data.tileSize) + data.tileSize / 2.0;

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
                g.forceStairs = false;
                g.state = GuestState.AT_DESTINATION;
            }
        }
    }
}