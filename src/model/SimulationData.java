package model;

import java.util.*;

public class SimulationData {
    public List<Area> areas;
    public Elevator elevator;
    public Map<Integer, Guest> guests = new HashMap<>();
    public int tileSize = 60;
    public int horizontalOffset = 60;

    // NIEUW: Vervangt de losse 'public Cleaner cleaner;' voor SRP en meerdere schoonmakers
    public int numberOfCleaners = 2; // Standaard basis van 2 stuks, wordt overschreven bij de start
    public Map<Integer, Cleaner> cleaners = new HashMap<>(); // Beheert alle actieve schoonmakers

    public CleanerSettings cleanerSettings;
    public GuestSettings guestSettings;
    public FacilitySettings facilitySettings;

    public SimulationData(List<Area> areas, int capacity, int cleaningSeconds, int cinemaDurationSeconds, int restaurantDurationSeconds, int fitnessDurationSeconds) {
        this.areas = areas;
        this.cleanerSettings = new CleanerSettings(cleaningSeconds);
        this.guestSettings = new GuestSettings(15);

        int bottomFloor = areas.stream()
                .mapToInt(a -> a.getPos()[1])
                .max()
                .orElse(0);

        this.elevator = new Elevator(5.0, bottomFloor * tileSize, tileSize);
        this.elevator.maxCapacity = capacity;
        this.facilitySettings = new FacilitySettings(cinemaDurationSeconds, restaurantDurationSeconds, fitnessDurationSeconds);

        // OUDE LOGICA VERWIJDERD: De losse cleaner wordt hier niet meer aangemaakt.
        // Dit gebeurt nu dynamisch in de loop van het SimulationPanel via de PersonFactory!
    }
}