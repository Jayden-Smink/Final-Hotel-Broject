package factory;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import org.junit.jupiter.api.Test;

class UIFactoryTest {

    @Test
    void testCreatePrimaryStyledButton() {
        // Arrange & Act
        JButton button = UIFactory.createStyledButton("Start", true);

        // Assert: Basisinstellingen controleren
        assertNotNull(button, "De gegenereerde button mag niet null zijn.");
        assertEquals("Start", button.getText(), "De tekst op de button klopt niet.");
        assertFalse(button.isContentAreaFilled(), "ContentAreaFilled moet false zijn voor custom rendering.");
        assertFalse(button.isBorderPainted(), "BorderPainted moet false zijn.");
        assertFalse(button.isFocusPainted(), "FocusPainted moet false zijn.");
        assertEquals(Component.CENTER_ALIGNMENT, button.getAlignmentX(), "AlignmentX moet gecentreerd zijn.");
    }

    @Test
    void testCreateSecondaryStyledButton() {
        // Arrange & Act
        JButton button = UIFactory.createStyledButton("Annuleren", false);

        // Assert
        assertEquals("Annuleren", button.getText());
        // Zowel primary als secondary moeten dezelfde dimensie-restricties hebben
        assertEquals(new Dimension(350, 45), button.getMaximumSize());
    }

    @Test
    void testMouseHoverEvents() {
        // Arrange
        JButton button = UIFactory.createStyledButton("Hover Me", true);
        MouseListener[] listeners = button.getMouseListeners();

        // Controleer of de MouseAdapter daadwerkelijk is toegevoegd
        assertTrue(listeners.length > 0, "Er moet een MouseListener geregistreerd staan.");
        MouseListener hoverListener = listeners[0];

        // Act & Assert: Simuleer muis die de button binnentreedt (mouseEntered)
        // We vuren handmatig een MouseEvent af op de listener
        assertDoesNotThrow(() -> {
            hoverListener.mouseEntered(new MouseEvent(
                    button,
                    MouseEvent.MOUSE_ENTERED,
                    System.currentTimeMillis(),
                    0, 10, 10, 0, false
            ));
        }, "Het mouseEntered event mag geen exceptions gooien.");

        // Simuleer muis die de button verlaat (mouseExited)
        assertDoesNotThrow(() -> {
            hoverListener.mouseExited(new MouseEvent(
                    button,
                    MouseEvent.MOUSE_EXITED,
                    System.currentTimeMillis(),
                    0, 10, 10, 0, false
            ));
        }, "Het mouseExited event mag geen exceptions gooien.");
    }
}