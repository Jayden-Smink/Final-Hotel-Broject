package controller;

import factory.RoomFactory;
import model.Area;
import model.RoomType;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class layoutGenerator {
    private int roomCounter = 1;

    public List<Area> generateLayout(String fileName) {
        List<Area> areas = new ArrayList<>();
        roomCounter = 1;

        try {
            File file = new File("src/" + fileName);
            if (!file.exists()) file = new File(fileName);
            if (!file.exists()) {
                System.err.println("FOUT: " + fileName + " niet gevonden!");
                return areas;
            }

            String content = new String(Files.readAllBytes(file.toPath()));
            String[] parts = content.split("\\{");

            int maxGridX = 0;
            int maxGridY = 0;
            int minGridY = Integer.MAX_VALUE;

            for (String p : parts) {
                if (!p.contains("AreaType")) continue;

                String typeName = val(p, "AreaType").toUpperCase();
                String position  = val(p, "Position");
                String dimension = val(p, "Dimension");

                if (typeName.isEmpty()) continue;

                // Bepaal id
                int id;
                if (p.contains("\"ID\"")) {
                    id = Integer.parseInt(val(p, "ID").replaceAll("[^0-9]", ""));
                } else if (typeName.matches("ROOM|CINEMA|FITNESS|RESTAURANT")) {
                    id = roomCounter++;
                } else {
                    id = -1;
                }

                // Maak Area via RoomFactory
                RoomType type = RoomType.valueOf(typeName);
                Area a = RoomFactory.createRuimte(type, position, dimension, id);

                // Capaciteit bepalen
                if (p.contains("\"Capacity\"")) {
                    a.capacity = Integer.parseInt(val(p, "Capacity").replaceAll("[^0-9]", ""));
                } else {
                    if (typeName.equals("CINEMA"))     a.capacity = 10;
                    else if (typeName.equals("RESTAURANT")) a.capacity = 5;
                    else a.capacity = 1;
                }

                areas.add(a);

                // Update grid grenzen
                int[] pos = a.getPos();
                int[] dim = a.getDim();
                maxGridX = Math.max(maxGridX, pos[0] + dim[0]);
                maxGridY = Math.max(maxGridY, pos[1] + dim[1]);
                minGridY = Math.min(minGridY, pos[1]);
            }

            // Flip Y coordinates
            for (Area a : areas) {
                int[] pos = a.getPos();
                int[] dim = a.getDim();
                int flippedY = (maxGridY - minGridY) - pos[1] - dim[1] + minGridY;
                a.Position = pos[0] + ", " + flippedY;
            }

            int flippedMax = maxGridY - minGridY;

// Vaste onderdelen via RoomFactory
            areas.add(RoomFactory.createRuimte(RoomType.LIFTSCHACHT,
                    "0, 0", "1, " + flippedMax, -99));
            areas.add(RoomFactory.createRuimte(RoomType.TRAP,
                    maxGridX + ", 0", "1, " + flippedMax, -98));
            areas.add(RoomFactory.createRuimte(RoomType.LOBBY,
                    "0, " + flippedMax, maxGridX + ", 1", -100));
            areas.add(RoomFactory.createRuimte(RoomType.RECEPTION,
                    maxGridX + ", " + flippedMax, "1, 1", -101));
        } catch (Exception e) {
            System.err.println("Fout bij genereren layout: " + e.getMessage());
            e.printStackTrace();
        }

        return areas;
    }

    private String val(String block, String key) {
        try {
            int keyIdx = block.indexOf("\"" + key + "\"");
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