package controller;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.LogPanel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerAssignerTest {

    private SimulationData data;
    private CleanerAssigner assigner;
    private LogPanel logPanel;

    @BeforeEach
    void setUp() {
        // SimulationData has no no-arg constructor, so we pass a minimal list.
        // Each test replaces data.areas directly before acting.
        data = new SimulationData(new ArrayList<>(), 1, 1, 1, 1, 1, 1);
        logPanel = null;
        assigner = new CleanerAssigner(data, logPanel);
    }

    private Area room(int id, int x, int y) {
        Area a = new Area();
        a.id = id;
        a.AreaType = "ROOM";
        a.Position = x + "," + y;
        a.Dimension = "2,2";
        return a;
    }

    private Area lobby(int x, int y) {
        Area a = new Area();
        a.id = 999;
        a.AreaType = "LOBBY";
        a.Position = x + "," + y;
        a.Dimension = "2,2";
        return a;
    }

    @Test
    void assignToRoom_setsStateAndTarget() {
        Cleaner c = new Cleaner(1, 0, 0);
        Area r = room(1, 2, 3);
        data.areas = new ArrayList<>(List.of(r));

        assigner.assignToRoom(c, 1);

        assertEquals(CleanerState.WALKING_TO_ROOM, c.state);
        assertEquals(1, c.assignedRoomId);
        assertTrue(c.targetX > 0);
        assertTrue(c.targetY > 0);
    }

    @Test
    void assignToRoom_invalidRoom_doesNothing() {
        Cleaner c = new Cleaner(1, 0, 0);
        Area r = room(2, 1, 1);
        data.areas = new ArrayList<>(List.of(r));

        assigner.assignToRoom(c, 999);

        assertEquals(CleanerState.IDLE, c.state);
        assertEquals(-1, c.assignedRoomId);
    }

    @Test
    void sendToLobby_setsTarget() {
        Cleaner c = new Cleaner(1, 0, 0);
        Area l = lobby(1, 1);
        data.areas = new ArrayList<>(List.of(l));

        assigner.sendToLobby(c);

        assertTrue(c.targetX > 0);
        assertTrue(c.targetY > 0);
    }

    @Test
    void findFirstRoomId_returnsFirstRoom() {
        Area r1 = room(10, 1, 1);
        Area r2 = room(20, 2, 2);
        data.areas = new ArrayList<>(List.of(r1, r2));

        int id = assigner.findFirstRoomId();

        assertEquals(10, id);
    }

    @Test
    void findFirstRoomId_noRooms_returnsMinus1() {
        Area l = lobby(1, 1);
        data.areas = new ArrayList<>(List.of(l));

        int id = assigner.findFirstRoomId();

        assertEquals(-1, id);
    }
}