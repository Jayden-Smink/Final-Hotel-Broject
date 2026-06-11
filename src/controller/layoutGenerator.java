package controller;

import model.Area;

import java.util.ArrayList;
import java.util.List;

/**
 * Verantwoordelijkheid: Orkestreren van het layout-generatieproces.
 *
 * Deze klasse doet zelf niets anders dan de drie stappen in de juiste volgorde
 * aanroepen. Elke stap heeft zijn eigen klasse met één duidelijke taak:
 *
 *   1. {@link LayoutFileReader}              – bestand van schijf lezen
 *   2. {@link LayoutParser}                  – tekst omzetten naar Area-objecten
 *   3. {@link LayoutFlipper}   – Y-as omdraaien + infrastructuur toevoegen
 */
public class layoutGenerator {

    private final LayoutFileReader           fileReader;
    private final LayoutParser               parser;
    private final LayoutFlipper transformer;

    public layoutGenerator() {
        this(new LayoutFileReader(), new LayoutParser(), new LayoutFlipper());
    }

    /** Constructor voor dependency injection (handig voor tests). */
    public layoutGenerator(LayoutFileReader fileReader,
                           LayoutParser parser,
                           LayoutFlipper transformer) {
        this.fileReader  = fileReader;
        this.parser      = parser;
        this.transformer = transformer;
    }

    /**
     * Leest een layoutbestand in en geeft de volledig verwerkte lijst met Area-objecten terug.
     */
    public List<Area> generateLayout(String fileName) {
        try {
            String content   = fileReader.read(fileName);
            List<Area> areas = parser.parse(content);
            transformer.transformAndAddInfrastructure(areas);
            return areas;
        } catch (Exception e) {
            System.err.println("Fout bij genereren layout: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}