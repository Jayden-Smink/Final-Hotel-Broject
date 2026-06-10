package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestNavigatorTest {

    private SimulationData data;
    private GuestNavigator navigator;

    private Area makeArea(int id, String type, String pos, int cap) {
        Area a = new Area();
        a.id = id;
        a.AreaType = type;
        a.Position = pos;
        a.Dimension = "2,1";
        a.Capacity = cap;
        return a;
    }

    private Guest makeGuest(int id) {
        Guest g = new Guest(id, 0, 0);
        g.state = GuestState.IDLE;
        g.assignedRoomId = -1;
        return g;
    }

    @BeforeEach
    void setUp() {
        List<Area> areas = new ArrayList<>();
        areas.add(makeArea(0, "LOBBY", "0,5", 50));
        areas.add(makeArea(1, "RECEPTION", "2,5", 10));
        areas.add(makeArea(10, "ROOM", "1,2", 5));
        areas.add(makeArea(20, "RESTAURANT", "3,3", 10));
        areas.add(makeArea(21, "CINEMA", "5,3", 10));
        areas.add(makeArea(22, "FITNESS", "7,3", 10));
        data = new SimulationData(areas, 4, 30, 30, 10, 15);
        navigator = new GuestNavigator(data, null);
    }

    // ── sendGuestToExit ───────────────────────────────────────────────────────

    @Test
    void sendGuestToExit_setsTargetXToNearLeft() {
        Guest g = makeGuest(1);
        navigator.sendGuestToExit(g);
        assertEquals(20.0, g.targetX, 0.001, "Uitgang moet op x=20 liggen");
    }

    @Test
    void sendGuestToExit_setsTargetYToLobbyRow() {
        Guest g = makeGuest(1);
        navigator.sendGuestToExit(g);
        // Lobby op pos 0,5 → y = 5*60 + 30
        double expectedY = 5 * data.tileSize + data.tileSize / 2.0;
        assertEquals(expectedY, g.targetY, 0.001);
    }

    @Test
    void sendGuestToExit_setsStateToWalking() {
        Guest g = makeGuest(1);
        g.state = GuestState.IDLE;
        navigator.sendGuestToExit(g);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void sendGuestToExit_noLobby_doesNotThrowOrCrash() {
        data.areas.removeIf(a -> a.AreaType.equalsIgnoreCase("LOBBY"));
        Guest g = makeGuest(1);
        assertDoesNotThrow(() -> navigator.sendGuestToExit(g));
    }

    // ── sendGuestToRandomFacility ─────────────────────────────────────────────

    @Test
    void sendGuestToRandomFacility_activityBecomesWalkingToFacility() {
        Guest g = makeGuest(1);
        navigator.sendGuestToRandomFacility(g);
        assertEquals("WALKING_TO_FACILITY", g.currentActivity);
    }

    @Test
    void sendGuestToRandomFacility_currentFacilityIsKnownType() {
        Guest g = makeGuest(1);
        navigator.sendGuestToRandomFacility(g);
        List<String> validTypes = List.of("RESTAURANT", "CINEMA", "FITNESS");
        assertTrue(validTypes.contains(g.currentFacility),
                "Faciliteit moet RESTAURANT, CINEMA of FITNESS zijn, maar was: " + g.currentFacility);
    }

    @Test
    void sendGuestToRandomFacility_setsStateToWalking() {
        Guest g = makeGuest(1);
        g.state = GuestState.IDLE;
        navigator.sendGuestToRandomFacility(g);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void sendGuestToRandomFacility_noFacilitiesAvailable_doesNotCrash() {
        data.areas.removeIf(a -> List.of("RESTAURANT", "CINEMA", "FITNESS")
                .contains(a.AreaType.toUpperCase()));
        Guest g = makeGuest(1);
        assertDoesNotThrow(() -> navigator.sendGuestToRandomFacility(g));
    }

    @Test
    void sendGuestToRandomFacility_noFacilities_activityUnchanged() {
        data.areas.removeIf(a -> List.of("RESTAURANT", "CINEMA", "FITNESS")
                .contains(a.AreaType.toUpperCase()));
        Guest g = makeGuest(1);
        String original = g.currentActivity;
        navigator.sendGuestToRandomFacility(g);
        assertEquals(original, g.currentActivity, "Activiteit mag niet wijzigen als er geen faciliteiten zijn");
    }

    // ── returnGuestToRoom ─────────────────────────────────────────────────────

    @Test
    void returnGuestToRoom_setsActivityToWalkingToRoom() {
        Guest g = makeGuest(1);
        g.assignedRoomId = 10;
        navigator.returnGuestToRoom(g);
        assertEquals("WALKING_TO_ROOM", g.currentActivity);
    }

    @Test
    void returnGuestToRoom_setsTargetToRoomCenter() {
        Guest g = makeGuest(1);
        g.assignedRoomId = 10;
        navigator.returnGuestToRoom(g);
        // Room 10 op pos 1,2 dim 2,1 tile 60 → targetX = 1*60 + (2*60/2) = 120
        assertEquals(120.0, g.targetX, 0.001);
    }

    @Test
    void returnGuestToRoom_noAssignedRoom_doesNothing() {
        Guest g = makeGuest(1);
        g.assignedRoomId = -1;
        String originalActivity = g.currentActivity;
        navigator.returnGuestToRoom(g);
        assertEquals(originalActivity, g.currentActivity,
                "Activiteit mag niet wijzigen zonder toegewezen kamer");
    }

    @Test
    void returnGuestToRoom_unknownRoomId_doesNotCrash() {
        Guest g = makeGuest(1);
        g.assignedRoomId = 999; // bestaat niet in data.areas
        assertDoesNotThrow(() -> navigator.returnGuestToRoom(g));
    }

    @Test
    void returnGuestToRoom_setsStateToWalking() {
        Guest g = makeGuest(1);
        g.assignedRoomId = 10;
        g.state = GuestState.IDLE;
        navigator.returnGuestToRoom(g);
        assertEquals(GuestState.WALKING, g.state);
    }
}
