package model;

import model.Area;
import model.SimulationData;
import java.util.*;

public class RuimteService {
    public static Optional<Area> reserveerVrijeKamer(SimulationData data, int voorkeurId, int gastId) {
        // 1. Check of de voorkeurskamer toevallig vrij is
        Optional<Area> voorkeur = data.areas.stream()
                .filter(a -> a.id == voorkeurId && a.AreaType.equalsIgnoreCase("ROOM") && !a.isFull())
                .findFirst();

        if (voorkeur.isPresent()) {
            voorkeur.get().currentOccupants.add(gastId);
            return voorkeur;
        }

        // 2. Indien vol: Zoek een WILLEKEURIGE andere vrije kamer
        List<Area> vrijeKamers = data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM") && !a.isFull())
                .collect(java.util.stream.Collectors.toList());

        if (!vrijeKamers.isEmpty()) {
            // Pak een willekeurige index uit de lijst met vrije kamers
            Area gekozenKamer = vrijeKamers.get(new Random().nextInt(vrijeKamers.size()));
            gekozenKamer.currentOccupants.add(gastId);
            System.out.println("[RuimteFactory] Gast " + gastId + " naar willekeurige vrije kamer: " + gekozenKamer.id);
            return Optional.of(gekozenKamer);
        }

        System.err.println("[RuimteFactory] GEEN KAMERS VRIJ voor gast " + gastId);
        return Optional.empty();
    }

    public static void maakGastVrij(SimulationData data, int gastId) {
        for (Area a : data.areas) {
            a.currentOccupants.remove(Integer.valueOf(gastId));
        }
    }

    public static Optional<Area> vindVrijeActiviteit(SimulationData data, String type, int gastId) {
        maakGastVrij(data, gastId);
        return data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase(type) && !a.isFull())
                .findAny();
    }
}

