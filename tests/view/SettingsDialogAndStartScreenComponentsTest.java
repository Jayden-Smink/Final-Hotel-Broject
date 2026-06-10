package view;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SettingsDialog.
 *
 * SettingsDialog extends JDialog (modal). We construct it with a null parent
 * so no real JFrame is needed, and we never call setVisible(true) so it does
 * not block the test thread.
 */
class SettingsDialogTest {

    private SettingsDialog buildDialog(int cleaningSeconds, int cleanerCount, int scenario) {
        return new SettingsDialog(null, cleaningSeconds, cleanerCount, scenario);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> buildDialog(10, 2, 1));
    }

    @Test
    void constructor_withMinimumValues_doesNotThrow() {
        assertDoesNotThrow(() -> buildDialog(1, 1, 1));
    }

    @Test
    void constructor_withMaximumValues_doesNotThrow() {
        assertDoesNotThrow(() -> buildDialog(300, 10, 4));
    }

    @Test
    void constructor_withAllScenarios_doesNotThrow() {
        for (int s = 1; s <= 4; s++) {
            int scenario = s;
            assertDoesNotThrow(() -> buildDialog(10, 2, scenario),
                    "Constructor should not throw for scenario " + scenario);
        }
    }

    @Test
    void constructor_withOutOfRangeScenario_doesNotThrow() {
        // Scenario outside 1–4 → combobox stays at default
        assertDoesNotThrow(() -> buildDialog(10, 2, 99));
        assertDoesNotThrow(() -> buildDialog(10, 2, 0));
    }

    // ── getCleaningSeconds ────────────────────────────────────────────────────

    @Test
    void getCleaningSeconds_returnsInitialValue() {
        SettingsDialog dialog = buildDialog(45, 2, 1);
        assertEquals(45, dialog.getCleaningSeconds());
    }

    @Test
    void getCleaningSeconds_returnsOneForMinimum() {
        SettingsDialog dialog = buildDialog(1, 1, 1);
        assertEquals(1, dialog.getCleaningSeconds());
    }

    // ── getCleanerCount ───────────────────────────────────────────────────────

    @Test
    void getCleanerCount_returnsInitialValue() {
        SettingsDialog dialog = buildDialog(10, 5, 1);
        assertEquals(5, dialog.getCleanerCount());
    }

    @Test
    void getCleanerCount_returnsOneWhenSetToOne() {
        SettingsDialog dialog = buildDialog(10, 1, 1);
        assertEquals(1, dialog.getCleanerCount());
    }

    // ── getCinemaDurationSeconds (default 30) ─────────────────────────────────

    @Test
    void getCinemaDurationSeconds_returnsDefaultThirty() {
        SettingsDialog dialog = buildDialog(10, 2, 1);
        assertEquals(30, dialog.getCinemaDurationSeconds());
    }

    // ── getRestaurantDurationSeconds (default 10) ─────────────────────────────

    @Test
    void getRestaurantDurationSeconds_returnsDefaultTen() {
        SettingsDialog dialog = buildDialog(10, 2, 1);
        assertEquals(10, dialog.getRestaurantDurationSeconds());
    }

    // ── getFitnessDurationSeconds (default 15) ────────────────────────────────

    @Test
    void getFitnessDurationSeconds_returnsDefaultFifteen() {
        SettingsDialog dialog = buildDialog(10, 2, 1);
        assertEquals(15, dialog.getFitnessDurationSeconds());
    }

    // ── getSelectedScenario ───────────────────────────────────────────────────

    @Test
    void getSelectedScenario_returnsScenarioOne_whenConstructedWithOne() {
        SettingsDialog dialog = buildDialog(10, 2, 1);
        assertEquals(1, dialog.getSelectedScenario());
    }

    @Test
    void getSelectedScenario_returnsScenarioFour_whenConstructedWithFour() {
        SettingsDialog dialog = buildDialog(10, 2, 4);
        assertEquals(4, dialog.getSelectedScenario());
    }

    // ── isConfirmed ───────────────────────────────────────────────────────────

    @Test
    void isConfirmed_returnsFalseBeforeUserAction() {
        SettingsDialog dialog = buildDialog(10, 2, 1);
        assertFalse(dialog.isConfirmed(),
                "Dialog must not be confirmed before the user clicks Save");
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tests for StartScreenComponents.
 */
class StartScreenComponentsTest {

    // ── createBackgroundPanel ─────────────────────────────────────────────────

    @Test
    void createBackgroundPanel_doesNotThrow() {
        assertDoesNotThrow(StartScreenComponents::createBackgroundPanel);
    }

    @Test
    void createBackgroundPanel_returnsNonNull() {
        assertNotNull(StartScreenComponents.createBackgroundPanel());
    }

    @Test
    void createBackgroundPanel_returnsJPanel() {
        assertTrue(StartScreenComponents.createBackgroundPanel() instanceof JPanel);
    }

    @Test
    void createBackgroundPanel_hasNoChildrenByDefault() {
        JPanel panel = StartScreenComponents.createBackgroundPanel();
        assertEquals(0, panel.getComponentCount(),
                "A freshly created background panel should have no children");
    }

    @Test
    void createBackgroundPanel_canReceiveChildren() {
        JPanel panel = StartScreenComponents.createBackgroundPanel();
        panel.add(new JLabel("test"));
        assertEquals(1, panel.getComponentCount());
    }

    @Test
    void createBackgroundPanel_calledTwice_returnsIndependentInstances() {
        JPanel p1 = StartScreenComponents.createBackgroundPanel();
        JPanel p2 = StartScreenComponents.createBackgroundPanel();
        assertNotSame(p1, p2);
    }

    // ── createTitle ───────────────────────────────────────────────────────────

    @Test
    void createTitle_doesNotThrow() {
        assertDoesNotThrow(() -> StartScreenComponents.createTitle("Hotel Simulator"));
    }

    @Test
    void createTitle_returnsNonNull() {
        assertNotNull(StartScreenComponents.createTitle("Test"));
    }

    @Test
    void createTitle_returnsJLabel() {
        assertTrue(StartScreenComponents.createTitle("Test") instanceof JLabel);
    }

    @Test
    void createTitle_textIsSet() {
        JLabel label = StartScreenComponents.createTitle("Mijn Hotel");
        assertEquals("Mijn Hotel", label.getText());
    }

    @Test
    void createTitle_fontIsLarge() {
        JLabel label = StartScreenComponents.createTitle("X");
        assertTrue(label.getFont().getSize() >= 20,
                "Title font should be large (at least 20pt), was: " + label.getFont().getSize());
    }

    @Test
    void createTitle_isCentreAligned() {
        JLabel label = StartScreenComponents.createTitle("X");
        assertEquals(Component.CENTER_ALIGNMENT, label.getAlignmentX(), 0.001f);
    }

    @Test
    void createTitle_withEmptyString_doesNotThrow() {
        assertDoesNotThrow(() -> StartScreenComponents.createTitle(""));
    }
}
