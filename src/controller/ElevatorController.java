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

        // Update wachttimers en verwijder gasten die te lang wachten
        updateWaitTimers(elevator);

        if (!elevator.isMoving) {
            int currentFloorY = (int) (elevator.curY / data.tileSize);

            // Laat passagiers uitstappen op de juiste verdieping
            for (int i = elevator.passengers.size() - 1; i >= 0; i--) {
                Guest g = elevator.passengers.get(i);
                int targetFloorY = (int) (g.targetY / data.tileSize);
                if (targetFloorY == currentFloorY) {
                    elevator.passengers.remove(i);
                    g.state = GuestState.EXITING_LIFT;
                    g.x = elevator.curX + data.tileSize;
                }
            }

            // Laat wachtende gasten op de huidige verdieping instappen
            for (int i = elevator.waitingGuests.size() - 1; i >= 0; i--) {
                if (elevator.passengers.size() >= elevator.maxCapacity) break;
                Guest g = elevator.waitingGuests.get(i);
                if (g.waitingOnFloor == currentFloorY) {
                    elevator.waitingGuests.remove(i);
                    g.elevatorWaitTimer = 0;
                    g.state = GuestState.IN_LIFT;
                    elevator.passengers.add(g);
                }
            }

            determineElevatorTarget(elevator);
        }
    }

    // Hoog de wachttimer op van elke wachtende gast. Geeft op na ELEVATOR_WAIT_TIMEOUT frames.
    private void updateWaitTimers(Elevator elevator) {
        for (int i = elevator.waitingGuests.size() - 1; i >= 0; i--) {
            Guest g = elevator.waitingGuests.get(i);
            g.elevatorWaitTimer++;

            if (g.elevatorWaitTimer >= data.guestSettings.getElevatorWaitTimeout()) {
                elevator.waitingGuests.remove(i);
                g.elevatorWaitTimer = 0;
                g.forceStairs = true;
                g.state = GuestState.WALKING;

                if (logPanel != null) logPanel.addLog("😤 Gast " + g.id + " geeft op en neemt de trap.");
            }
        }
    }

    private void determineElevatorTarget(Elevator elevator) {
        int currentFloor = (int) (elevator.curY / data.tileSize);

        // Prioriteit 1: breng passagiers naar hun verdieping
        if (!elevator.passengers.isEmpty()) {
            int target = currentFloor;
            for (int i = 0; i < elevator.passengers.size(); i++) {
                int floorY = (int) (elevator.passengers.get(i).targetY / data.tileSize);
                if (i == 0 || floorY < target) {
                    target = floorY;
                }
            }
            elevator.targetFloor = target;
            return;
        }

        // Prioriteit 2: ga naar de verdieping waar iemand staat te wachten
        if (!elevator.waitingGuests.isEmpty()) {
            int floorY = (int) ((elevator.waitingGuests.get(0).y + 10) / data.tileSize);
            elevator.targetFloor = floorY;
        }
    }
}