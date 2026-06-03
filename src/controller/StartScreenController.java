package controller;

import model.Area;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Beheert de logica van het startscherm. Regelt het selecteren van het
 * lay-outbestand en het opstarten van het uiteindelijke simulatieframvenster.
 */
public class StartScreenController {

    private final DefaultListModel<String> model;
    private String selectedLayoutFile = "layouts/layout.json"; // Standaard back-up bestand

    public StartScreenController(DefaultListModel<String> model) {
        this.model = model;
    }

    /**
     * Start het proces: opent een bestandskiezer en bouwt bij succes het hoofdscherm van de simulatie.
     * * @param parent          Het huidige JFrame van het startscherm (om te kunnen sluiten).
     * @param capacity        De maximale liftcapaciteit die is ingevoerd door de gebruiker.
     * @param cleaningSeconds De schoonmaaktijd in seconden die is ingevoerd door de gebruiker.
     */
    public void handleStart(JFrame parent, int capacity, int cleaningSeconds) {

        // --- STAP 1: BESTANDSKIEZER (FILE PICKER) ---
        JFileChooser chooser = new JFileChooser();

        // Bepaal de hoofdmap van het project om direct in de juiste map te starten
        String projectPath = System.getProperty("user.dir");
        File defaultFolder = new File(projectPath + File.separator + "src" + File.separator + "layouts");

        // Als de map 'src/layouts' bestaat, open de verkenner daar, anders in de project-root
        if (defaultFolder.exists()) {
            chooser.setCurrentDirectory(defaultFolder);
        } else {
            chooser.setCurrentDirectory(new File(projectPath));
        }

        // Zorg ervoor dat de gebruiker in de verkenner alleen JSON-bestanden kan selecteren
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("JSON files", "json")
        );

        // Open het pop-up venster van de bestandskiezer
        int fileResult = chooser.showOpenDialog(parent);

        // Als de gebruiker op 'Annuleren' klikt of het venster sluit, breek de methode dan direct af
        if (fileResult != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Sla het absolute bestandspad op van het gekozen lay-outbestand
        selectedLayoutFile = chooser.getSelectedFile().getPath();

        // --- STAP 2: START SIMULATIE ---
        parent.dispose(); // Sluit en vernietig het startschermvenster om geheugen vrij te maken

        // Roep de lay-outgenerator aan om het geselecteerde bestand te parsen naar echte hotelruimtes (Areas)
        layoutGenerator gen = new layoutGenerator();
        List<Area> areas = gen.generateLayout(selectedLayoutFile);

        if (areas.isEmpty()) {
            System.err.println("WAARSCHUWING: Geen areas geladen!");
        }

        // Maak het hoofd-simulatiepaneel aan en geef de ingeladen ruimtes en gebruikersinstellingen mee
        SimulationPanel panel = new SimulationPanel(areas, capacity, cleaningSeconds);

        // Bouw het gloednieuwe hoofdvenster (JFrame) voor de hotelsimulator op
        JFrame frame = new JFrame("Hotel Simulator");
        frame.setLayout(new BorderLayout());

        // Voeg de graphics en het logboek (Center) en de tijdbedieningsknoppen (South) toe
        frame.add(panel, BorderLayout.CENTER);
        frame.add(panel.createBottomPanel(), BorderLayout.SOUTH);

        // Vensterinstellingen
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null); // Centreer het venster netjes in het midden van het computerscherm
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stop het Java-proces volledig bij het sluiten via het kruisje
        frame.setVisible(true); // Maak het venster daadwerkelijk zichtbaar voor de gebruiker
    }
}