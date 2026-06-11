package model;

import java.util.*;

public class SimulationData {
    public List<Area> areas;
    public Elevator elevator;
    public Map<Integer, Guest> guests = new HashMap<>();
    public int tileSize = 60;
    public int horizontalOffset = 60;

    public int numberOfCleaners = 2;
    public Map<Integer, Cleaner> cleaners = new HashMap<>();

    public CleanerSettings cleanerSettings;
    public GuestSettings guestSettings;
    public FacilitySettings facilitySettings;

    public SimulationData(List<Area> areas, int capacity, int cleaningSeconds, int cinemaDurationSeconds, int restaurantDurationSeconds, int fitnessDurationSeconds, int elevatorWaitSeconds) {
        this.areas = areas;
        this.cleanerSettings = new CleanerSettings(cleaningSeconds);
        this.guestSettings = new GuestSettings(elevatorWaitSeconds);

        // Zoek de onderste verdieping via een gewone for loop
        int bottomFloor = 0;
        for (int i = 0; i < areas.size(); i++) {
            int floorY = areas.get(i).getPos()[1];
            if (floorY > bottomFloor) {
                bottomFloor = floorY;
            }
        }

        this.elevator = new Elevator(5.0, bottomFloor * tileSize, tileSize);
        this.elevator.maxCapacity = capacity;
        this.facilitySettings = new FacilitySettings(cinemaDurationSeconds, restaurantDurationSeconds, fitnessDurationSeconds);
    }
}