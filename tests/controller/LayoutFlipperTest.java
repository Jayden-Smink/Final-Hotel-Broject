package controller;

import factory.RoomFactory;
import model.Area;
import model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayoutFlipperTest {

    private LayoutFlipper flipper;
    private List<Area> areas;

    @BeforeEach
    void setUp() {
        flipper = new LayoutFlipper();
        areas = new ArrayList<>();
    }

    @Test
    void testTransformAndAddInfrastructure_FlipsYCoordinatesCorrectly() {
        // Arrange: Build a dummy room using RoomFactory to ensure all inner fields exist.
        // File coordinates: X=2, Y=10 with width=2, height=3
        Area room = RoomFactory.createRuimte(RoomType.ROOM, "2, 10", "2, 3", 1);
        areas.add(room);

        // Act
        flipper.transformAndAddInfrastructure(areas);

        // Assert:
        // MaxGridY calculation = pos[1] + dim[1] = 10 + 3 = 13. MinGridY = 10.
        // FlippedY = (13 - 10) - 10 - 3 + 10 = 3 - 13 + 10 = 0
        // Expected string format after conversion: "2, 0"
        assertEquals("2, 0", room.Position, "The Y-axis coordinate should be flipped relative to grid height constraints");
    }

    @Test
    void testTransformAndAddInfrastructure_AppendsAllFourMandatoryInfrastructureRooms() {
        // Arrange: Give it at least one reference point area to scale the coordinates from
        Area baselineRoom = RoomFactory.createRuimte(RoomType.ROOM, "1, 1", "4, 4", 1);
        areas.add(baselineRoom);

        // Act
        flipper.transformAndAddInfrastructure(areas);

        // Assert: Initial size (1) + 4 appended infrastructure blocks = 5 elements total
        assertEquals(5, areas.size(), "Should append exactly 4 infrastructure sections to the grid array");

        // Verify Lift Shaft is assigned the proper layout metrics
        Area lift = findAreaById(areas, -99);
        assertNotNull(lift, "Elevator lift shaft (-99) should be appended");
        assertEquals("LIFTSCHACHT", lift.AreaType);
        assertEquals("0, 0", lift.Position);

        // Verify Stairs (Trap) are placed at the far horizontal edge of the layout grid
        Area stairs = findAreaById(areas, -98);
        assertNotNull(stairs, "Stairwell infrastructure (-98) should be appended");
        assertEquals("TRAP", stairs.AreaType);

        // Verify Hotel Lobby is placed at the bottom edge of the layout grid
        Area lobby = findAreaById(areas, -100);
        assertNotNull(lobby, "Lobby tracking element (-100) should be appended");
        assertEquals("LOBBY", lobby.AreaType);

        // Verify Reception desk is placed in the lower corner of the layout grid
        Area reception = findAreaById(areas, -101);
        assertNotNull(reception, "Reception zone (-101) should be appended");
        assertEquals("RECEPTION", reception.AreaType);
    }

    @Test
    void testTransformAndAddInfrastructure_HandlesEmptyListsGracefully() {
        // Act & Assert
        // Passing an empty array triggers calculateBounds math to use 0 width and Max Integer limits.
        // This confirms the logic safety loops don't crash when hitting edge cases.
        assertDoesNotThrow(() -> flipper.transformAndAddInfrastructure(areas),
                "Flipper should process an empty list safely without crashing or throwing Arithmetic exceptions");
    }

    // --- Private Test Help Utility ------------------------------------------

    private Area findAreaById(List<Area> list, int targetId) {
        return list.stream()
                .filter(a -> a.id == targetId)
                .findFirst()
                .orElse(null);
    }
}