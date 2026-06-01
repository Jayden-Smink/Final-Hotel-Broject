package model;

import controller.RoomController;
import java.util.Optional;

public class Receptionist {
    private SimulationData data;

    public Receptionist(SimulationData data) {
        this.data = data;
    }

    public Optional<Area> wijsKamerToe(int voorkeurId, int gastId) {
        return RoomController.reserveerVrijeKamer(data, voorkeurId, gastId);
    }

    public void checkOut(int gastId) {
        RoomController.maakGastVrij(data, gastId);
    }
}