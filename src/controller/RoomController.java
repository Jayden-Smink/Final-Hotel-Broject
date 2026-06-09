package controller;

import model.Area;
import model.IRoomAssigner;
import model.SimulationData;

import java.util.*;

/**
 * Verantwoordelijk voor het kamer- en faciliteitenbeheer binnen het hotel.
 * Regelt het zoeken, reserveren en vrijmaken van ruimtes voor gasten.
 */
public class RoomController implements IRoomAssigner {

    /**
     * Probeert een kamer te reserveren voor een gast. Kijkt eerst naar de voorkeurskamer,
     * en pakt anders een willekeurige (random) andere vrije kamer.
     */
    public Optional<Area> reserveerVrijeKamer(SimulationData data, int voorkeurId, int gastId) {

        // STAP 1: Controleer of de voorkeurskamer bestaat, een "ROOM" is en nog niet vol zit
        Optional<Area> voorkeur = data.areas.stream()
                .filter(a -> a.id == voorkeurId)
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.isFull())
                .findFirst();

        // Als de voorkeurskamer beschikbaar is, voeg de gast direct toe en geef de kamer terug
        if (voorkeur.isPresent()) {
            voorkeur.get().currentOccupants.add(gastId);
            return voorkeur;
        }

        // STAP 2: Voorkeurskamer is bezet/niet bruikbaar. Filter alle overige kamers die nog vrij zijn
        List<Area> vrijeKamers = data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("ROOM"))
                .filter(a -> !a.isFull())
                .toList();

        // Als er nog vrije kamers zijn, kies er eentje willekeurig uit
        if (!vrijeKamers.isEmpty()) {
            Area randomKamer = vrijeKamers.get(new Random().nextInt(vrijeKamers.size()));
            randomKamer.currentOccupants.add(gastId); // Registreer de gast in deze kamer
            System.out.println("[RoomController] Gast " + gastId + " naar kamer " + randomKamer.id);
            return Optional.of(randomKamer);
        }

        // STAP 3: Het hotel zit volledig vol
        System.err.println("[RoomController] GEEN KAMERS VRIJ voor gast " + gastId);
        return Optional.empty();
    }

    /**
     * Verwijdert een gast volledig uit alle kamers en faciliteiten waar hij/zij momenteel in staat.
     * Wordt gebruikt bij het wisselen van activiteit of bij het definitief uitchecken.
     */
    public void maakGastVrij(SimulationData data, int gastId) {
        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            // Integer.valueOf is nodig zodat Java het getal als een Object-waarde ziet
            // en niet per ongeluk een index probeert te verwijderen.
            area.currentOccupants.remove(Integer.valueOf(gastId));
        }
    }

    /**
     * Zoekt een vrije faciliteit (zoals RESTAURANT, CINEMA of FITNESS) op basis van het type.
     * Maakt de gast voor de zekerheid eerst overal los voordat hij ergens anders wordt aangemeld.
     */
    public Optional<Area> vindVrijeActiviteit(SimulationData data, String type, int gastId) {
        maakGastVrij(data, gastId); // Voorkom dat de gast op twee plekken tegelijk geregistreerd staat

        // Zoek de eerste ruimte van dit type die nog capaciteit over heeft
        return data.areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase(type))
                .filter(a -> !a.isFull())
                .findFirst();
    }
}