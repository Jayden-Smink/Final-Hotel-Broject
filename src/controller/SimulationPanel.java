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
    private GameLoop gameLoop;

    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds,
            int cleanerCount,
            int selectedScenario,
            int cinemaDurationSeconds,
            int restaurantDurationSeconds,
            int fitnessDurationSeconds,
            int elevatorWaitSeconds
    ) {
        this.data = new SimulationData(
                areas,
                capacity,
                cleaningSeconds,
                cinemaDurationSeconds,
                restaurantDurationSeconds,
                fitnessDurationSeconds,
                elevatorWaitSeconds
        );

        this.data.numberOfCleaners = cleanerCount;
        this.logPanel = new LogPanel();

        this.controller = new SimulationController(data, logPanel, selectedScenario);
        this.renderer = new SimulationRenderer(data, controller.getCleanerController());
        // Hotel grid height = highest Y tile position (lobby sits at flippedMax, which is the tallest Y)
        int hotelGridHeight = data.areas.stream()
                .mapToInt(a -> a.getPos()[1] + a.getDim()[1])
                .max().orElse(10);
        this.renderer.setGodzillaController(controller.getGodzillaController(), data.tileSize, data.horizontalOffset, hotelGridHeight);

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

                // Draw the "HOTEL DESTROYED" overlay once Godzilla is done
                if (controller.isGodzillaDone()) {
                    drawHotelDestroyedOverlay((Graphics2D) g, getWidth(), getHeight());
                }
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

        gameLoop = new GameLoop(controller, hte, () -> {
            if (timeControlPanel != null) timeControlPanel.refresh();
            repaint();
            // Stop everything once Godzilla has finished — do one final repaint first
            if (controller.isGodzillaDone()) {
                gameLoop.stop();
                SwingUtilities.invokeLater(() -> repaint()); // ensure overlay renders
            }
        });

        gameLoop.start();
    }

    private void drawHotelDestroyedOverlay(Graphics2D g2, int panelWidth, int panelHeight) {
        // Dark semi-transparent full screen cover
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g2.setColor(new Color(10, 0, 0));
        g2.fillRect(0, 0, panelWidth, panelHeight);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Box
        int boxW = 520, boxH = 180;
        int boxX = (panelWidth - boxW) / 2;
        int boxY = (panelHeight - boxH) / 2;

        g2.setColor(new Color(30, 0, 0));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 24, 24);
        g2.setColor(new Color(180, 20, 20));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 24, 24);

        // Title line
        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
        g2.setColor(new Color(255, 60, 60));
        String line1 = "🦖  HOTEL DESTROYED";
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(line1, boxX + (boxW - fm1.stringWidth(line1)) / 2, boxY + 70);

        // Subtitle line
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2.setColor(new Color(220, 180, 180));
        String line2 = "ONLY GODZILLA REMAINS";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(line2, boxX + (boxW - fm2.stringWidth(line2)) / 2, boxY + 120);
    }

    private boolean isLobbyClicked(int mouseX, int mouseY) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            if (area.AreaType.equalsIgnoreCase("LOBBY")) {
                int x = (area.getPos()[0] * data.tileSize) + data.horizontalOffset;
                int y = area.getPos()[1] * data.tileSize;
                int w = area.getDim()[0] * data.tileSize;
                int h = area.getDim()[1] * data.tileSize;
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

        JButton godzillaButton = new JButton("🦖 Test Godzilla");
        godzillaButton.setToolTipText("Start de Godzilla aanval en stop de simulatie daarna");
        godzillaButton.setBackground(new Color(180, 40, 40));
        godzillaButton.setForeground(Color.WHITE);
        godzillaButton.setFocusPainted(false);
        godzillaButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        godzillaButton.addActionListener(e -> {
            controller.triggerGodzilla();
            godzillaButton.setEnabled(false);
            godzillaButton.setText("🦖 Godzilla is onderweg...");
            if (logPanel != null) logPanel.addLog("🦖 Godzilla test gestart!");
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(timeControlPanel, BorderLayout.CENTER);
        bottomPanel.add(godzillaButton, BorderLayout.EAST);
        return bottomPanel;
    }
}