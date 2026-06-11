package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorControllerTest {

    private SimulationData data;
    private ElevatorController controller;

    private Area makeArea(int id, String type, String pos) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = 10;
        return a;
    }

    private Guest makeGuest(int id, double x, double y) {
        Guest g = new Guest(id, x, y);
        g.state = GuestState.WALKING;
        return g;
    }

    /** Zet de lift hard op een specifieke verdieping zodat curY, targetFloor en isMoving kloppen. */
    private void setElevatorFloor(Elevator elevator, int floor) {
        elevator.curY = floor * data.tileSize;
        elevator.targetFloor = floor;
        elevator.isMoving = false;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,0")); // pos y=0 → lift start op verdieping 0
        data = new SimulationData(areas, 4, 30, 30, 10, 15, 60);
        controller = new ElevatorController(data, null);
    }

    // ── update — geen lift ────────────────────────────────────────────────────

    @Test
    void update_noElevator_doesNotThrow() {
        data.elevator = null;
        assertDoesNotThrow(() -> controller.update());
    }

    // ── update — passagier uitstappen ─────────────────────────────────────────

    @Test
    void update_passengerOnCorrectFloor_exitsLift() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(1, elevator.curX, 0);
        g.state = GuestState.IN_LIFT;
        g.targetY = 0; // doelverdieping = 0
        elevator.passengers.add(g);
        data.guests.put(1, g);

        controller.update();

        assertFalse(elevator.passengers.contains(g), "Passagier moet uitstappen op de goede verdieping");
        assertEquals(GuestState.EXITING_LIFT, g.state);
    }

    @Test
    void update_passengerOnWrongFloor_staysInLift() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(1, elevator.curX, 0);
        g.state = GuestState.IN_LIFT;
        g.targetY = 300; // wil op verdieping 5
        elevator.passengers.add(g);
        data.guests.put(1, g);

        controller.update();

        assertTrue(elevator.passengers.contains(g), "Passagier mag NIET uitstappen op de verkeerde verdieping");
    }

    // ── update — wachtende gast instappen ────────────────────────────────────

    @Test
    void update_waitingGuestOnCurrentFloor_boardsElevator() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(1, elevator.curX, 0);
        g.state = GuestState.IN_QUEUE;
        g.waitingOnFloor = 0;
        elevator.waitingGuests.add(g);
        data.guests.put(1, g);

        controller.update();

        assertTrue(elevator.passengers.contains(g), "Wachtende gast op huidige verdieping moet instappen");
        assertEquals(GuestState.IN_LIFT, g.state);
        assertEquals(0, g.elevatorWaitTimer, "Wachttimer moet resetten bij instappen");
    }

    @Test
    void update_waitingGuestOnOtherFloor_doesNotBoard() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(1, elevator.curX, 120);
        g.state = GuestState.IN_QUEUE;
        g.waitingOnFloor = 2; // wacht op verdieping 2, lift staat op 0
        elevator.waitingGuests.add(g);
        data.guests.put(1, g);

        controller.update();

        assertFalse(elevator.passengers.contains(g), "Gast op andere verdieping mag niet instappen");
    }

    // ── update — capaciteitslimiet ────────────────────────────────────────────

    @Test
    void update_elevatorFull_doesNotBoardExtraGuest() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);
        elevator.maxCapacity = 1;

        Guest existing = makeGuest(10, elevator.curX, 0);
        existing.state = GuestState.IN_LIFT;
        existing.targetY = 300;
        elevator.passengers.add(existing);

        Guest waiting = makeGuest(2, elevator.curX, 0);
        waiting.state = GuestState.IN_QUEUE;
        waiting.waitingOnFloor = 0;
        elevator.waitingGuests.add(waiting);
        data.guests.put(2, waiting);

        controller.update();

        assertFalse(elevator.passengers.contains(waiting), "Volle lift mag geen extra passagier oppikken");
    }

    // ── update — wachttimer te lang → gast sterft ────────────────────────────

    @Test
    void update_waitTimerExceeded_removesGuestFromSimulation() {
        Elevator elevator = data.elevator;
        int timeout = data.guestSettings.getElevatorWaitTimeout();

        Guest g = makeGuest(5, 0, 0);
        g.state = GuestState.IN_QUEUE;
        g.waitingOnFloor = 0;
        g.elevatorWaitTimer = timeout - 1;
        elevator.waitingGuests.add(g);
        data.guests.put(5, g);

        controller.update();

        assertFalse(elevator.waitingGuests.contains(g), "Gast moet uit wachtrij worden verwijderd na timeout");
        assertFalse(data.guests.containsKey(5), "Gast moet uit de simulatie worden verwijderd na timeout");
    }

    @Test
    void update_waitTimerNotExceeded_keepGuestInQueue() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 3); // andere verdieping zodat gast niet direct instapt

        Guest g = makeGuest(6, 0, 0);
        g.state = GuestState.IN_QUEUE;
        g.waitingOnFloor = 0;
        g.elevatorWaitTimer = 0;
        elevator.waitingGuests.add(g);
        data.guests.put(6, g);

        controller.update();

        assertTrue(elevator.waitingGuests.contains(g) || elevator.passengers.contains(g),
                "Gast met korte wachttijd mag niet verwijderd worden");
    }

    // ── update — lift rijdt naar passagier ───────────────────────────────────

    @Test
    void update_passengerInLift_elevatorTargetsTheirFloor() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(1, elevator.curX, 0);
        g.state = GuestState.IN_LIFT;
        g.targetY = 180; // verdieping 3 (180 / 60 = 3)
        elevator.passengers.add(g);
        data.guests.put(1, g);

        controller.update();

        assertEquals(3, elevator.targetFloor, "Lift moet naar de doelverdieping van de passagier rijden");
    }

    // ── update — lift rijdt naar wachtende gast ──────────────────────────────

    @Test
    void update_noPassengersButWaitingGuest_elevatorGoesToWaitingFloor() {
        Elevator elevator = data.elevator;
        setElevatorFloor(elevator, 0);

        Guest g = makeGuest(2, 0, 120);
        g.state = GuestState.IN_QUEUE;
        g.waitingOnFloor = 2;
        elevator.waitingGuests.add(g);
        data.guests.put(2, g);

        controller.update();

        assertEquals(2, elevator.targetFloor, "Lift moet naar de verdieping van de wachtende gast gaan");
    }
}