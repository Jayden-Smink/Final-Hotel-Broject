package controller;

import factory.RoomFactory;
import model.Area;
import model.RoomType;

import java.util.List;

/**
 * Verantwoordelijkheid: Coördinaten corrigeren en vaste infrastructuur toevoegen.
 *
 * Twee taken zitten hier samen omdat ze allebei dezelfde gridafmetingen nodig
 * hebben én altijd als één ondeelbare stap uitgevoerd worden. Als ze later
 * toch apart nodig zijn, is de splitsing triviaal.
 */
public class LayoutFlipper {

    /**
     * Draait de Y-as om (bestandscoördinaten → schermcoördinaten) en voegt
     * de verplichte infrastructuur (lift, trap, lobby, receptie) toe.
     *
     * @param areas De lijst direct uit de parser — wordt in-place aangepast.
     */
    public void transformAndAddInfrastructure(List<Area> areas) {
        int[] bounds = calculateBounds(areas);
        int maxGridX = bounds[0];
        int maxGridY = bounds[1];
        int minGridY = bounds[2];

        flipYCoordinates(areas, maxGridY, minGridY);

        int flippedMax = maxGridY - minGridY;
        appendFixedInfrastructure(areas, maxGridX, flippedMax);
    }

    // --- Grenzen bepalen ----------------------------------------------------

    private int[] calculateBounds(List<Area> areas) {
        int maxGridX = 0;
        int maxGridY = 0;
        int minGridY = Integer.MAX_VALUE;

        for (Area area : areas) {
            int[] pos = area.getPos();
            int[] dim = area.getDim();
            maxGridX = Math.max(maxGridX, pos[0] + dim[0]);
            maxGridY = Math.max(maxGridY, pos[1] + dim[1]);
            minGridY = Math.min(minGridY, pos[1]);
        }

        return new int[]{ maxGridX, maxGridY, minGridY };
    }

    // --- Y-as omdraaien ------------------------------------------------------

    private void flipYCoordinates(List<Area> areas, int maxGridY, int minGridY) {
        for (Area area : areas) {
            int[] pos = area.getPos();
            int[] dim = area.getDim();
            int flippedY = (maxGridY - minGridY) - pos[1] - dim[1] + minGridY;
            area.Position = pos[0] + ", " + flippedY;
        }
    }

    // --- Vaste infrastructuur toevoegen -------------------------------------

    private void appendFixedInfrastructure(List<Area> areas, int maxGridX, int flippedMax) {
        areas.add(RoomFactory.createRuimte(RoomType.LIFTSCHACHT,
                "0, 0",               "1, " + flippedMax,      -99));
        areas.add(RoomFactory.createRuimte(RoomType.TRAP,
                maxGridX + ", 0",     "1, " + flippedMax,      -98));
        areas.add(RoomFactory.createRuimte(RoomType.LOBBY,
                "0, " + flippedMax,   maxGridX + ", 1",         -100));
        areas.add(RoomFactory.createRuimte(RoomType.RECEPTION,
                maxGridX + ", " + flippedMax, "1, 1",           -101));
    }
}