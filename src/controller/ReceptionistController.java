package controller;

import model.*;
import view.LogPanel; // Importeer LogPanel voor de meldingen
import java.util.Optional;

public class ReceptionistController {
    private final Receptionist receptionist;
    private final SimulationData data;
    private final LogPanel logPanel; // Nieuw veld

    // Constructor aangepast om logPanel te ontvangen
    public ReceptionistController(SimulationData data, LogPanel logPanel) {
        this.data = data;
        this.logPanel = logPanel;
        this.receptionist = new Receptionist(data, new RoomController());

    }

    public void handleCheckIn(int guestId, int preferredRoomId) {
        receptionist.wijsKamerToe(preferredRoomId, guestId).ifPresent(room -> {
            Guest g = data.guests.get(guestId);
            if (g != null) {
                g.assignedRoomId = room.id;
                sendToReception(g);
                // Optioneel: log hier dat de kamer is toegewezen
            }
        });
    }

    public void sendToReception(Guest g) {
        findAreaByType("RECEPTION").ifPresent(rec -> {
            double tx = (rec.getPos()[0] * data.tileSize) + ((rec.getDim()[0] * data.tileSize) / 2.0);
            double ty = (rec.getPos()[1] * data.tileSize) + 25;
            g.setTarget(tx, ty);
        });
    }

    public void sendToRoom(Guest g) {
        if (g.assignedRoomId != -1) {
            findAreaById(g.assignedRoomId).ifPresent(room -> {
                double tx = (room.getPos()[0] * data.tileSize) + ((room.getDim()[0] * data.tileSize) / 2.0);
                double ty = (room.getPos()[1] * data.tileSize) + 25;
                g.setTarget(tx, ty);

                if (logPanel != null) {
                    logPanel.addLog("🔑 Receptie: Gast " + g.id
                            + " heeft ingecheckt. Kamer " + room.id + " toegewezen.");
                }
            });
        }
    }

    private Optional<Area> findAreaByType(String t) {
        return data.areas.stream().filter(a -> a.AreaType.equalsIgnoreCase(t)).findFirst();
    }

    private Optional<Area> findAreaById(int id) {
        return data.areas.stream().filter(a -> a.id == id).findFirst();
    }
}