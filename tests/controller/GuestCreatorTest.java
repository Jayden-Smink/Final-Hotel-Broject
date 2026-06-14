package controller;

import factory.PersonFactory;
import model.Guest;
import model.PersonType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuestCreatorTest {

    @Test
    void testCreate_InstantiatesGuestWithCorrectIdentifiers() {
        // Arrange
        GuestCreator creator = new GuestCreator();
        int expectedId = 505;

        // Act
        Guest result = creator.create(expectedId);

        // Assert
        assertNotNull(result, "The creator should return a valid instantiated Guest object");
        assertEquals(expectedId, result.id, "The generated Guest should carry the exact ID passed into the creator method");
    }

    @Test
    void testCreate_InitializesSpatialCoordinatesToZero() {
        // Arrange
        GuestCreator creator = new GuestCreator();

        // Act
        Guest result = creator.create(1);

        // Assert
        // The creator calls PersonFactory with 0, 0 for x and y defaults
        assertEquals(0.0, result.x, 0.001, "Initial raw x position should be anchored to 0");
        assertEquals(0.0, result.y, 0.001, "Initial raw y position should be anchored to 0");
    }

    @Test
    void testCreate_ReturnsCorrectInstanceType() {
        // Arrange
        GuestCreator creator = new GuestCreator();

        // Act
        Object result = creator.create(99);

        // Assert
        // Verifies that the internal factory generation didn't break down or cause a casting crash
        assertTrue(result instanceof Guest, "The returned object must be an instance of (or extend) the Guest class model");
    }
}