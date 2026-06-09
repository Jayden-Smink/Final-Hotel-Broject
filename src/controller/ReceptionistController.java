package controller;

import model.*;
import view.LogPanel; // Importeer LogPanel voor de meldingen
import java.util.Optional;

/**
 * Beheert de interactie tussen gasten en de hotelreceptie.
 * Regelt het administratieve incheckproces en stuurt poppetjes fysiek naar de balie of kamers.
 */
public class ReceptionistController {
    private final Receptionist receptionist;
    private final SimulationData data;
    private final LogPanel logPanel;

    // Constructor die nu ook het logPanel meekrijgt om statusberichten naar het scherm te pushen
    public ReceptionistController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
        // De receptionist krijgt de data en een nieuwe RoomController om kamers te kunnen beheren
        this.receptionist = new Receptionist(data, new RoomController());
    }

    /**
     * Start het incheckproces voor een gast op basis van een (optionele) voorkeurskamer.
     * Wordt aangeroepen wanneer de SimulationController een CHECK_IN event afvangt.
     */
    public void handleCheckIn(int guestId, int preferredRoomId) {
        // Vraag aan het receptionist-model om een kamer toe te wijzen (geeft een Optional terug)
        receptionist.wijsKamerToe(preferredRoomId, guestId).ifPresent(room -> {
            Guest guest = data.guests.get(guestId);
            if (guest != null) {
                guest.assignedRoomId = room.id; // Koppel het kamer-ID aan de gast
                sendToReception(guest);         // Stuur de gast eerst naar de receptiebalie om de sleutel te halen
            }
        });
    }

    /**
     * Wijzigt het loopdoel (target) van de gast naar het middelpunt van de receptie.
     */
    public void sendToReception(Guest guest) {
        findAreaByType("RECEPTION").ifPresent(rec -> {
            // Bereken het exacte middelpunt van de receptiebalie
            double tx = (rec.getPos()[0] * data.tileSize) + ((rec.getDim()[0] * data.tileSize) / 2.0);
            double ty = (rec.getPos()[1] * data.tileSize) + 25; // +25px speling voor de voeten op de vloer
            guest.setTarget(tx, ty);
        });
    }

    /**
     * Stuurt een gast vanaf de receptie door naar zijn/haar definitief toegewezen hotelkamer.
     */
    public void sendToRoom(Guest guest) {
        // Veiligheidscheck: heeft de gast daadwerkelijk een kamer gekregen?
        if (guest.assignedRoomId != -1) {
            findAreaById(guest.assignedRoomId).ifPresent(room -> {
                // Bereken het middelpunt van de toegewezen kamer
                double tx = (room.getPos()[0] * data.tileSize) + ((room.getDim()[0] * data.tileSize) / 2.0);
                double ty = (room.getPos()[1] * data.tileSize) + 25;
                guest.setTarget(tx, ty); // Gast gaat nu onderweg naar zijn kamer

                if (logPanel != null) {
                    logPanel.addLog("🔑 Receptie: Gast " + guest.id
                            + " heeft ingecheckt. Kamer " + room.id + " toegewezen.");
                }
            });
        }
    }

    /**
     * Helper-methode: Zoekt in de database naar de eerste hotelruimte van een specifiek type (bijv. "RECEPTION").
     * Retourneert een Optional om NullPointerExceptions te voorkomen als het type niet bestaat.
     */
    private Optional<Area> findAreaByType(String t) {
        return data.areas.stream().filter(a -> a.AreaType.equalsIgnoreCase(t)).findFirst();
    }

    /**
     * Helper-methode: Zoekt in de database naar een specifieke hotelruimte op basis van het unieke ID.
     */
    private Optional<Area> findAreaById(int id) {
        return data.areas.stream().filter(a -> a.id == id).findFirst();
    }
}