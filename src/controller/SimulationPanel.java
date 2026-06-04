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

    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds,
            int selectedScenario,
            int cinemaDurationSeconds,      // ADD
            int restaurantDurationSeconds,  // ADD
            int fitnessDurationSeconds      // ADD
    ) {

        // Simulatie data
        this.data = new SimulationData(areas, capacity, cleaningSeconds, cinemaDurationSeconds, restaurantDurationSeconds, fitnessDurationSeconds);

        // Renderer
        this.renderer = new SimulationRenderer(data);

        // Log scherm
        this.logPanel = new LogPanel();

        // Controller
        this.controller = new SimulationController(data, logPanel, selectedScenario);

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