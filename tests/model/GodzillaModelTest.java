package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GodzillaModelTest {

    private GodzillaModel godzilla;
    private final double startX = 10.0;
    private final double startY = 50.0;
    private final double speed = 2.5;

    @BeforeEach
    void setUp() {
        godzilla = new GodzillaModel(startX, startY, speed);
    }

    @Test
    void testConstructor_InitializesCorrectly() {
        assertEquals(startX, godzilla.x);
        assertEquals(startY, godzilla.y);
        assertEquals(speed, godzilla.speed);
        assertFalse(godzilla.isActive, "Godzilla should start asleep/inactive");
        assertEquals(0, godzilla.currentColumn);
        assertEquals(0, godzilla.columnDestroyTimer);
    }

    @Test
    void testMoveRight_IncreasesXBySpeed() {
        // Act
        godzilla.moveRight();

        // Assert
        assertEquals(startX + speed, godzilla.x, 0.001);
    }

    @Test
    void testHasReachedNextColumn_CalculatesBoundaryCorrectly() {
        int tileSize = 32;
        int horizontalOffset = 10;

        // Arrange: With currentColumn = 0, expectedX = (0 * 32) + 10 = 10.
        godzilla.x = 9.9;
        assertFalse(godzilla.hasReachedNextColumn(tileSize, horizontalOffset), "Should be false if x is below threshold");

        godzilla.x = 10.0;
        assertTrue(godzilla.hasReachedNextColumn(tileSize, horizontalOffset), "Should be true exactly at the boundary x threshold");

        godzilla.x = 15.5;
        assertTrue(godzilla.hasReachedNextColumn(tileSize, horizontalOffset), "Should be true if way past the threshold");
    }

    @Test
    void testNextColumn_IncrementsAndResetsTimer() {
        // Arrange
        godzilla.columnDestroyTimer = 45;
        godzilla.currentColumn = 2;

        // Act
        godzilla.nextColumn();

        // Assert
        assertEquals(3, godzilla.currentColumn, "Should increment column tracker");
        assertEquals(0, godzilla.columnDestroyTimer, "Timer must be reset to zero for the next room row/column block");
    }

    @Test
    void testIsDone_VerifiesCompletionBoundaries() {
        int maxColumn = 5;

        // Case 1: Before or exactly at max column
        godzilla.currentColumn = 5;
        assertFalse(godzilla.isDone(maxColumn), "Should not be finished if currently processing the final column");

        // Case 2: Crossed past max column bounds
        godzilla.currentColumn = 6;
        assertTrue(godzilla.isDone(maxColumn), "Should flag as done once current column exceeds max layout threshold bounds");
    }
}