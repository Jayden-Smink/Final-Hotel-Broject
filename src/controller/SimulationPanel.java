package controller;

import view.LogPanel;
import view.TimeControlPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import model.*;
import view.SimulationRenderer;
import factory.PersonFactory;
import model.PersonType;

public class SimulationPanel extends JPanel {

    private final SimulationData data;
    private final SimulationRenderer renderer;
    private final SimulationController controller;
    private final LogPanel logPanel;
    private final HotelTimeEngine hte; // Staat weer zoals vanouds

    private TimeControlPanel timeControlPanel;

    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds,
            int cleanerCount,
            int selectedScenario,
            int cinemaDurationSeconds,
            int restaurantDurationSeconds,
            int fitnessDurationSeconds
    ) {
        // 1. Simulatie data initialiseren
        this.data = new SimulationData(
                areas,
                capacity,
                cleaningSeconds,
                cinemaDurationSeconds,
                restaurantDurationSeconds,
                fitnessDurationSeconds
        );

        this.data.numberOfCleaners = cleanerCount;
        this.logPanel = new LogPanel();

        // 2. Controller & Renderer initialiseren
        this.controller = new SimulationController(data, logPanel, selectedScenario);
        this.renderer = new SimulationRenderer(data, controller.getCleanerController());

        // 3. Schoonmakers dynamisch spawnen in de lobby
        this.data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                .findFirst()
                .ifPresent(lobby -> {
                    double lobbyX = (lobby.getPos()[0] * data.tileSize) + data.horizontalOffset + ((lobby.getDim()[0] * data.tileSize) / 2.0);
                    double lobbyY = (lobby.getPos()[1] * data.tileSize) + 25.0;

                    for (int i = 1; i <= data.numberOfCleaners; i++) {
                        int cleanerId = i; // Nette unieke IDs (c1, c2...)

                        Cleaner cleaner = (Cleaner) PersonFactory.createPerson(PersonType.CLEANER, cleanerId, lobbyX, lobbyY);
                        cleaner.speed = 2.0;
                        cleaner.setTarget(lobbyX, lobbyY);
                        cleaner.state = CleanerState.IDLE;

                        data.cleaners.put(cleaner.id, cleaner);
                    }
                });

        // 4. HTE & Layout opbouwen
        this.hte = new HotelTimeEngine();

        setLayout(new BorderLayout());

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

        // Game loop starten
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