package model;

import java.util.Optional;

public class Receptionist {
    private final SimulationData data; // ADD THIS
    private final IRoomAssigner roomAssigner;

    public Receptionist(SimulationData data, IRoomAssigner roomAssigner) {
        this.data = data;
        this.roomAssigner = roomAssigner;
    }

    public Optional<Area> wijsKamerToe(int voorkeurId, int gastId) {
        return roomAssigner.reserveerVrijeKamer(data, voorkeurId, gastId);
    }

    public void checkOut(int gastId) {
        roomAssigner.maakGastVrij(data, gastId);
    }
}