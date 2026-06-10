package model;

public class FacilitySettings {
    private int cinemaDurationSeconds;
    private int restaurantDurationSeconds;
    private int fitnessDurationSeconds;

    public FacilitySettings(int cinemaDurationSeconds, int restaurantDurationSeconds, int fitnessDurationSeconds) {
        this.cinemaDurationSeconds = cinemaDurationSeconds;
        this.restaurantDurationSeconds = restaurantDurationSeconds;
        this.fitnessDurationSeconds = fitnessDurationSeconds;
    }

    public int getCinemaDurationFrames() {
        return cinemaDurationSeconds * 60;
    }

    public int getRestaurantDurationFrames() {
        return restaurantDurationSeconds * 60;
    }

    public int getFitnessDurationFrames() {
        return fitnessDurationSeconds * 60;
    }

}