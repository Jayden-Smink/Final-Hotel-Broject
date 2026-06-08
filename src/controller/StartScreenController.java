package controller;

import model.Area;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class StartScreenController {

    private final DefaultListModel<String> model;

    private String selectedLayoutFile = "layouts/layout.json";

    public StartScreenController(DefaultListModel<String> model) {
        this.model = model;
    }

    // METHODE-HANDTEKENING AANGEPAST: int cleanerCount is nu toegevoegd als 4e parameter
    public void handleStart(JFrame parent, int capacity, int cleaningSeconds, int cleanerCount, int selectedScenario,
                            int cinemaDurationSeconds, int restaurantDurationSeconds, int fitnessDurationSeconds) {

        // 1. FILE PICKER
        JFileChooser chooser = new JFileChooser();

        String projectPath = System.getProperty("user.dir");
        File defaultFolder = new File(projectPath + File.separator + "src" + File.separator + "layouts");

        if (defaultFolder.exists()) {
            chooser.setCurrentDirectory(defaultFolder);
        } else {
            chooser.setCurrentDirectory(new File(projectPath));
        }

        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json")
        );

        int fileResult = chooser.showOpenDialog(parent);

        if (fileResult != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedLayoutFile = chooser.getSelectedFile().getPath();

        // 2. START SIMULATIE
        parent.dispose();

        layoutGenerator gen = new layoutGenerator();
        List<Area> areas = gen.generateLayout(selectedLayoutFile);

        if (areas.isEmpty()) {
            System.err.println("WAARSCHUWING: Geen areas geladen!");
        }

        // AANGEPAST: cleanerCount wordt nu meegegeven aan de SimulationPanel constructor
        SimulationPanel panel = new SimulationPanel(
                areas,
                capacity,
                cleaningSeconds,
                cleanerCount, // NIEUW
                selectedScenario,
                cinemaDurationSeconds,
                restaurantDurationSeconds,
                fitnessDurationSeconds
        );

        JFrame frame = new JFrame("Hotel Simulator");
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(panel.createBottomPanel(), BorderLayout.SOUTH);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}