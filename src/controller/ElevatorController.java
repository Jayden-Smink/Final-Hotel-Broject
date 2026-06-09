package controller;

import model.*;
import view.LogPanel;

public class ElevatorController {
    private final SimulationData data;
    private final LogPanel logPanel;

    public ElevatorController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
    }

    public void update() {
        Elevator elevator = data.elevator;
        if (elevator == null) return;

        elevator.update();

        updateWaitTimers(elevator);

        if (!elevator.isMoving) {
            int currentFloorY = (int) (elevator.curY / data.tileSize);

            // Laat passagiers uitstappen op de juiste verdieping
            for (int i = elevator.passengers.size() - 1; i >= 0; i--) {
                Guest passenger = elevator.passengers.get(i);
                int targetFloorY = (int) (passenger.targetY / data.tileSize);
                if (targetFloorY == currentFloorY) {
                    elevator.passengers.remove(i);
                    passenger.state = GuestState.EXITING_LIFT;
                    passenger.x = elevator.curX + data.tileSize;
                }
            }

            // Laat wachtende gasten op de huidige verdieping instappen
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

            determineElevatorTarget(elevator);
        }
    }

    private void updateWaitTimers(Elevator elevator) {
        for (int i = elevator.waitingGuests.size() - 1; i >= 0; i--) {
            Guest waitingGuest = elevator.waitingGuests.get(i);
            waitingGuest.elevatorWaitTimer++;

            if (waitingGuest.elevatorWaitTimer >= data.guestSettings.getElevatorWaitTimeout()) {
                elevator.waitingGuests.remove(i);
                data.guests.remove(waitingGuest.id);

                if (logPanel != null) logPanel.addLog("💀 Gast " + waitingGuest.id + " is gestorven in de liftwachtrij.");
            }
        }
    }

    private void determineElevatorTarget(Elevator elevator) {
        int currentFloor = (int) (elevator.curY / data.tileSize);

        // Prioriteit 1: breng passagiers naar hun verdieping
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

        // Prioriteit 2: ga naar de verdieping waar iemand staat te wachten
        if (!elevator.waitingGuests.isEmpty()) {
            Guest firstWaiting = elevator.waitingGuests.get(0);
            elevator.targetFloor = (int) ((firstWaiting.waitingOnFloor));
        }
    }
}