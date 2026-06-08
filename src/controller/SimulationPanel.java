package controller;

import view.LogPanel;
import view.RoomOverviewPanel;
import view.TimeControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final HotelTimeEngine hte;

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

        this.controller = new SimulationController(data, logPanel, selectedScenario);
        this.renderer = new SimulationRenderer(data, controller.getCleanerController());

        // Zoek de lobby en spawn schoonmakers
        Area lobbyArea = null;
        for (int i = 0; i < this.data.areas.size(); i++) {
            if (this.data.areas.get(i).AreaType.equalsIgnoreCase("LOBBY")) {
                lobbyArea = this.data.areas.get(i);
                break;
            }
        }

        if (lobbyArea != null) {
            double lobbyX = (lobbyArea.getPos()[0] * data.tileSize) + data.horizontalOffset + ((lobbyArea.getDim()[0] * data.tileSize) / 2.0);
            double lobbyY = (lobbyArea.getPos()[1] * data.tileSize) + 25.0;

            for (int i = 1; i <= data.numberOfCleaners; i++) {
                Cleaner cleaner = (Cleaner) PersonFactory.createPerson(PersonType.CLEANER, i, lobbyX, lobbyY);
                cleaner.speed = 2.0;
                cleaner.setTarget(lobbyX, lobbyY);
                cleaner.state = CleanerState.IDLE;
                data.cleaners.put(cleaner.id, cleaner);
            }
        }

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

        simulationView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLobbyClicked(e.getX(), e.getY())) {
                    openRoomOverview();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(simulationView);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPane,
                logPanel
        );

        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.75));

        add(splitPane, BorderLayout.CENTER);

        GameLoop gameLoop = new GameLoop(controller, hte, () -> {
            if (timeControlPanel != null) timeControlPanel.refresh();
            repaint();
        });

        gameLoop.start();
    }

    private boolean isLobbyClicked(int mouseX, int mouseY) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (a.AreaType.equalsIgnoreCase("LOBBY")) {
                int x = (a.getPos()[0] * data.tileSize) + data.horizontalOffset;
                int y = a.getPos()[1] * data.tileSize;
                int w = a.getDim()[0] * data.tileSize;
                int h = a.getDim()[1] * data.tileSize;
                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    return true;
                }
            }
        }
        return false;
    }

    private void openRoomOverview() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Kamer Overzicht",
                Dialog.ModalityType.MODELESS
        );
        dialog.setContentPane(new RoomOverviewPanel(data));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public JPanel createBottomPanel() {
        timeControlPanel = new TimeControlPanel(hte, data);
        return timeControlPanel;
    }
}