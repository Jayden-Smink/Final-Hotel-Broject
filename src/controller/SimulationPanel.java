package controller;

import view.LogPanel;
import view.TimeControlPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import model.*;
import view.SimulationRenderer;

public class SimulationPanel extends JPanel {

    private final SimulationData data;
    private final SimulationRenderer renderer;
    private final SimulationController controller;
    private final LogPanel logPanel;
    private final HotelTimeEngine hte;

    private TimeControlPanel timeControlPanel;

    // GEFIXT: De constructor accepteert nu de 3 extra parameters die SimulationData nodig heeft
    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds,
            int extraParam1, // TODO: Kijk in SimulationData.java hoe deze 3 ints heten (bijv. elevatorCapacity, etc.)
            int extraParam2,
            int extraParam3,
            int selectedScenario
    ) {

        // 1. Simulatie data initialiseren (GEFIXT: Nu met alle 5 de vereiste ints)
        this.data = new SimulationData(areas, capacity, cleaningSeconds, extraParam1, extraParam2, extraParam3);

        // 2. Log scherm initialiseren
        this.logPanel = new LogPanel();

        // 3. Controller initialiseren
        this.controller = new SimulationController(data, logPanel, selectedScenario);

        // 4. Renderer initialiseren (met de cleanerController koppeling voor de twee schoonmakers)
        this.renderer = new SimulationRenderer(data, controller.getCleanerController());

        // HTE
        this.hte = new HotelTimeEngine();

        // Layout
        setLayout(new BorderLayout());

        // Simulatie scherm
        JPanel simulationView = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderer.render((Graphics2D) g, data);
            }
        };

        simulationView.setPreferredSize(new Dimension(900, 2000));

        JScrollPane scrollPane = new JScrollPane(simulationView);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        scrollPane,
                        logPanel
                );

        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);

        SwingUtilities.invokeLater(() ->
                splitPane.setDividerLocation(0.75)
        );

        add(splitPane, BorderLayout.CENTER);

        // Game loop
        GameLoop gameLoop = new GameLoop(controller, hte, () -> {
            if (timeControlPanel != null) {
                timeControlPanel.refresh();
            }
            repaint();
        });

        gameLoop.start();
    }

    public JPanel createBottomPanel() {
        timeControlPanel = new TimeControlPanel(hte, data);
        return timeControlPanel;
    }
}