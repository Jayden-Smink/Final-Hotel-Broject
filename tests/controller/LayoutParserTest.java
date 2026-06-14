package controller;

import model.Area;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayoutParserTest {

    private LayoutParser parser;

    @BeforeEach
    void setUp() {
        parser = new LayoutParser();
    }

    @Test
    void testParse_SuccessfulWithExplicitValues() {
        // Appending a dummy variable at the end ensures a comma exists after the Capacity property
        String layoutContent = "{\"AreaType\":\"ROOM\",\"Position\":\"1,2\",\"Dimension\":\"2,2\",\"ID\":\"101\",\"Capacity\":\"4\",\"Dummy\":\"Fix\"}";

        // Act
        List<Area> result = parser.parse(layoutContent);

        // Assert
        assertFalse(result.isEmpty(), "Parser returned an empty list");
        Area area = result.get(0);
        assertEquals("ROOM", area.AreaType);
        assertEquals("1,2", area.Position);
        assertEquals("2,2", area.Dimension);
        assertEquals(101, area.id);
        assertEquals(4, area.Capacity);
    }

    @Test
    void testParse_AutoIncrementsIdsWhenMissing() {
        // Both segments include a trailing comma/property configuration block
        String layoutContent = "{\"AreaType\":\"ROOM\",\"Position\":\"0,0\",\"Dimension\":\"1,1\",\"Dummy\":\"1\"}" +
                "{\"AreaType\":\"CINEMA\",\"Position\":\"3,0\",\"Dimension\":\"4,2\",\"Dummy\":\"2\"}";

        // Act
        List<Area> result = parser.parse(layoutContent);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id);
        assertEquals(2, result.get(1).id);
    }

    @Test
    void testParse_FallsBackToDefaultCapacities() {
        String layoutContent = "{\"AreaType\":\"CINEMA\",\"Position\":\"0,0\",\"Dimension\":\"1,1\",\"Dummy\":\"1\"}" +
                "{\"AreaType\":\"RESTAURANT\",\"Position\":\"0,0\",\"Dimension\":\"1,1\",\"Dummy\":\"1\"}" +
                "{\"AreaType\":\"ROOM\",\"Position\":\"0,0\",\"Dimension\":\"1,1\",\"Dummy\":\"1\"}";

        // Act
        List<Area> result = parser.parse(layoutContent);

        // Assert
        assertEquals(3, result.size());
        assertEquals(10, result.get(0).Capacity);
        assertEquals(5, result.get(1).Capacity);
        assertEquals(1, result.get(2).Capacity);
    }

    @Test
    void testParse_ReturnsNegativeOneIdForNonRoomInfrastructures() {
        String layoutContent = "{\"AreaType\":\"TRAP\",\"Position\":\"0,5\",\"Dimension\":\"10,1\",\"Dummy\":\"1\"}";

        // Act
        List<Area> result = parser.parse(layoutContent);

        // Assert
        assertEquals(1, result.size());
        assertEquals(-1, result.get(0).id);
    }

    @Test
    void testParse_IgnoresBlocksMissingAreaType() {
        String invalidContent = "{\"SomethingElse\":\"VALUE\",\"Position\":\"1,2\",\"Dummy\":\"1\"}";

        // Act
        List<Area> result = parser.parse(invalidContent);

        // Assert
        assertTrue(result.isEmpty());
    }
}