package controller;

import factory.RoomFactory;
import model.Area;
import model.RoomType;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Leest een lay-outbestand (custom JSON-achtig formaat) in en genereert
 * op basis daarvan alle hotelruimtes (Areas) met de juiste coördinaten.
 */
public class layoutGenerator {
    private int roomCounter = 1; // Unieke teller voor kamers/faciliteiten die geen eigen ID hebben

    /**
     * Vertaalt de tekstinhoud van een bestand naar een lijst met bruikbare Area-objecten.
     */
    public List<Area> generateLayout(String fileName) {
        List<Area> areas = new ArrayList<>();
        roomCounter = 1; // Reset de teller bij een nieuwe generatie

        try {
            // Probeer het bestand te vinden in de src-map, anders in de root-map
            File file = new File("src/" + fileName);
            if (!file.exists()) file = new File(fileName);
            if (!file.exists()) {
                System.err.println("FOUT: " + fileName + " niet gevonden!");
                return areas;
            }

            // Lees het hele bestand in als één grote tekst-String en splits per object-opening '{'
            String content = new String(Files.readAllBytes(file.toPath()));
            String[] parts = content.split("\\{");

            // Variabelen om de uiterste grenzen van het hotelgrid te bepalen (nodig voor het flippen van de Y-as)
            int maxGridX = 0;
            int maxGridY = 0;
            int minGridY = Integer.MAX_VALUE;

            // Loop door elk tekstblok (elk los hotelonderdeel) heen
            for (String p : parts) {
                if (!p.contains("AreaType")) continue; // Sla tekstblokken zonder kamertype over

                // Haal de belangrijkste waardes op met de ingebouwde val() helper
                String typeName = val(p, "AreaType").toUpperCase();
                String position  = val(p, "Position");
                String dimension = val(p, "Dimension");

                if (typeName.isEmpty()) continue;

                // ID-bepaling: gebruik het ID uit het bestand, geef kamers een oplopend nummer, de rest krijgt -1
                int id;
                if (p.contains("\"ID\"")) {
                    id = Integer.parseInt(val(p, "ID").replaceAll("[^0-9]", ""));
                } else if (typeName.matches("ROOM|CINEMA|FITNESS|RESTAURANT")) {
                    id = roomCounter++;
                } else {
                    id = -1;
                }

                // Maak de specifieke hotelruimte aan via de RoomFactory
                RoomType type = RoomType.valueOf(typeName);
                Area a = RoomFactory.createRuimte(type, position, dimension, id);

                // Capaciteit bepalen: lees uit het bestand, of gebruik hardcoded standaarden per type
                if (p.contains("\"Capacity\"")) {
                    a.capacity = Integer.parseInt(val(p, "Capacity").replaceAll("[^0-9]", ""));
                } else {
                    if (typeName.equals("CINEMA"))     a.capacity = 10;
                    else if (typeName.equals("RESTAURANT")) a.capacity = 5;
                    else a.capacity = 1; // Standaard hotelkamercapaciteit
                }

                areas.add(a);

                // Update de uiterste grenzen van het hotel op basis van deze nieuwe ruimte
                int[] pos = a.getPos();
                int[] dim = a.getDim();
                maxGridX = Math.max(maxGridX, pos[0] + dim[0]);
                maxGridY = Math.max(maxGridY, pos[1] + dim[1]);
                minGridY = Math.min(minGridY, pos[1]);
            }

            // Y-coördinaten omdraaien (Flippen): In bestanden is Verdieping 0 vaak de top,
            // maar in graphics/coördinatenstelsels bouwen we het hotel vanaf de bodem (Lobby) op.
            for (Area a : areas) {
                int[] pos = a.getPos();
                int[] dim = a.getDim();
                int flippedY = (maxGridY - minGridY) - pos[1] - dim[1] + minGridY;
                a.Position = pos[0] + ", " + flippedY; // Sla de gecorrigeerde positie weer op als String
            }

            int flippedMax = maxGridY - minGridY;

            // Voeg de vaste, verplichte infrastructuur toe die ALTIJD in het hotel moet zitten via de Factory
            // Deze krijgen een uniek negatief ID om verwarring met kamers te voorkomen
            areas.add(RoomFactory.createRuimte(RoomType.LIFTSCHACHT,
                    "0, 0", "1, " + flippedMax, -99)); // Gehele linkerkant
            areas.add(RoomFactory.createRuimte(RoomType.TRAP,
                    maxGridX + ", 0", "1, " + flippedMax, -98)); // Gehele rechterkant
            areas.add(RoomFactory.createRuimte(RoomType.LOBBY,
                    "0, " + flippedMax, maxGridX + ", 1", -100)); // Onderkant/begane grond
            areas.add(RoomFactory.createRuimte(RoomType.RECEPTION,
                    maxGridX + ", " + flippedMax, "1, 1", -101)); // Rechts onderin de lobby

        } catch (Exception e) {
            System.err.println("Fout bij genereren layout: " + e.getMessage());
            e.printStackTrace();
        }

        return areas;
    }

    /**
     * Handmatige mini-parser helper. Zoekt binnen een tekstblok naar een specifieke sleutel (key)
     * en plukt de bijbehorende waarde (value) ertussenuit (simuleert een basis JSON-lezer).
     */
    private String val(String block, String key) {
        try {
            int keyIdx = block.indexOf("\"" + key + "\"");
            if (keyIdx == -1) return "";
            int colonIdx = block.indexOf(":", keyIdx);

            // Bepaal waar de tekstwaarde begint (rekening houdend met wel of geen aanhalingstekens)
            int start = (block.indexOf("\"", colonIdx) != -1
                    && block.indexOf("\"", colonIdx) < block.indexOf(",", colonIdx))
                    ? block.indexOf("\"", colonIdx) + 1
                    : colonIdx + 1;

            // Bepaal waar de tekstwaarde eindigt (bij een sluit-quote, komma of sluit-accolade)
            int end = block.indexOf("\"", start) != -1
                    ? block.indexOf("\"", start)
                    : (block.indexOf(",", start) != -1
                    ? block.indexOf(",", start)
                    : block.indexOf("}", start));

            return block.substring(start, end).trim().replace("\"", "");
        } catch (Exception e) {
            return ""; // Retourneer een lege string bij parsing-fouten van dit specifieke veld
        }
    }
}