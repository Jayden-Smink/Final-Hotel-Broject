package controller;

import model.*;
import java.util.*;

public class ElevatorController {
    private final SimulationData data;

    //Constructor
    public ElevatorController(SimulationData data) {
        this.data = data;
    }

    public void update() {
        Elevator elevator = data.elevator;
        if (elevator == null) return;

        elevator.update();

        if (!elevator.isMoving) {
            int currentFloorY = (int) (elevator.curY / data.tileSize);

            // 1. Let guests exit
            elevator.passengers.removeIf(g -> {
                int targetFloorY = (int) (g.targetY / data.tileSize);
                if (targetFloorY == currentFloorY)
                {
                    g.state = GuestState.EXITING_LIFT;
                    g.x = elevator.curX + data.tileSize;
                    return true;
                }
                return false;
            });

            // 2. Let guests enter from the queue of the current floor
            Queue<Guest> queue = data.floorQueues.get(currentFloorY);
            while (queue != null && !queue.isEmpty() && elevator.passengers.size() < elevator.maxCapacity) {
                Guest g = queue.poll();
                if (g != null) {
                    g.state = GuestState.IN_LIFT;
                    elevator.passengers.add(g);
                }
            }

            determineElevatorTarget(elevator);
        }
    }

    private void determineElevatorTarget(Elevator elevator) {
        if (!elevator.passengers.isEmpty()) {
            int currentFloor = (int) (elevator.curY / data.tileSize);

            // Find the nearest floor among all passengers
            elevator.targetFloor = elevator.passengers.stream()
                    .mapToInt(g -> (int) (g.targetY / data.tileSize))
                    .min() // closest floor first (going up)
                    .orElse(currentFloor);

        } else {
            // Find the first floor with a waiting queue
            for (Map.Entry<Integer, Queue<Guest>> entry : data.floorQueues.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    elevator.targetFloor = entry.getKey();
                    break;
                }
            }
        }
    }
}