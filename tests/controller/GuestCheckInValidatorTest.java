package controller;

import model.Area;
import model.Guest;
import model.SimulationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.LogPanel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestCheckInValidatorTest {

    private SimulationData realData;
    private LogPanel stubLogPanel;
    private GuestCheckInValidator validator;
    private List<String> capturedLogs;

    @BeforeEach
    void setUp() {
        capturedLogs = new ArrayList<>();
        stubLogPanel = new LogPanel() {
            @Override
            public void addLog(String message) {
                capturedLogs.add(message);
            }
        };
    }

    private void initValidatorWithLayout(boolean includeLobby, boolean includeReception) {
        List<Area> areas = new ArrayList<>();

        if (includeLobby) {
            Area lobby = new Area();
            lobby.AreaType = "LOBBY";
            lobby.Position = "0,0";
            lobby.Dimension = "1,1";
            areas.add(lobby);
        }

        if (includeReception) {
            Area reception = new Area();
            reception.AreaType = "RECEPTION";
            reception.Position = "1,0";
            reception.Dimension = "1,1";
            areas.add(reception);
        }

        realData = new SimulationData(areas, 5, 10, 10, 10, 10, 10);
        validator = new GuestCheckInValidator(realData, stubLogPanel);
    }

    @Test
    void testValidate_Successful() {
        initValidatorWithLayout(true, true);

        boolean isValid = validator.validate(1);

        assertTrue(isValid, "Validation should pass when layout is valid and guest ID is unique");
        assertTrue(capturedLogs.isEmpty(), "No error logs should be recorded on a successful pass");
    }

    @Test
    void testValidate_FailsDueToInvalidGuestId() {
        initValidatorWithLayout(true, true);

        boolean isValidZero = validator.validate(0);
        boolean isValidNegative = validator.validate(-5);

        assertFalse(isValidZero);
        assertFalse(isValidNegative);
        assertFalse(capturedLogs.isEmpty(), "An error message should be sent to the log panel");
        assertTrue(capturedLogs.get(0).contains("Ongeldige guestId genegeerd"));
    }

    @Test
    void testValidate_FailsDueToDuplicateGuest() {
        initValidatorWithLayout(true, true);

        // Add a guest with the same ID into the system ahead of time
        Guest duplicateGuest = new Guest(1, 0.0, 0.0);
        realData.guests.put(1, duplicateGuest);

        boolean isValid = validator.validate(1);

        assertFalse(isValid, "Validator must reject a guest ID that already exists in the system");
        assertTrue(capturedLogs.stream().anyMatch(log -> log.contains("Dubbele check-in genegeerd")));
    }

    @Test
    void testValidate_FailsWhenLobbyIsMissing() {
        initValidatorWithLayout(false, true); // No Lobby, Has Reception

        boolean isValid = validator.validate(1);

        assertFalse(isValid);
        assertTrue(capturedLogs.stream().anyMatch(log -> log.contains("lobby ontbreekt")));
    }

    @Test
    void testValidate_FailsWhenReceptionIsMissing() {
        initValidatorWithLayout(true, false); // Has Lobby, No Reception

        boolean isValid = validator.validate(1);

        assertFalse(isValid);
        assertTrue(capturedLogs.stream().anyMatch(log -> log.contains("receptie ontbreekt")));
    }
}