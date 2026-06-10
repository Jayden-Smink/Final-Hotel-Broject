package view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StartScreen.
 *
 * StartScreen extends JFrame and calls setVisible(true) in its constructor,
 * which requires a display. Tests are therefore skipped in headless environments
 * (CI without a display server). Run locally or with a virtual display (Xvfb).
 *
 * Add -Djava.awt.headless=false to JVM args to enable in a display environment,
 * or -Djava.awt.headless=true to confirm headless skipping works.
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
class StartScreenTest {

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        StartScreen screen = assertDoesNotThrow(StartScreen::new);
        screen.dispose();
    }

    @Test
    void constructor_createsVisibleJFrame() {
        StartScreen screen = new StartScreen();
        assertTrue(screen.isVisible());
        screen.dispose();
    }

    @Test
    void constructor_setsCorrectSize() {
        StartScreen screen = new StartScreen();
        assertEquals(620, screen.getWidth());
        assertEquals(700, screen.getHeight());
        screen.dispose();
    }

    @Test
    void constructor_isUndecorated() {
        StartScreen screen = new StartScreen();
        assertTrue(screen.isUndecorated());
        screen.dispose();
    }

    @Test
    void constructor_contentPaneIsNotNull() {
        StartScreen screen = new StartScreen();
        assertNotNull(screen.getContentPane());
        screen.dispose();
    }

    @Test
    void constructor_contentPaneContainsComponents() {
        StartScreen screen = new StartScreen();
        assertTrue(screen.getContentPane().getComponentCount() > 0,
                "Root panel should contain at least one child component");
        screen.dispose();
    }

    @Test
    void dispose_doesNotThrow() {
        StartScreen screen = new StartScreen();
        assertDoesNotThrow(screen::dispose);
    }

    @Test
    void multipleInstances_canBeCreatedAndDisposed() {
        StartScreen s1 = new StartScreen();
        StartScreen s2 = new StartScreen();
        assertDoesNotThrow(() -> {
            s1.dispose();
            s2.dispose();
        });
    }
}
