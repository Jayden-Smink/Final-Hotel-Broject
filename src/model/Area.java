package model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Area {
    public int id;

    public String AreaType;
    public String Position;
    public String Dimension;

    public String classification;

    /*
     * Jouw JSON gebruikt "Capacity" met hoofdletter C.
     * Deze annotation zorgt dat Gson dat correct inleest in area.capacity.
     */
    @SerializedName("Capacity")
    public int capacity = 1;

    public List<Integer> currentOccupants = new ArrayList<>();

    public int[] getPos() {
        String[] p = Position.split(",");
        return new int[]{
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim())
        };
    }

    public int[] getDim() {
        String[] d = Dimension.split(",");
        return new int[]{
                Integer.parseInt(d[0].trim()),
                Integer.parseInt(d[1].trim())
        };
    }

    public boolean isFull() {
        return currentOccupants.size() >= capacity;
    }
}