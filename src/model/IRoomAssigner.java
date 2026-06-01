package model;

import java.util.Optional;

public interface IRoomAssigner {
    Optional<Area> reserveerVrijeKamer(SimulationData data, int voorkeurId, int gastId);
    void maakGastVrij(SimulationData data, int gastId);
    Optional<Area> vindVrijeActiviteit(SimulationData data, String type, int gastId);
}