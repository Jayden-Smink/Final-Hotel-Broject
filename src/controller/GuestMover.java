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
    public void moveGuest(Guest guest) {
        if (guest.state != GuestState.WALKING && guest.state != GuestState.EXITING_LIFT) {
            return;
        }

        int currentFloorY = (int) ((guest.y + 10) / data.tileSize);
        int targetFloorY  = (int) ((guest.targetY + 10) / data.tileSize);

        // SCENARIO A: De gast bevindt zich nog NIET op de juiste verdieping
        if (currentFloorY != targetFloorY && guest.state != GuestState.EXITING_LIFT) {

            if (guest.forceStairs || routeCalculator.isFasterByStairs(guest.x, currentFloorY, targetFloorY)) {

                // --- OPTIE 1: De trap nemen ---
                double stairX = routeCalculator.getStairX();

                if (Math.abs(guest.x - stairX) > guest.speed) {
                    guest.x += (guest.x < stairX) ? guest.speed : -guest.speed;
                } else {
                    guest.x = stairX;
                    double stairSpeed = guest.speed * 0.5;
                    guest.y += (guest.y < guest.targetY) ? stairSpeed : -stairSpeed;

                    if (Math.abs(guest.y - targetFloorY * data.tileSize) < stairSpeed) {
                        guest.y = targetFloorY * data.tileSize;
                        guest.forceStairs = false; // reset zodra gast via trap op de goede verdieping staat
                        guest.state = GuestState.EXITING_LIFT;
                    }
                }

            } else {

                // --- OPTIE 2: De lift nemen ---
                double elevatorX = data.horizontalOffset + 10 + Math.abs(guest.personalOffset % 20);

                if (Math.abs(guest.x - elevatorX) > guest.speed) {
                    guest.x += (guest.x < elevatorX) ? guest.speed : -guest.speed;
                } else {
                    guest.x = elevatorX;
                    guest.state = GuestState.IN_QUEUE;
                    guest.elevatorWaitTimer = 0;
                    guest.waitingOnFloor = currentFloorY; // sla verdieping op bij het instappen van de wachtrij
                    data.elevator.waitingGuests.add(guest);
                }
            }

        } else {
            // SCENARIO B: De gast is op de JUISTE verdieping, loop naar de kamer/faciliteit
            double finalTargetX = guest.targetX;
            double finalTargetY = (targetFloorY * data.tileSize) + data.tileSize / 2.0;

            if (Math.abs(guest.x - finalTargetX) > guest.speed) {
                guest.x += (guest.x < finalTargetX) ? guest.speed : -guest.speed;

                if (Math.abs(guest.y - finalTargetY) > guest.speed) {
                    guest.y += (guest.y < finalTargetY) ? guest.speed : -guest.speed;
                }
            } else if (Math.abs(guest.y - guest.targetY) > guest.speed) {
                guest.y += (guest.y < guest.targetY) ? guest.speed : -guest.speed;
            } else {
                guest.x = finalTargetX;
                guest.y = guest.targetY;
                guest.forceStairs = false;
                guest.state = GuestState.AT_DESTINATION;
            }
        }
    }
}