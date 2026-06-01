package controller;

import view.LogPanel;

import javax.swing.*;
import java.awt.*;
import java.security.Provider;
import java.util.List;

import model.*;
import view.SimulationRenderer;

public class SimulationPanel extends JPanel {

    private final SimulationData data;
    private final SimulationRenderer renderer;
    private final SimulationController controller;
    private final LogPanel logPanel;

    private JLabel statusLabel;

    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds // ADD THIS
    ) {

        // Simulatie data
        this.data = new SimulationData(areas, capacity, cleaningSeconds); // ADD cleaningSeconds

        // Renderer
        this.renderer = new SimulationRenderer(data);

        // Log scherm
        this.logPanel = new LogPanel();

        // Controller
        this.controller = new SimulationController(data, logPanel);

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

        new Timer(16, e -> {

            controller.updateTick();

            if (statusLabel != null) {
                long activeGuests =
                        data.guests.values()
                                .stream()
                                .filter(g -> !g.isCheckingOut)
                                .count();

                statusLabel.setText("Hotel Status: Actief | Gasten: " + activeGuests);
            }

            repaint();

        }).start();
    }

    public JPanel createBottomPanel() {

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout(FlowLayout.LEFT));

        if (statusLabel == null) {
            statusLabel = new JLabel("Hotel Status: Initialiseren...");
        }

        bottom.add(statusLabel);
        return bottom;
    }
}