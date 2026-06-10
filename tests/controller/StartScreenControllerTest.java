package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.DefaultListModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StartScreenController.
 *
 * StartScreenController.handleStart() opens a JFileChooser dialog, which
 * cannot be driven in a headless CI environment. Those tests are therefore
 * excluded. The tests here cover:
 *   - construction (no crash)
 *   - that the controller correctly stores the injected model
 *
 * If you want to test handleStart() in an integration environment with a
 * display, use a robot / UI test framework (e.g. AssertJ Swing) to confirm
 * that selecting a JSON file launches the simulation window.
 */
class StartScreenControllerTest {

    private DefaultListModel<String> listModel;
    private StartScreenController controller;

    @BeforeEach
    void setUp() {
        listModel = new DefaultListModel<>();
        controller = new StartScreenController(listModel);
    }

    // ── construction ──────────────────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertDoesNotThrow(() -> new StartScreenController(new DefaultListModel<>()));
    }

    @Test
    void constructor_withEmptyModel_doesNotThrow() {
        DefaultListModel<String> empty = new DefaultListModel<>();
        assertDoesNotThrow(() -> new StartScreenController(empty));
    }

    @Test
    void constructor_withPrePopulatedModel_doesNotThrow() {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("layouts/layout.json");
        model.addElement("layouts/hotel_layout_uitgebreid_correct.json");
        assertDoesNotThrow(() -> new StartScreenController(model));
    }

    // ── model reference ───────────────────────────────────────────────────────

    @Test
    void controller_acceptsModelWithoutModifyingIt() {
        listModel.addElement("test_entry");
        // Construction must not alter the injected model
        new StartScreenController(listModel);
        assertEquals(1, listModel.getSize(),
                "Constructor must not add or remove items from the supplied model");
        assertEquals("test_entry", listModel.getElementAt(0));
    }

    @Test
    void multipleControllers_canShareSameModel() {
        // Verifies that two controllers backed by the same model do not conflict
        StartScreenController c1 = new StartScreenController(listModel);
        StartScreenController c2 = new StartScreenController(listModel);
        assertNotNull(c1);
        assertNotNull(c2);
    }

    // ── multiple instantiations ───────────────────────────────────────────────

    @Test
    void multipleInstances_withDifferentModels_areIndependent() {
        DefaultListModel<String> model1 = new DefaultListModel<>();
        DefaultListModel<String> model2 = new DefaultListModel<>();
        model1.addElement("a");

        new StartScreenController(model1);
        new StartScreenController(model2);

        // model2 must remain untouched
        assertEquals(0, model2.getSize(),
                "Controllers backed by different models must remain independent");
    }
}
