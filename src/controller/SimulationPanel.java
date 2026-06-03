package controller;

import view.LogPanel;
import view.TimeControlPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import model.*;
import view.SimulationRenderer;

/**
 * Het hoofdpaneel van de gebruikersinterface (GUI).
 * Dit paneel voegt de graphics (renderer), het logboek en de tijdsbesturing samen.
 */
public class SimulationPanel extends JPanel {

    private final SimulationData data;
    private final SimulationRenderer renderer;
    private final SimulationController controller;
    private final LogPanel logPanel;
    private final HotelTimeEngine hte;
    private TimeControlPanel timeControlPanel; // Paneel voor de knoppen (pauze, versnellen)

    /**
     * Constructor die het hele simulatiescherm opbouwt en de gameloop start.
     */
    public SimulationPanel(
            List<Area> areas,
            int capacity,
            int cleaningSeconds
    ) {
        // 1. Initialiseer de centrale simulatiedata (kamers, liftcapaciteit en schoonmaaktijd)
        this.data = new SimulationData(areas, capacity, cleaningSeconds);

        // 2. Initialiseer de grafische tekenaar (renderer) en geef hem toegang tot de data
        this.renderer = new SimulationRenderer(data);

        // 3. Maak het tekstuele logpaneel aan voor de meldingen aan de rechterkant
        this.logPanel = new LogPanel();

        // 4. Start de hoofdcontroller die alle logica en sub-controllers aanstuurt
        this.controller = new SimulationController(data, logPanel);

        // 5. Maak de tijds-engine aan voor pauzeren en versnellen
        this.hte = new HotelTimeEngine();

        // Gebruik een BorderLayout voor de hoofdindeling (Center = simulatie, South = knoppenbalk)
        setLayout(new BorderLayout());

        // 6. Maak het specifieke sub-paneel aan waar het hotel daadwerkelijk op getekend wordt
        JPanel simulationView = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Roep de renderer aan om het hotel, de gasten en de lift in 2D op het scherm te tekenen
                renderer.render((Graphics2D) g, data);
            }
        };

        // Stel de grootte van het tekenscherm in (breedte 900px, hoogte 2000px voor hoge hotels)
        simulationView.setPreferredSize(new Dimension(900, 2000));

        // Stop het tekenscherm in een ScrollPane zodat de gebruiker omhoog/omlaag kan scrollen
        JScrollPane scrollPane = new JScrollPane(simulationView);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Zorgt voor soepel scrollen

        // 7. Maak een SplitPane aan om het scrollscherm (links) en het logPanel (rechts) te scheiden
        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        scrollPane,
                        logPanel
                );

        splitPane.setResizeWeight(1.0);     // Geef het scrollscherm prioriteit bij het vergroten van het venster
        splitPane.setDividerSize(6);        // Dikte van de scheidingslijn
        splitPane.setContinuousLayout(true); // Direct hertekenen tijdens het slepen van de lijn

        // Zet de scheidingslijn op 75% van het scherm zodra de GUI volledig is opgestart
        SwingUtilities.invokeLater(() ->
                splitPane.setDividerLocation(0.75)
        );

        // Voeg het gesplitste paneel toe aan het midden van dit SimulationPanel
        add(splitPane, BorderLayout.CENTER);

        // 8. START DE GAMELOOP (De hartslag)
        // Elke tick voert de controller de updates uit, ververst de knoppenbalk en verplicht het scherm tot hertekenen (repaint)
        GameLoop gameLoop = new GameLoop(controller, hte, () -> {
            if (timeControlPanel != null) timeControlPanel.refresh();
            repaint(); // Triggert indirect paintComponent() van simulationView
        });
        gameLoop.start();
    }

    /**
     * Factory-methode om de onderste balk met tijdsknoppen aan te maken.
     * Wordt apart aangeroepen vanuit de hoofd-JFrame.
     */
    public JPanel createBottomPanel() {
        timeControlPanel = new TimeControlPanel(hte, data);
        return timeControlPanel;
    }
}