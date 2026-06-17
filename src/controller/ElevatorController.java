package controller;

import model.*;
import view.LogPanel;

/**
 * Beheert de lift: beweging, in- en uitstappen van gasten,
 * en het bepalen van de volgende bestemming (target floor).
 */
public class ElevatorController {
    private final SimulationData data;
    private final LogPanel logPanel;

    public ElevatorController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    /**
     * Hoofd-update loop voor de lift. Wordt elke tick aangeroepen.
     */
    public void update() {
        Elevator elevator = data.elevator;
        if (elevator == null) return;

        // Beweegt de lift richting zijn huidige targetFloor
        elevator.update();

        // Houdt bij hoe lang gasten al in de wachtrij staan
        updateWaitTimers(elevator);

        // In- en uitstappen mag alleen als de lift stilstaat op een verdieping
        if (!elevator.isMoving) {
            int currentFloorY = (int) (elevator.curY / data.tileSize);

            // Laat passagiers uitstappen op de juiste verdieping.
            // Achterwaarts itereren omdat we tijdens het lopen items uit de lijst verwijderen.
            for (int i = elevator.passengers.size() - 1; i >= 0; i--) {
                Guest passenger = elevator.passengers.get(i);
                int targetFloorY = (int) (passenger.targetY / data.tileSize);
                if (targetFloorY == currentFloorY) {
                    elevator.passengers.remove(i);
                    passenger.state = GuestState.EXITING_LIFT;
                    passenger.x = elevator.curX + data.tileSize;
                }
            }

            // Laat wachtende gasten op de huidige verdieping instappen,
            // zolang de lift nog niet vol is.
            for (int i = elevator.waitingGuests.size() - 1; i >= 0; i--) {
                if (elevator.passengers.size() >= elevator.maxCapacity) break;
                Guest waitingGuest = elevator.waitingGuests.get(i);
                if (waitingGuest.waitingOnFloor == currentFloorY) {
                    elevator.waitingGuests.remove(i);
                    waitingGuest.elevatorWaitTimer = 0;
                    waitingGuest.state = GuestState.IN_LIFT;
                    elevator.passengers.add(waitingGuest);
                }
            }

            // Bepaal waar de lift hierna naartoe moet
            determineElevatorTarget(elevator);
        }
    }

    /**
     * Verhoogt de wachttimer van elke wachtende gast.
     * Gasten die te lang wachten "sterven" en worden verwijderd.
     */
    private void updateWaitTimers(Elevator elevator) {
        for (int i = elevator.waitingGuests.size() - 1; i >= 0; i--) {
            Guest waitingGuest = elevator.waitingGuests.get(i);
            waitingGuest.elevatorWaitTimer++;

            // Timeout bereikt: gast geeft op en sterft in de wachtrij
            if (waitingGuest.elevatorWaitTimer >= data.guestSettings.getElevatorWaitTimeout()) {
                elevator.waitingGuests.remove(i);
                waitingGuest.state = GuestState.FALLING;

                if (logPanel != null) logPanel.addLog("💀 Gast " + waitingGuest.id + " is gestorven in de liftwachtrij.");
            }
        }
    }

    /**
     * Bepaalt de volgende bestemming (targetFloor) van de lift.
     * Prioriteit 1: passagiers afleveren. Prioriteit 2: nieuwe wachtenden ophalen.
     */
    private void determineElevatorTarget(Elevator elevator) {
        int currentFloor = (int) (elevator.curY / data.tileSize);

        // Prioriteit 1: breng passagiers naar hun verdieping (laagste doel eerst)
        if (!elevator.passengers.isEmpty()) {
            int target = currentFloor;
            for (int i = 0; i < elevator.passengers.size(); i++) {
                Guest passenger = elevator.passengers.get(i);
                int floorY = (int) (passenger.targetY / data.tileSize);
                if (i == 0 || floorY < target) {
                    target = floorY;
                }
            }
            elevator.targetFloor = target;
            return;
        }

        // Prioriteit 2: ga naar de verdieping waar de eerste wachtende gast staat
        if (!elevator.waitingGuests.isEmpty()) {
            Guest firstWaiting = elevator.waitingGuests.get(0);
            elevator.targetFloor = (int) ((firstWaiting.waitingOnFloor));
        }
    }
}