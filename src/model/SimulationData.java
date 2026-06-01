package model;

import java.util.*;

public class SimulationData {
    public List<Area> areas;
    public Elevator elevator;
    public Map<Integer, Guest> guests = new HashMap<>();
    public Map<Integer, Queue<Guest>> floorQueues = new HashMap<>();
    public int tileSize = 60;
    public int horizontalOffset = 60;
    public Cleaner cleaner;
    public CleanerSettings cleanerSettings;

    public SimulationData(List<Area> areas, int capacity, int cleaningSeconds) {
        this.areas = areas;
        this.cleanerSettings = new CleanerSettings(cleaningSeconds); // use passed value

        int bottomFloor = areas.stream()
                .mapToInt(a -> a.getPos()[1])
                .max()
                .orElse(0);

        this.elevator = new Elevator(5.0, bottomFloor * tileSize, tileSize);
        this.elevator.maxCapacity = capacity;

        for (Area a : areas) {
            int floorY = a.getPos()[1];
            floorQueues.putIfAbsent(floorY, new LinkedList<>());
        }

        areas.stream()
                .filter(a -> a.AreaType.equalsIgnoreCase("LOBBY"))
                .findFirst()
                .ifPresent(lobby -> {
                    int[] pos = lobby.getPos();
                    int[] dim = lobby.getDim();
                    double centerX = (pos[0] + dim[0] / 2.0) * 60.0;
                    double centerY = (pos[1] * 60.0) + 25.0;
                    cleaner = new Cleaner(0, centerX, centerY);
                    cleaner.homeRoomId = -1;
                    cleaner.targetX = centerX;
                    cleaner.targetY = centerY;
                });
    }
}