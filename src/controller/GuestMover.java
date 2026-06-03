package controller;

import model.*;
import model.Cleaner;

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
        // Alleen beweging berekenen als de gast de status WALKING heeft of net de lift verlaat
        if (g.state != GuestState.WALKING && g.state != GuestState.EXITING_LIFT) {
            return;
        }

        // Bereken de huidige verdieping en de doelverdieping (omgerekend van pixels naar grid-index)
        int currentFloorY = (int) ((g.y + 10) / data.tileSize);
        int targetFloorY = (int) ((g.targetY + 10) / data.tileSize);

        // SCENARIO A: De gast bevindt zich nog NIET op de juiste verdieping
        if (currentFloorY != targetFloorY && g.state != GuestState.EXITING_LIFT) {

            // Keuzemoment: Is het sneller om via de trap te gaan?
            if (routeCalculator.isFasterByStairs(g.x, currentFloorY, targetFloorY)) {

                // --- OPTIE 1: De trap nemen ---
                double stairX = routeCalculator.getStairX();

                // Stap 1a: Loop horizontaal totdat de gast recht voor de trap staat
                if (Math.abs(g.x - stairX) > g.speed) {
                    g.x += (g.x < stairX) ? g.speed : -g.speed;
                } else {
                    // Stap 1b: Gast staat voor de trap. Beweeg nu verticaal (omhoog of omlaag)
                    g.x = stairX;
                    double stairSpeed = g.speed * 0.5; // Halveer de snelheid, want traplopen is zwaarder
                    g.y += (g.y < g.targetY) ? stairSpeed : -stairSpeed;

                    // Stap 1c: Check of de gast de nieuwe verdieping heeft bereikt
                    if (Math.abs(g.y - targetFloorY * data.tileSize) < stairSpeed) {
                        g.y = targetFloorY * data.tileSize;
                        g.state = GuestState.EXITING_LIFT; // Misbruik deze status om de loop-modus weer te triggeren
                    }
                }
            } else {

                // --- OPTIE 2: De lift nemen ---
                // Bereken de X-positie van de liftwachtrij (inclusief persoonlijke offset tegen poppetjes-overlapping)
                double elevatorX = data.horizontalOffset + 10 + Math.abs(g.personalOffset % 20);

                // Stap 2a: Loop horizontaal richting de liftschacht
                if (Math.abs(g.x - elevatorX) > g.speed) {
                    g.x += (g.x < elevatorX) ? g.speed : -g.speed;
                } else {
                    // Stap 2b: Aangekomen bij de lift. Sluit aan in de virtuele wachtrij van deze verdieping
                    g.x = elevatorX;
                    g.state = GuestState.IN_QUEUE;

                    if (data.floorQueues.containsKey(currentFloorY)) {
                        data.floorQueues.get(currentFloorY).add(g);
                    }
                }
            }

        } else {
            // SCENARIO B: De gast is op de JUISTE verdieping. Loop nu naar de specifieke kamer/faciliteit
            double finalTargetX = g.targetX;
            double finalTargetY = (targetFloorY * data.tileSize) + data.tileSize/2.0;

            // Stap 1: Beweeg eerst horizontaal (X-as) naar de kamer toe
            if (Math.abs(g.x - finalTargetX) > g.speed) {
                g.x += (g.x < finalTargetX) ? g.speed : -g.speed;

                // Kleine correctie voor de Y-as tijdens het lopen (indien nodig)
                if (Math.abs(g.y - finalTargetY) > g.speed) {
                    g.y += (g.y < finalTargetY) ? g.speed : -g.speed;
                }
                // Stap 2: Corrigeer de laatste pixels op de Y-as
            } else if (Math.abs(g.y - g.targetY) > g.speed) {
                g.y += (g.y < g.targetY) ? g.speed : -g.speed;
                // Stap 3: Exacte eindbestemming bereikt! Zet de status om naar AT_DESTINATION
            } else {
                g.x = finalTargetX;
                g.y = g.targetY;
                g.state = GuestState.AT_DESTINATION;
            }
        }
    }

    /**
     * Regelt de stapsgewijze verplaatsing van de schoonmaker.
     * NB: De schoonmaker gebruikt NOOIT de lift, altijd de trap.
     */
    public void moveCleaner(Cleaner cleaner) {
        int currentFloorY = (int) ((cleaner.y + 10) / data.tileSize);
        int targetFloorY = (int) ((cleaner.targetY + 10) / data.tileSize);

        // Moet de schoonmaker naar een andere verdieping?
        if (currentFloorY != targetFloorY) {
            double stairX = routeCalculator.getStairX();

            // Loop eerst horizontaal naar de trap toe
            if (Math.abs(cleaner.x - stairX) > cleaner.speed) {
                cleaner.x += (cleaner.x < stairX) ? cleaner.speed : -cleaner.speed;
            } else {
                // Loop verticaal over de trap (op halve snelheid)
                cleaner.x = stairX;
                double stairSpeed = cleaner.speed * 0.5;
                cleaner.y += (cleaner.y < cleaner.targetY) ? stairSpeed : -stairSpeed;
            }
        } else {
            // Schoonmaker is op de juiste verdieping: loop rechtstreeks naar de doellocatie
            if (Math.abs(cleaner.x - cleaner.targetX) > cleaner.speed) {
                cleaner.x += (cleaner.x < cleaner.targetX) ? cleaner.speed : -cleaner.speed;
            } else if (Math.abs(cleaner.y - cleaner.targetY) > cleaner.speed) {
                cleaner.y += (cleaner.y < cleaner.targetY) ? cleaner.speed : -cleaner.speed;
            } else {
                // Exacte bestemming (vieze kamer of lobby) bereikt
                cleaner.x = cleaner.targetX;
                cleaner.y = cleaner.targetY;
            }
        }
    }
}