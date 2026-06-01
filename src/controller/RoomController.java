package controller;

import model.Area;
import model.IRoomAssigner;
import model.SimulationData;

import java.util.*;

public class RoomController implements IRoomAssigner {

    // Probeert eerst voorkeurskamer, anders random vrije kamer
    public Optional<Area> reserveerVrijeKamer(SimulationData data, int voorkeurId, int gastId) {

        // 1. Check voorkeurskamer
        Optional<Area> voorkeur = data.areas.stream()
                .filter(a -> a.id == voorkeurId)
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.isFull())
                .findFirst();

        if (voorkeur.isPresent()) {
            voorkeur.get().currentOccupants.add(gastId);
            return voorkeur;
        }

        // 2. Zoek andere vrije kamer
        List<Area> vrijeKamers = data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.isFull())
                .toList();

        if (!vrijeKamers.isEmpty()) {
            Area randomKamer = vrijeKamers.get(new Random().nextInt(vrijeKamers.size()));
            randomKamer.currentOccupants.add(gastId);
            System.out.println("[RoomController] Gast " + gastId + " naar kamer " + randomKamer.id);
            return Optional.of(randomKamer);
        }

        System.err.println("[RoomController] GEEN KAMERS VRIJ voor gast " + gastId);
        return Optional.empty();
    }

    // Gast uit alle kamers/faciliteiten verwijderen
    public  void maakGastVrij(SimulationData data, int gastId) {
        for (Area a : data.areas) {
            a.currentOccupants.remove(Integer.valueOf(gastId));
        }
    }

    // Vrije faciliteit zoeken (restaurant, cinema, etc.)
    public  Optional<Area> vindVrijeActiviteit(SimulationData data, String type, int gastId) {
        maakGastVrij(data, gastId);

        return data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase(type))
                .filter(a -> !a.isFull())
                .findFirst();
    }
}