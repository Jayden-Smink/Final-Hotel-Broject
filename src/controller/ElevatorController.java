package controller;

import model.*;
import java.util.*;

/**
 * Beheert de logica van de lift: het in- en uitstappen van gasten
 * en het bepalen van de volgende verdieping.
 */
public class ElevatorController {
    private final SimulationData data;

    public ElevatorController(SimulationData data) {
        this.data = data;
    }

    /**
     * De hoofd-update loop van de lift. Wordt elke frame aangeroepen.
     */
    public void update() {
        Elevator elevator = data.elevator;
        if (elevator == null) return;

        // Update de interne status van de lift (zoals de huidige Y-positie)
        elevator.update();

        // Alleen acties uitvoeren als de lift stilstaat op een verdieping
        if (!elevator.isMoving) {
            // Bereken op welke verdieping de lift nu staat op basis van de pixels (Y-as)
            int currentFloorY = (int) (elevator.curY / data.tileSize);

            // 1. Laat gasten uitstappen: controleer of hun doelverdieping overeenkomt met de huidige verdieping
            elevator.passengers.removeIf(g -> {
                int targetFloorY = (int) (g.targetY / data.tileSize);
                if (targetFloorY == currentFloorY) {
                    g.state = GuestState.EXITING_LIFT;
                    g.x = elevator.curX + data.tileSize; // Zet de gast net buiten de lift op de gang
                    return true; // Verwijder de gast uit de lift-lijst
                }
                return false;
            });

            // 2. Laat wachtende gasten instappen vanaf de wachtrij van de huidige verdieping
            Queue<Guest> queue = data.floorQueues.get(currentFloorY);
            // Blijf gasten toevoegen zolang de rij niet leeg is en de lift zijn maximale capaciteit nog niet heeft bereikt
            while (queue != null && !queue.isEmpty() && elevator.passengers.size() < elevator.maxCapacity) {
                Guest g = queue.poll(); // Haal de voorste gast uit de wachtrij
                if (g != null) {
                    g.state = GuestState.IN_LIFT;
                    elevator.passengers.add(g);
                }
            }

            // Bepaal na het in- en uitstappen waar de lift nu naartoe moet
            determineElevatorTarget(elevator);
        }
    }

    /**
     * Bepaalt de volgende doelverdieping van de lift op basis van passagiers of wachtrijen.
     */
    private void determineElevatorTarget(Elevator elevator) {
        // Prioriteit 1: Als er mensen in de lift zitten, breng hen eerst weg
        if (!elevator.passengers.isEmpty()) {
            int currentFloor = (int) (elevator.curY / data.tileSize);

            elevator.targetFloor = elevator.passengers.stream()
                    .mapToInt(g -> (int) (g.targetY / data.tileSize))
                    .min()
                    .orElse(currentFloor);

        } else {
            // Prioriteit 2: Als de lift leeg is, zoek de eerste verdieping waar mensen in de rij staan te wachten
            for (Map.Entry<Integer, Queue<Guest>> entry : data.floorQueues.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    elevator.targetFloor = entry.getKey(); // Stuur lift naar de verdieping met wachtenden
                    break; // Stop met zoeken zodra er één gevonden is
                }
            }
        }
    }
}