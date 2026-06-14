package controller;

import model.Area;
import model.Guest;
import model.GuestState;
import model.SimulationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.LogPanel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestControllerTest {

    private SimulationData realData;
    private LogPanel stubLogPanel;
    private TestReceptionistController spyReceptionistController;
    private GuestController guestController;
    private List<String> capturedLogs;

    // Custom spy class to prevent Java 25 Byte Buddy proxy bugs with heavy GUI components
    private static class TestReceptionistController extends ReceptionistController {
        public int lastGuestId = -1;
        public int lastPreferredRoomId = -1;
        public int callCount = 0;

        public TestReceptionistController(SimulationData data, LogPanel logPanel) {
            super(data, logPanel);
        }

        @Override
        public void handleCheckIn(int guestId, int preferredRoomId) {
            this.lastGuestId = guestId;
            this.lastPreferredRoomId = preferredRoomId;
            this.callCount++;
        }
    }

    @BeforeEach
    void setUp() {
        capturedLogs = new ArrayList<>();

        stubLogPanel = new LogPanel() {
            @Override
            public void addLog(String message) {
                capturedLogs.add(message);
            }
        };

        // Building layout infrastructure required by GuestCheckInValidator, GuestSpawner & StairModel
        List<Area> areas = new ArrayList<>();

        // 1. Lobby Area
        Area lobby = new Area();
        lobby.Position = "0,0";
        lobby.Dimension = "1,1";
        lobby.AreaType = "LOBBY";
        areas.add(lobby);

        // 2. Reception Area
        Area reception = new Area();
        reception.Position = "1,0";
        reception.Dimension = "1,1";
        reception.AreaType = "RECEPTION";
        areas.add(reception);

        // 3. Staircase/Stairs Area
        Area stairs = new Area();
        stairs.Position = "2,0";
        stairs.Dimension = "1,1";
        stairs.AreaType = "STAIRS";
        areas.add(stairs);

        // Instantiate authentic data structures
        realData = new SimulationData(areas, 5, 10, 10, 10, 10, 10);
        spyReceptionistController = new TestReceptionistController(realData, stubLogPanel);

        guestController = new GuestController(realData, stubLogPanel, spyReceptionistController);
    }

    @Test
    void testProcessCheckIn_Successful() {
        int guestId = 1;
        int preferredRoomId = 101;

        // Act
        guestController.processCheckIn(guestId, preferredRoomId);

        // Assert
        assertEquals(1, spyReceptionistController.callCount, "Receptionist should be notified of successful check-in");
        assertEquals(guestId, spyReceptionistController.lastGuestId);
        assertEquals(preferredRoomId, spyReceptionistController.lastPreferredRoomId);
    }

    @Test
    void testProcessCheckIn_FailsValidation_DuplicateGuest() {
        int guestId = 1;
        int preferredRoomId = 101;

        // Arrange: Pre-populate map to trigger duplicate check constraints
        Guest existingGuest = new Guest(guestId, 0.0, 0.0);
        realData.guests.put(guestId, existingGuest);

        // Act
        guestController.processCheckIn(guestId, preferredRoomId);

        // Assert
        assertEquals(0, spyReceptionistController.callCount, "Receptionist should not be called when duplicate check blocks check-in");
    }

    @Test
    void testUpdate_ChangesStateFromExitingLiftToWalking() {
        int guestId = 42;

        // Arrange: Position guest at 0,0 but set a far away target
        // to prevent GuestMover from instantly flipping them to AT_DESTINATION
        Guest guest = new Guest(guestId, 0.0, 0.0);
        guest.setTarget(500.0, 500.0);
        guest.state = GuestState.EXITING_LIFT;
        realData.guests.put(guestId, guest);

        // Act
        guestController.update();

        // Assert
        assertEquals(GuestState.WALKING, guest.state, "EXITING_LIFT state must safely transition into WALKING");
    }

    @Test
    void testUpdate_MaintainsOtherStateThanExitingLift() {
        int guestId = 43;

        // Arrange
        Guest guest = new Guest(guestId, 0.0, 0.0);
        guest.setTarget(500.0, 500.0);
        guest.state = GuestState.IDLE;
        realData.guests.put(guestId, guest);

        // Act
        guestController.update();

        // Assert
        assertEquals(GuestState.IDLE, guest.state, "IDLE state should stay unchanged through state-machine transitions");
    }
}