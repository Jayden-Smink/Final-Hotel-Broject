package controller;

import factory.RoomFactory;
import model.Area;
import model.RoomType;

import java.util.ArrayList;
import java.util.List;

/**
 * Verantwoordelijkheid: De ruwe tekst van een layoutbestand omzetten naar Area-objecten.
 * Weet niets over bestanden, coördinatentransformaties, of vaste infrastructuur.
 */
public class LayoutParser {

    private int roomCounter;

    public LayoutParser() {
        this.roomCounter = 1;
    }

    /**
     * Parseert de volledige inhoud van een layoutbestand naar een lijst Area-objecten.
     * De Y-coördinaten zijn op dit punt nog ongetransformeerd (zoals in het bestand staat).
     */
    public List<Area> parse(String content) {
        List<Area> areas = new ArrayList<>();
        roomCounter = 1;

        String[] parts = content.split("\\{");

        for (String part : parts) {
            if (!part.contains("AreaType")) continue;

            String typeName  = val(part, "AreaType").toUpperCase();
            String position  = val(part, "Position");
            String dimension = val(part, "Dimension");

            if (typeName.isEmpty()) continue;

            int id = resolveId(part, typeName);

            RoomType type = RoomType.valueOf(typeName);
            Area area = RoomFactory.createRuimte(type, position, dimension, id);
            area.Capacity = resolveCapacity(part, typeName);

            areas.add(area);
        }

        return areas;
    }

    // --- ID-bepaling --------------------------------------------------------

    private int resolveId(String block, String typeName) {
        if (block.contains("\"ID\"")) {
            return Integer.parseInt(val(block, "ID").replaceAll("[^0-9]", ""));
        }
        if (typeName.matches("ROOM|CINEMA|FITNESS|RESTAURANT")) {
            return roomCounter++;
        }
        return -1;
    }

    // --- Capaciteitsbepaling ------------------------------------------------

    private int resolveCapacity(String block, String typeName) {
        if (block.contains("\"Capacity\"")) {
            return Integer.parseInt(val(block, "Capacity").replaceAll("[^0-9]", ""));
        }
        switch (typeName) {
            case "CINEMA":     return 10;
            case "RESTAURANT": return 5;
            default:           return 1;
        }
    }

    // --- Mini-parser helper -------------------------------------------------

    /**
     * Zoekt binnen een tekstblok naar een sleutel en geeft de bijbehorende waarde terug.
     */
    private String val(String block, String key) {
        try {
            int keyIdx   = block.indexOf("\"" + key + "\"");
            if (keyIdx == -1) return "";
            int colonIdx = block.indexOf(":", keyIdx);

            int start = (block.indexOf("\"", colonIdx) != -1
                    && block.indexOf("\"", colonIdx) < block.indexOf(",", colonIdx))
                    ? block.indexOf("\"", colonIdx) + 1
                    : colonIdx + 1;

            int end = block.indexOf("\"", start) != -1
                    ? block.indexOf("\"", start)
                    : (block.indexOf(",", start) != -1
                    ? block.indexOf(",", start)
                    : block.indexOf("}", start));

            return block.substring(start, end).trim().replace("\"", "");
        } catch (Exception e) {
            return "";
        }
    }
}