package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireDestructionTest {

    private FireDestruction fireStrategy;
    private Area testArea;
    // Using 2 seconds for a predictable, realistic test environment frame scale
    private final int testDurationSeconds = 2;
    private final int expectedMaxFrames = testDurationSeconds * 60; // 120 frames

    @BeforeEach
    void setUp() {
        fireStrategy = new FireDestruction(testDurationSeconds);
        testArea = new Area();
    }

    @Test
    void testConstructor_ConvertsSecondsToFramesCorrectly() {
        // We can infer conversion success through functional update limits
        testArea.isOnFire = true;
        testArea.isDestroyed = false;
        testArea.fireTimer = 0;

        // Advance 119 frames (1 frame short of burnout threshold)
        for (int i = 0; i < expectedMaxFrames - 1; i++) {
            fireStrategy.update(testArea);
        }

        assertFalse(testArea.isDestroyed, "Area should remain intact at frame " + (expectedMaxFrames - 1));

        // Advance the 120th frame
        fireStrategy.update(testArea);
        assertTrue(testArea.isDestroyed, "Area should collapse exactly on frame threshold limit match");
    }

    @Test
    void testDestroy_InitializesFireStateWithoutCollapsingStructure() {
        // Arrange: Start with an area that has already been broken or has flags mixed up
        testArea.isOnFire = false;
        testArea.isDestroyed = true;
        testArea.fireTimer = 500;

        // Act
        fireStrategy.destroy(testArea);

        // Assert: It should force ignition fields to active baseline conditions
        assertTrue(testArea.isOnFire, "Area must immediately catch fire");
        assertFalse(testArea.isDestroyed, "Area should not be marked as destroyed during initial ignition phase");
        assertEquals(0, testArea.fireTimer, "Fire clock tracker must reset back to zero frames");
    }

    @Test
    void testUpdate_IncrementsTimerWhileBurning() {
        // Arrange
        fireStrategy.destroy(testArea);

        // Act
        fireStrategy.update(testArea);

        // Assert
        assertEquals(1, testArea.fireTimer, "Each update call must step the internal framework clock tracker up by 1 tick");
        assertTrue(testArea.isOnFire);
        assertFalse(testArea.isDestroyed);
    }

    @Test
    void testUpdate_CollapsesStructureAndExtinguishesFireAtBurnoutThreshold() {
        // Arrange
        fireStrategy.destroy(testArea);

        // Advance the simulation loop all the way up to the frame ceiling limit
        for (int i = 0; i < expectedMaxFrames; i++) {
            fireStrategy.update(testArea);
        }

        // Assert
        assertTrue(testArea.isDestroyed, "The structure should now be collapsed/destroyed");
        assertFalse(testArea.isOnFire, "The fire flag should be extinguished after complete consumption");
    }

    @Test
    void testUpdate_DoesNothingIfAreaIsAlreadyDestroyed() {
        // Arrange
        testArea.isOnFire = true;
        testArea.isDestroyed = true;
        testArea.fireTimer = 10;

        // Act
        fireStrategy.update(testArea);

        // Assert: The guard check '&& !area.isDestroyed' should short-circuit execution
        assertEquals(10, testArea.fireTimer, "The fire clock should freeze if the room block is already down");
    }

    @Test
    void testUpdate_DoesNothingIfAreaIsNotOnFire() {
        // Arrange
        testArea.isOnFire = false;
        testArea.isDestroyed = false;
        testArea.fireTimer = 0;

        // Act
        fireStrategy.update(testArea);

        // Assert: The guard check 'if (area.isOnFire)' should short-circuit execution
        assertEquals(0, testArea.fireTimer, "The fire clock should not increment on clean, non-burning objects");
    }
}