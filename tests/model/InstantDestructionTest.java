package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstantDestructionTest {

    private InstantDestruction destructionStrategy;
    private Area testArea;

    @BeforeEach
    void setUp() {
        destructionStrategy = new InstantDestruction();
        testArea = new Area();
    }

    @Test
    void testDestroy_FlipsAreaStateCorrectly() {
        // Arrange: Give the area an initial state where it's intact and currently on fire
        testArea.isDestroyed = false;
        testArea.isOnFire = true;

        // Act
        destructionStrategy.destroy(testArea);

        // Assert: Verify that the strategy instantly updates the state flags
        assertTrue(testArea.isDestroyed, "The area should be marked as destroyed");
        assertFalse(testArea.isOnFire, "The fire flag should be put out/reset upon instant destruction");
    }

    @Test
    void testUpdate_DoesNothing() {
        // Arrange: Establish an initial baseline state
        testArea.isDestroyed = false;
        testArea.isOnFire = true;

        // Act
        destructionStrategy.update(testArea);

        // Assert: Because the update method is empty, nothing in the area should change
        assertFalse(testArea.isDestroyed, "Update should not alter the destruction state");
        assertTrue(testArea.isOnFire, "Update should leave the fire flag untouched");
    }
}