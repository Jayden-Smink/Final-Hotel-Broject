package factory;

import model.Area;
import model.RoomType;

public class RoomFactory {

    public static Area createRuimte(RoomType type, String position, String dimension, int id) {
        switch (type) {
            case ROOM:        return createArea("ROOM",        position, dimension, id);
            case CINEMA:      return createArea("CINEMA",      position, dimension, id);
            case FITNESS:     return createArea("FITNESS",     position, dimension, id);
            case RESTAURANT:  return createArea("RESTAURANT",  position, dimension, id);
            case LIFTSCHACHT: return createArea("LIFTSCHACHT", position, dimension, id);
            case TRAP:        return createArea("TRAP",        position, dimension, id);
            case LOBBY:       return createArea("LOBBY",       position, dimension, id);
            case RECEPTION:   return createArea("RECEPTION",   position, dimension, id);
            default: throw new IllegalArgumentException("Onbekend type: " + type);
        }
    }

    private static Area createArea(String type, String pos, String dim, int id) {
        Area area = new Area();
        area.AreaType = type;
        area.Position = pos;
        area.Dimension = dim;
        area.id = id;
        return area;
    }
}